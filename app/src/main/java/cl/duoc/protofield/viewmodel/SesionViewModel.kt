package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * PANTALLA DE CARGA (apoyo): decide con qué pantalla parte la app.
 * Mismo patrón de EstadoViewModel (Ejemplo 5):
 *  - el estado parte en null → la pantalla muestra el circulito de carga
 *  - se cargan los datos ficticios (solo la primera vez) y se lee el DataStore
 *  - el estado pasa a true (hay sesión) o false (no hay)
 */
class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = UserPreferences(application)
    private val repositorio = ProtocoloRepository.desde(application)

    // null = todavía cargando, true = hay sesión, false = no hay sesión
    private val _haySesion = MutableStateFlow<Boolean?>(value = null)
    val haySesion: StateFlow<Boolean?> = _haySesion

    init {
        viewModelScope.launch {
            repositorio.cargarDatosIniciales()
            delay(timeMillis = 800) // pausa corta para que se vea la animación de carga
            _haySesion.value = preferencias.obtenerSesion().first() != null
        }
    }
}