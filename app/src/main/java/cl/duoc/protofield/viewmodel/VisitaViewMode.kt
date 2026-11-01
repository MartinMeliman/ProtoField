package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.EquipoEnVisita
import cl.duoc.protofield.data.local.entity.VisitaDetalle
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.CondicionesErrores
import cl.duoc.protofield.model.CondicionesUiState
import cl.duoc.protofield.model.EstadosVisita
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.Roles
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisitaUiState(
    val detalle: VisitaDetalle? = null,
    val equipos: List<EquipoEnVisita> = emptyList(),
    val esTecnicoAsignado: Boolean = false,       // solo él registra pruebas y cierra
    val mostrarCondiciones: Boolean = false,      // hoja inferior abierta
    val condiciones: CondicionesUiState = CondicionesUiState()
) {
    val probados: Int get() = equipos.count { it.pruebas > 0 }
    val faltan: Int get() = equipos.size - probados
    /** El próximo equipo sin pruebas: el botón "Siguiente · Probar" lleva directo ahí. */
    val siguiente: EquipoEnVisita? get() = equipos.firstOrNull { it.pruebas == 0 }
    val estaCerrada: Boolean get() = detalle?.visita?.estado == EstadosVisita.CERRADA
    /** Regla de negocio: no se cierra un protocolo con equipos sin probar. */
    val puedeCerrar: Boolean get() = equipos.isNotEmpty() && faltan == 0 && esTecnicoAsignado && !estaCerrada
}

/**
 * PANTALLA 4 · VISITA Y PROTOCOLO (+ hoja "Condiciones de la visita").
 */
class VisitaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)

    private val _estado = MutableStateFlow(value = VisitaUiState())
    val estado: StateFlow<VisitaUiState> = _estado

    private var trabajo: Job? = null

    fun cargar(visitaId: Long) {
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            combine(
                repositorio.observarVisita(visitaId),
                repositorio.observarEquiposDeVisita(visitaId),
                preferencias.obtenerSesion()
            ) { detalle, equipos, sesion ->
                Triple(detalle, equipos, sesion)
            }.collect { (detalle, equipos, sesion) ->
                _estado.update {
                    it.copy(
                        detalle = detalle,
                        equipos = equipos,
                        esTecnicoAsignado = sesion != null && sesion.rol == Roles.TECNICO && sesion.usuarioId == detalle?.visita?.tecnicoId
                    )
                }
            }
        }
    }

    // ---------------- Hoja "Condiciones de la visita" ----------------

    /** Abre la hoja con los valores que ya tenga la visita. */
    fun abrirCondiciones() {
        val visita = _estado.value.detalle?.visita ?: return
        _estado.update {
            it.copy(
                mostrarCondiciones = true,
                condiciones = CondicionesUiState(
                    temperatura = visita.temperatura?.let { t -> Reglas.formatearNumero(t) }.orEmpty(),
                    humedad = visita.humedad?.let { h -> Reglas.formatearNumero(h) }.orEmpty(),
                    instrumentos = visita.instrumentos.split(",").filter { s -> s.isNotBlank() }.toSet()
                )
            )
        }
    }

    fun cerrarCondiciones() {
        _estado.update { it.copy(mostrarCondiciones = false) }
    }

    fun onTemperaturaChange(valor: String) {
        _estado.update { it.copy(condiciones = it.condiciones.copy(temperatura = valor, errores = it.condiciones.errores.copy(temperatura = null))) }
    }

    fun onHumedadChange(valor: String) {
        _estado.update { it.copy(condiciones = it.condiciones.copy(humedad = valor, errores = it.condiciones.errores.copy(humedad = null))) }
    }

    fun onInstrumentoToggle(id: String) {
        _estado.update {
            val actuales = it.condiciones.instrumentos
            val nuevos = if (id in actuales) actuales - id else actuales + id
            it.copy(condiciones = it.condiciones.copy(instrumentos = nuevos, errores = it.condiciones.errores.copy(instrumentos = null)))
        }
    }

    fun validarCondiciones(): Boolean {
        val c = _estado.value.condiciones
        val temperatura = Reglas.aNumero(c.temperatura)
        val humedad = Reglas.aNumero(c.humedad)
        val errores = CondicionesErrores(
            temperatura = when {
                c.temperatura.isBlank() -> "Ingresa la temperatura"
                temperatura == null -> "Debe ser un número"
                temperatura !in -20.0..60.0 -> "Entre -20 y 60 °C"
                else -> null
            },
            humedad = when {
                c.humedad.isBlank() -> "Ingresa la humedad"
                humedad == null -> "Debe ser un número"
                humedad !in 0.0..100.0 -> "Entre 0 y 100 %"
                else -> null
            },
            instrumentos = if (c.instrumentos.isEmpty()) "Elige al menos un instrumento" else null
        )
        val hayErrores = listOfNotNull(errores.temperatura, errores.humedad, errores.instrumentos).isNotEmpty()
        _estado.update { it.copy(condiciones = it.condiciones.copy(errores = errores)) }
        return !hayErrores
    }

    fun guardarCondiciones() {
        val visita = _estado.value.detalle?.visita ?: return
        val c = _estado.value.condiciones
        viewModelScope.launch {
            repositorio.actualizarVisita(
                visita.copy(
                    temperatura = Reglas.aNumero(c.temperatura),
                    humedad = Reglas.aNumero(c.humedad),
                    instrumentos = c.instrumentos.sorted().joinToString(","),
                    estado = if (visita.estado == EstadosVisita.PROGRAMADA) EstadosVisita.EN_CURSO else visita.estado
                )
            )
            _estado.update { it.copy(mostrarCondiciones = false) }
        }
    }
}