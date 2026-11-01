package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.VisitaResumen
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.EstadosVisita
import cl.duoc.protofield.model.Roles
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class VisitasUiState(
    val cargando: Boolean = true,
    val rol: String = Roles.TECNICO,
    val nombre: String = "",
    val hoy: List<VisitaResumen> = emptyList(),
    val proximas: List<VisitaResumen> = emptyList(),
    val cerradas: List<VisitaResumen> = emptyList()
)

class VisitasViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)

    val estado: StateFlow<VisitasUiState> =
        combine(preferencias.obtenerSesion(), repositorio.observarVisitas()) { sesion, visitas ->
            val rol = sesion?.rol ?: Roles.TECNICO
            val visibles = if (rol == Roles.TECNICO) visitas.filter { it.tecnicoId == sesion?.usuarioId } else visitas
            val limite = finDeHoy()
            val abiertas = visibles.filter { it.estado != EstadosVisita.CERRADA }
            VisitasUiState(
                cargando = false,
                rol = rol,
                nombre = sesion?.nombre.orEmpty(),
                // las abiertas atrasadas también se muestran en "hoy" para que no se olviden
                hoy = abiertas.filter { it.fechaHora < limite },
                proximas = abiertas.filter { it.fechaHora >= limite },
                cerradas = visibles.filter { it.estado == EstadosVisita.CERRADA }.sortedByDescending { it.fechaHora }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VisitasUiState())

    /** Medianoche de hoy (en milisegundos): lo que es antes, es "de hoy". */
    private fun finDeHoy(): Long {
        val inicioDeHoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return inicioDeHoy + 24L * 60 * 60 * 1000
    }
}