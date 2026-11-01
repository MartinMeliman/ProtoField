package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.EquipoConUbicacion
import cl.duoc.protofield.data.repository.ProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EquiposUiState(
    val busqueda: String = "",
    val filtro: String? = null,                 // null = todos los estados
    val equipos: List<EquipoConUbicacion> = emptyList(),
    val conteo: Map<String, Int> = emptyMap(),  // cuántos hay por estado (para los chips)
    val total: Int = 0
)

/**
 * PANTALLA 10 · EQUIPOS: buscador + filtro por estado.
 * "combine" junta 3 flujos (búsqueda, filtro y la tabla). Si cambia cualquiera,
 * la lista se recalcula sola.
 */
class EquiposViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)

    private val _busqueda = MutableStateFlow("")
    private val _filtro = MutableStateFlow<String?>(null)

    val estado: StateFlow<EquiposUiState> =
        combine(_busqueda, _filtro, repositorio.observarEquipos()) { texto, filtro, todos ->
            val filtrados = todos.filter { e ->
                val coincideEstado = filtro == null || e.equipo.estado == filtro
                val coincideTexto = texto.isBlank() ||
                        e.equipo.codigo.contains(texto, ignoreCase = true) ||
                        e.instalacionNombre.contains(texto, ignoreCase = true) ||
                        e.clienteNombre.contains(texto, ignoreCase = true)
                coincideEstado && coincideTexto
            }
            EquiposUiState(
                busqueda = texto,
                filtro = filtro,
                equipos = filtrados,
                conteo = todos.groupingBy { it.equipo.estado }.eachCount(),
                total = todos.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EquiposUiState())

    fun onBusquedaChange(valor: String) {
        _busqueda.value = valor
    }

    /** Tocar el chip activo lo apaga (vuelve a "todos"). */
    fun onFiltroChange(estado: String?) {
        _filtro.value = if (_filtro.value == estado) null else estado
    }
}