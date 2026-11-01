package cl.duoc.protofield.model

/** Errores de "Registrar equipo". Marca, modelo, ubicación y foto son opcionales. */
data class EquipoErrores(
    val codigo: String? = null,
    val tipo: String? = null,
    val instalacion: String? = null,
    val serie: String? = null
)