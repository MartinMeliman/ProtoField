package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.CambioEstadoErrores
import cl.duoc.protofield.model.CambioEstadoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PANTALLA 12 · CAMBIAR ESTADO (solo supervisor).
 * Regla: hay que elegir un estado DISTINTO al actual y explicar el motivo.
 * El cambio queda en el historial (tabla cambios_estado).
 */
class CambioEstadoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)

    private val _estado = MutableStateFlow(value = CambioEstadoUiState())
    val estado: StateFlow<CambioEstadoUiState> = _estado

    fun cargar(equipoId: Long) {
        if (_estado.value.equipo != null) return
        viewModelScope.launch {
            val equipo = repositorio.observarEquipo(equipoId).first()
            val ultima = repositorio.observarPruebas(equipoId).first().firstOrNull()
            _estado.update { it.copy(equipo = equipo, ultimaPrueba = ultima) }
        }
    }

    fun onEstadoChange(valor: String) {
        _estado.update { it.copy(nuevoEstado = valor, errores = it.errores.copy(nuevoEstado = null)) }
    }

    fun onMotivoChange(valor: String) {
        _estado.update { it.copy(motivo = valor, errores = it.errores.copy(motivo = null)) }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val errores = CambioEstadoErrores(
            nuevoEstado = when {
                actual.nuevoEstado.isBlank() -> "Elige el nuevo estado"
                actual.nuevoEstado == actual.equipo?.equipo?.estado -> "Elige un estado distinto al actual"
                else -> null
            },
            motivo = if (actual.motivo.trim().length < 10) "Explica el motivo (mín. 10 caracteres)" else null
        )
        val hayErrores = listOfNotNull(errores.nuevoEstado, errores.motivo).isNotEmpty()
        _estado.update { it.copy(errores = errores) }
        return !hayErrores
    }

    fun guardar(onGuardado: () -> Unit) {
        val actual = _estado.value
        val equipo = actual.equipo?.equipo ?: return
        viewModelScope.launch {
            val usuario = preferencias.obtenerSesion().first()?.nombre.orEmpty()
            repositorio.cambiarEstado(equipo, actual.nuevoEstado, actual.motivo.trim(), usuario)
            onGuardado()
        }
    }
}