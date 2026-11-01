package cl.duoc.protofield.model

data class CondicionesUiState(
    val temperatura: String = "",
    val humedad: String = "",
    val instrumentos: Set<String> = emptySet(),   // ids: "MG-5", "AR-2"...
    val errores: CondicionesErrores = CondicionesErrores()
)

data class CondicionesErrores(
    val temperatura: String? = null,
    val humedad: String? = null,
    val instrumentos: String? = null
)