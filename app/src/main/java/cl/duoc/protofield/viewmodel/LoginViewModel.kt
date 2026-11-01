package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.LoginErrores
import cl.duoc.protofield.model.LoginUiState
import cl.duoc.protofield.model.Reglas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)

    // PRIVADO y MUTABLE: solo este ViewModel lo cambia (encapsulamiento).
    private val _estado = MutableStateFlow(value = LoginUiState())

    // PÚBLICO y de solo lectura: la pantalla solo lo observa.
    val estado: StateFlow<LoginUiState> = _estado

    // Al escribir: se actualiza ESE campo y se limpia su error.
    fun onRutChange(valor: String) {
        _estado.update { it.copy(rut = Reglas.formatearRut(valor), errores = it.errores.copy(rut = null)) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errores = it.errores.copy(clave = null)) }
    }

    /** Revisa el formato de todos los campos. Devuelve true si se puede avanzar. */
    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val errores = LoginErrores(
            rut = when {
                actual.rut.isBlank() -> "Ingresa tu RUT"
                !Reglas.rutValido(actual.rut) -> "RUT inválido: revisa el dígito verificador"
                else -> null
            },
            clave = when {
                actual.clave.isBlank() -> "La contraseña es obligatoria"
                actual.clave.length < 6 -> "Mínimo 6 caracteres"
                else -> null
            }
        )
        // Bandera: si queda algún mensaje (no null), hay errores.
        val hayErrores = listOfNotNull(errores.rut, errores.clave).isNotEmpty()
        _estado.update { it.copy(errores = errores) }
        return !hayErrores
    }

    /**
     * Busca el RUT en Room. Si existe, guarda la sesión en el DataStore
     * (id, nombre y rol; NUNCA la clave) y avisa a la pantalla para que navegue.
     */
    fun iniciarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val usuario = repositorio.buscarUsuarioPorRut(Reglas.rutParaGuardar(_estado.value.rut))
            if (usuario == null) {
                _estado.update {
                    it.copy(cargando = false, errores = it.errores.copy(rut = "Este RUT no está inscrito en la empresa"))
                }
                return@launch
            }
            preferencias.iniciarSesion(usuario.id, usuario.nombre, usuario.rol)
            _estado.update { it.copy(cargando = false) }
            onListo()
        }
    }
}