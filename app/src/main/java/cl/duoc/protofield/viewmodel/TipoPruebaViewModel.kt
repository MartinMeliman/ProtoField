package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.TipoPruebaEntity
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.Criterio
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.TipoPruebaErrores
import cl.duoc.protofield.model.TipoPruebaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PANTALLA 14 · NUEVA / EDITAR PRUEBA DEL CATÁLOGO (supervisor).
 * Lo que pidió el profe: el administrador tiene poder sobre los tipos de pruebas.
 * Si cambia un criterio, las pruebas antiguas NO cambian (guardan minimoUsado/maximoUsado).
 */
class TipoPruebaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)

    private val _estado = MutableStateFlow(value = TipoPruebaUiState())
    val estado: StateFlow<TipoPruebaUiState> = _estado

    private var cargado = false

    /** @param id > 0 = editar. @param tipoEquipo para "Agregar" desde un grupo del catálogo. */
    fun cargar(id: Long?, tipoEquipo: String?) {
        if (cargado) return
        cargado = true
        viewModelScope.launch {
            val tipo = id?.let { repositorio.obtenerTipoPrueba(it) }
            _estado.update {
                if (tipo == null) it.copy(tipoEquipo = tipoEquipo.orEmpty())
                else it.copy(
                    id = tipo.id,
                    tipoEquipo = tipo.tipoEquipo,
                    nombre = tipo.nombre,
                    unidad = tipo.unidad,
                    criterio = when {
                        tipo.minimo != null && tipo.maximo != null -> Criterio.RANGO
                        tipo.minimo != null -> Criterio.MINIMO
                        tipo.maximo != null -> Criterio.MAXIMO
                        else -> Criterio.LIBRE
                    },
                    minimo = tipo.minimo?.let { m -> Reglas.formatearNumero(m) }.orEmpty(),
                    maximo = tipo.maximo?.let { m -> Reglas.formatearNumero(m) }.orEmpty(),
                    activo = tipo.activo
                )
            }
        }
    }

    fun onTipoEquipoChange(valor: String) {
        _estado.update { it.copy(tipoEquipo = valor, errores = it.errores.copy(tipoEquipo = null, nombre = null)) }
    }

    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }
    }

    fun onUnidadChange(valor: String) = _estado.update { it.copy(unidad = valor.trim()) } // opcional

    fun onCriterioChange(valor: Criterio) {
        _estado.update { it.copy(criterio = valor, errores = it.errores.copy(minimo = null, maximo = null)) }
    }

    fun onMinimoChange(valor: String) {
        _estado.update { it.copy(minimo = valor, errores = it.errores.copy(minimo = null)) }
    }

    fun onMaximoChange(valor: String) {
        _estado.update { it.copy(maximo = valor, errores = it.errores.copy(maximo = null)) }
    }

    fun onActivoChange(valor: Boolean) = _estado.update { it.copy(activo = valor) }

    private val usaMinimo get() = _estado.value.criterio == Criterio.MINIMO || _estado.value.criterio == Criterio.RANGO
    private val usaMaximo get() = _estado.value.criterio == Criterio.MAXIMO || _estado.value.criterio == Criterio.RANGO

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val minimo = Reglas.aNumero(actual.minimo)
        val maximo = Reglas.aNumero(actual.maximo)
        val errores = TipoPruebaErrores(
            tipoEquipo = if (actual.tipoEquipo.isBlank()) "Elige el tipo de equipo" else null,
            nombre = if (actual.nombre.trim().length < 3) "Escribe el nombre (mín. 3 caracteres)" else null,
            minimo = when {
                !usaMinimo -> null
                actual.minimo.isBlank() -> "Ingresa el valor mínimo"
                minimo == null -> "Debe ser un número"
                else -> null
            },
            maximo = when {
                !usaMaximo -> null
                actual.maximo.isBlank() -> "Ingresa el valor máximo"
                maximo == null -> "Debe ser un número"
                actual.criterio == Criterio.RANGO && minimo != null && maximo <= minimo -> "Debe ser mayor que el mínimo"
                else -> null
            }
        )
        val hayErrores = listOfNotNull(errores.tipoEquipo, errores.nombre, errores.minimo, errores.maximo).isNotEmpty()
        _estado.update { it.copy(errores = errores) }
        return !hayErrores
    }

    /** Antes de guardar revisa en Room que el nombre no se repita en ese tipo de equipo. */
    fun guardar(onGuardado: () -> Unit) {
        val actual = _estado.value
        viewModelScope.launch {
            if (repositorio.existeTipoPrueba(actual.tipoEquipo, actual.nombre.trim(), actual.id)) {
                _estado.update { it.copy(errores = it.errores.copy(nombre = "Ya existe esa prueba para ${actual.tipoEquipo.lowercase()}")) }
                return@launch
            }
            repositorio.guardarTipoPrueba(
                TipoPruebaEntity(
                    id = actual.id,
                    tipoEquipo = actual.tipoEquipo,
                    nombre = actual.nombre.trim(),
                    unidad = actual.unidad,
                    minimo = if (usaMinimo) Reglas.aNumero(actual.minimo) else null,
                    maximo = if (usaMaximo) Reglas.aNumero(actual.maximo) else null,
                    activo = actual.activo
                )
            )
            onGuardado()
        }
    }
}