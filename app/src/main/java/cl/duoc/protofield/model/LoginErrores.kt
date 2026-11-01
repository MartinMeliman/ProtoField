package cl.duoc.protofield.model

/**
 * Data class de ERRORES del login: solo los campos que SE VALIDAN.
 * Cada uno es String? y parte en null. null = sin error. Texto = mensaje de error.
 */
data class LoginErrores(
    val rut: String? = null,
    val clave: String? = null
)