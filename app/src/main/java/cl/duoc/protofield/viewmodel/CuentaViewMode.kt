package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.storage.Sesion
import cl.duoc.protofield.data.local.storage.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel pequeño que comparten la barra inferior y el menú de la cuenta:
 * sabe quién inició sesión (y su rol) y permite cerrar sesión.
 */
class CuentaViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = UserPreferences(application)

    /** stateIn convierte el Flow del DataStore en un StateFlow que la vista puede observar. */
    val sesion: StateFlow<Sesion?> = preferencias.obtenerSesion()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            preferencias.cerrarSesion()
            onListo()
        }
    }
}