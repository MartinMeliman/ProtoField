package cl.duoc.protofield.model

/** Errores de "Nueva prueba": solo los campos que se validan. */
data class PruebaErrores(
    val tipoPrueba: String? = null,
    val nombreLibre: String? = null,
    val valorMedido: String? = null,
    val temperatura: String? = null,
    val resultado: String? = null,
    val observaciones: String? = null
)