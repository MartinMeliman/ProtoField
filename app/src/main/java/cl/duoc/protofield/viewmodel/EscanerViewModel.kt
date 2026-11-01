package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.device.GeneradorQr
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.EscanerUiState
import cl.duoc.protofield.model.Reglas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PANTALLA 3 · ESCANEAR QR.
 * La VISTA abre el escáner (necesita la Activity). Cuando lee un código,
 * se lo entrega a este ViewModel, que decide qué hacer (lógica):
 *  - si el equipo existe → abrir su detalle
 *  - si no existe → ofrecer registrarlo con el código ya escrito
 */
class EscanerViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)

    private val _estado = MutableStateFlow(value = EscanerUiState())
    val estado: StateFlow<EscanerUiState> = _estado

    fun onCodigoManualChange(valor: String) {
        _estado.update { it.copy(codigoManual = valor.uppercase().trim(), error = null) }
    }

    /** Resultado del escáner. "protofield://equipo/TR-0001" → "TR-0001". null = canceló. */
    fun onCodigoLeido(texto: String?, onEncontrado: (Long) -> Unit) {
        if (texto == null) return
        val codigo = texto.removePrefix(GeneradorQr.PREFIJO).trim().uppercase()
        if (!Reglas.REGEX_CODIGO.matches(codigo)) {
            _estado.update { it.copy(error = "Esa etiqueta no es de ProtoField") }
            return
        }
        buscar(codigo, onEncontrado)
    }

    /** Búsqueda escribiendo el código a mano (por si la etiqueta está dañada). */
    fun buscarManual(onEncontrado: (Long) -> Unit) {
        val codigo = _estado.value.codigoManual
        val error = when {
            codigo.isBlank() -> "Escribe el código del equipo"
            !Reglas.REGEX_CODIGO.matches(codigo) -> "Formato: 2 letras, guion y 4 números (ej: TR-0001)"
            else -> null
        }
        if (error != null) {
            _estado.update { it.copy(error = error) }
            return
        }
        buscar(codigo, onEncontrado)
    }

    private fun buscar(codigo: String, onEncontrado: (Long) -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(buscando = true) }
            val equipo = repositorio.buscarEquipoPorCodigo(codigo)
            _estado.update { it.copy(buscando = false, noRegistrado = if (equipo == null) codigo else null) }
            equipo?.let { onEncontrado(it.id) }
        }
    }

    fun cerrarDialogo() {
        _estado.update { it.copy(noRegistrado = null) }
    }
}