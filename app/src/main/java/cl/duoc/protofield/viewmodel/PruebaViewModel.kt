package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.PruebaEntity
import cl.duoc.protofield.data.local.entity.TipoPruebaEntity
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.EstadosVisita
import cl.duoc.protofield.model.Instrumentos
import cl.duoc.protofield.model.PruebaErrores
import cl.duoc.protofield.model.PruebaUiState
import cl.duoc.protofield.model.Reglas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PruebaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)

    private val _estado = MutableStateFlow(value = PruebaUiState())
    val estado: StateFlow<PruebaUiState> = _estado

    // Si se está corrigiendo, la prueba tal como estaba guardada
    private var original: PruebaEntity? = null

    /**
     * Se llama al abrir la pantalla (una sola vez).
     * visitaId = null si es fuera de una visita. pruebaId = la prueba a corregir (null = nueva).
     */
    fun cargar(equipoId: Long, visitaId: Long?, pruebaId: Long? = null) {
        if (_estado.value.equipoId == equipoId) return
        if (pruebaId != null) {
            cargarCorreccion(pruebaId, equipoId)
            return
        }
        viewModelScope.launch {
            val equipo = repositorio.obtenerEquipo(equipoId) ?: return@launch
            val sesion = preferencias.obtenerSesion().first()
            val opciones = repositorio.pruebasActivasPara(equipo.tipo)
            // Solo se liga a la visita si no está cerrada y el equipo está en ella
            val visita = visitaId?.let { repositorio.obtenerVisita(it) }?.takeIf { v ->
                v.estado != EstadosVisita.CERRADA &&
                        repositorio.observarEquiposDeVisita(v.id).first().any { it.equipo.id == equipoId }
            }
            val condiciones = visita?.let { v ->
                v.temperatura?.let { t ->
                    val humedad = v.humedad?.let { h -> Reglas.formatearNumero(h) } ?: "—"
                    val instrumentos = v.instrumentos.split(",").filter { it.isNotBlank() }.joinToString { Instrumentos.nombre(it) }
                    "${Reglas.formatearNumero(t)} °C · $humedad % humedad · $instrumentos"
                }
            }
            // Preselecciona la primera prueba que todavía no se hizo en esta visita
            val hechas = visita?.let { repositorio.tiposHechosEnVisita(equipoId, it.id) }.orEmpty()
            val primera = opciones.firstOrNull { it.nombre !in hechas } ?: opciones.firstOrNull()
            _estado.update {
                it.copy(
                    equipoId = equipo.id,
                    visitaId = visita?.id,
                    numeroVisita = visita?.numero.orEmpty(),
                    codigoEquipo = equipo.codigo,
                    tipoEquipo = equipo.tipo,
                    opciones = opciones,
                    condicionesVisita = condiciones,
                    temperatura = visita?.temperatura?.let { t -> Reglas.formatearNumero(t) }.orEmpty(),
                    tecnico = sesion?.nombre.orEmpty()
                )
            }
            primera?.let { onTipoPruebaChange(it.id) }
        }
    }

    /** CORREGIR: carga la prueba guardada con el criterio que se usó ESE día. */
    private fun cargarCorreccion(pruebaId: Long, equipoId: Long) {
        viewModelScope.launch {
            // Regla: solo se corrige si la prueba es de este equipo y su visita no está cerrada
            val prueba = repositorio.obtenerPrueba(pruebaId)
                ?.takeIf { it.equipoId == equipoId && repositorio.sePuedeCorregir(it) } ?: return@launch
            val equipo = repositorio.obtenerEquipo(equipoId) ?: return@launch
            val visita = prueba.visitaId?.let { repositorio.obtenerVisita(it) }
            original = prueba
            // Una sola opción: la prueba como se evaluó ese día (al corregir no se cambia el tipo)
            val criterioDelDia = TipoPruebaEntity(
                tipoEquipo = equipo.tipo, nombre = prueba.tipoPrueba, unidad = prueba.unidad,
                minimo = prueba.minimoUsado, maximo = prueba.maximoUsado
            )
            val esLibre = prueba.minimoUsado == null && prueba.maximoUsado == null
            val anterior = repositorio.pruebaAnteriorA(prueba)
            _estado.update {
                it.copy(
                    pruebaId = prueba.id,
                    equipoId = equipo.id,
                    visitaId = prueba.visitaId,
                    numeroVisita = visita?.numero.orEmpty(),
                    codigoEquipo = equipo.codigo,
                    tipoEquipo = equipo.tipo,
                    opciones = listOf(criterioDelDia),
                    tipoPruebaId = criterioDelDia.id,
                    nombreLibre = if (esLibre) prueba.tipoPrueba else "",
                    valorMedido = Reglas.formatearNumero(prueba.valorMedido),
                    temperatura = Reglas.formatearNumero(prueba.temperaturaAmbiente),
                    resultadoManual = if (esLibre) prueba.aprobada else null,
                    observaciones = prueba.observaciones,
                    tecnico = prueba.tecnico,
                    valorAnterior = anterior?.valorMedido
                )
            }
            recalcular()
        }
    }

    fun onTipoPruebaChange(id: Long) {
        _estado.update {
            it.copy(tipoPruebaId = id, resultadoManual = null, errores = it.errores.copy(tipoPrueba = null, resultado = null))
        }
        recalcular()
        // Busca la última medición de ESTE tipo para mostrarla como referencia
        val nombre = _estado.value.seleccionada?.nombre ?: return
        viewModelScope.launch {
            val anterior = repositorio.ultimaPrueba(_estado.value.equipoId, nombre)
            _estado.update { it.copy(valorAnterior = anterior?.valorMedido) }
            recalcular()
        }
    }

    fun onNombreLibreChange(valor: String) {
        _estado.update { it.copy(nombreLibre = valor, errores = it.errores.copy(nombreLibre = null)) }
    }

    fun onValorMedidoChange(valor: String) {
        _estado.update { it.copy(valorMedido = valor, errores = it.errores.copy(valorMedido = null)) }
        recalcular()
    }

    fun onTemperaturaChange(valor: String) {
        _estado.update { it.copy(temperatura = valor, errores = it.errores.copy(temperatura = null)) }
    }

    fun onResultadoManualChange(cumple: Boolean) {
        _estado.update { it.copy(resultadoManual = cumple, errores = it.errores.copy(resultado = null)) }
    }

    fun onObservacionesChange(valor: String) {
        _estado.update { it.copy(observaciones = valor, errores = it.errores.copy(observaciones = null)) }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val valor = Reglas.aNumero(actual.valorMedido)
        val temperatura = Reglas.aNumero(actual.temperatura)
        val errores = PruebaErrores(
            tipoPrueba = if (actual.seleccionada == null) "Selecciona la prueba realizada" else null,
            nombreLibre = if (actual.esLibre && actual.nombreLibre.trim().length < 3) "Escribe el nombre de la prueba" else null,
            valorMedido = when {
                actual.valorMedido.isBlank() -> "Ingresa el valor medido"
                valor == null -> "Debe ser un número (ej: 1500 o 0,3)"
                valor < 0 -> "No puede ser negativo"
                else -> null
            },
            temperatura = when {
                actual.temperatura.isBlank() -> "Ingresa la temperatura ambiente"
                temperatura == null -> "Debe ser un número"
                temperatura !in -20.0..60.0 -> "Entre -20 y 60 °C"
                else -> null
            },
            resultado = if (actual.esLibre && actual.resultadoManual == null) "Indica si cumple o no" else null,
            observaciones = if (actual.esRechazada && actual.observaciones.trim().length < 10)
                "Si no cumple, explica qué pasó (mín. 10 caracteres)" else null
        )
        val hayErrores = listOfNotNull(
            errores.tipoPrueba, errores.nombreLibre, errores.valorMedido,
            errores.temperatura, errores.resultado, errores.observaciones
        ).isNotEmpty()
        _estado.update { it.copy(errores = errores) }
        return !hayErrores
    }

    /**
     * Guarda en Room con el criterio que se usó HOY (minimoUsado / maximoUsado).
     * Si es una corrección, actualiza la misma prueba (misma fecha, técnico y criterio).
     */
    fun guardar(onGuardado: () -> Unit) {
        val actual = _estado.value
        val tipo = actual.seleccionada ?: return
        val valor = Reglas.aNumero(actual.valorMedido) ?: return
        val aprobada = if (actual.esLibre) actual.resultadoManual == true
        else Reglas.cumpleCriterio(valor, tipo.minimo, tipo.maximo)
        viewModelScope.launch {
            val base = original
            if (base != null) {
                repositorio.corregirPrueba(
                    base.copy(
                        tipoPrueba = if (actual.esLibre) actual.nombreLibre.trim() else base.tipoPrueba,
                        valorMedido = valor,
                        temperaturaAmbiente = Reglas.aNumero(actual.temperatura) ?: base.temperaturaAmbiente,
                        observaciones = actual.observaciones.trim(),
                        aprobada = aprobada
                    )
                )
                onGuardado()
                return@launch
            }
            repositorio.guardarPrueba(
                PruebaEntity(
                    equipoId = actual.equipoId,
                    visitaId = actual.visitaId,
                    tipoPrueba = if (actual.esLibre) actual.nombreLibre.trim() else tipo.nombre,
                    valorMedido = valor,
                    unidad = tipo.unidad,
                    minimoUsado = tipo.minimo,
                    maximoUsado = tipo.maximo,
                    temperaturaAmbiente = Reglas.aNumero(actual.temperatura) ?: 0.0,
                    tecnico = actual.tecnico,
                    observaciones = actual.observaciones.trim(),
                    aprobada = aprobada
                )
            )
            onGuardado()
        }
    }

    /** Veredicto y variación en vivo mientras el técnico escribe. */
    private fun recalcular() {
        val actual = _estado.value
        val tipo = actual.seleccionada
        val valor = Reglas.aNumero(actual.valorMedido)
        val veredicto = if (tipo != null && valor != null && !actual.esLibre)
            Reglas.cumpleCriterio(valor, tipo.minimo, tipo.maximo) else null
        val variacion = valor?.let { Reglas.variacionPorcentual(it, actual.valorAnterior) }
        _estado.update { it.copy(aprobadaPreview = veredicto, variacion = variacion) }
    }
}