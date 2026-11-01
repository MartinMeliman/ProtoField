package cl.duoc.protofield.model

/**
 * PANTALLA 1 · LOGIN CON RUT.
 * Data class de ESTADO: TODOS los campos del formulario + el campo "errores".
 */

data class LoginUiState(
    val rut: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val errores: LoginErrores = LoginErrores()
)
