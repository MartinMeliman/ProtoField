package cl.duoc.protofield.model

/** PANTALLA 3 · ESCANEAR QR — MODELO. */
data class EscanerUiState(
    val codigoManual: String = "",
    val buscando: Boolean = false,
    val noRegistrado: String? = null,   // código leído que no existe → ofrecer registrarlo
    val error: String? = null
)