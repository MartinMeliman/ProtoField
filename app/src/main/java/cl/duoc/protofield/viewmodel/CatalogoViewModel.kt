package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.TipoPruebaEntity
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.TiposEquipo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * PANTALLA 13 · CATÁLOGO DE PRUEBAS (supervisor).
 * Agrupa las pruebas por tipo de equipo, en el orden de TiposEquipo.
 */
class CatalogoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)

    /** Mapa: "Transformador" → [sus pruebas], "Relé de protección" → [...], ... */
    val grupos: StateFlow<Map<String, List<TipoPruebaEntity>>> =
        repositorio.observarCatalogo()
            .map { todas -> TiposEquipo.todos.associateWith { tipo -> todas.filter { it.tipoEquipo == tipo } } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}