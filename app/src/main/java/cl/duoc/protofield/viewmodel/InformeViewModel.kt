package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.PruebaConEquipo
import cl.duoc.protofield.data.local.entity.VisitaDetalle
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.Roles
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class InformeUiState(
    val detalle: VisitaDetalle? = null,
    val pruebas: List<PruebaConEquipo> = emptyList(),
    val totalEquipos: Int = 0,
    val rol: String = Roles.TECNICO
) {
    val rechazadas: Int get() = pruebas.count { !it.prueba.aprobada }
}

/** PANTALLA 9 · PROTOCOLO PARA EL CLIENTE (solo lectura). */
class InformeViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)

    private val _estado = MutableStateFlow(value = InformeUiState())
    val estado: StateFlow<InformeUiState> = _estado

    private var trabajo: Job? = null

    fun cargar(visitaId: Long) {
        if (trabajo != null) return
        trabajo = viewModelScope.launch {
            combine(
                repositorio.observarVisita(visitaId),
                repositorio.observarPruebasDeVisita(visitaId),
                repositorio.observarEquiposDeVisita(visitaId),
                preferencias.obtenerSesion()
            ) { detalle, pruebas, equipos, sesion ->
                InformeUiState(detalle, pruebas, equipos.size, sesion?.rol ?: Roles.TECNICO)
            }.collect { _estado.value = it }
        }
    }
}