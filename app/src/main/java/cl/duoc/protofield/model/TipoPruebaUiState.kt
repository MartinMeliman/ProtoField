package cl.duoc.protofield.model

/** Cómo se aprueba una prueba del catálogo. */
enum class Criterio(val etiqueta: String) {
    MINIMO("Mínimo"),
    MAXIMO("Máximo"),
    RANGO("Rango"),
    LIBRE("Libre")
}

/** PANTALLA 14 · NUEVA / EDITAR PRUEBA DEL CATÁLOGO (supervisor) — MODELO. */
data class TipoPruebaUiState(
    val id: Long = 0,                 // 0 = nueva
    val tipoEquipo: String = "",
    val nombre: String = "",
    val unidad: String = "",
    val criterio: Criterio = Criterio.MINIMO,
    val minimo: String = "",
    val maximo: String = "",
    val activo: Boolean = true,
    val errores: TipoPruebaErrores = TipoPruebaErrores()
) {
    val esEdicion: Boolean get() = id != 0L
}

data class TipoPruebaErrores(
    val tipoEquipo: String? = null,
    val nombre: String? = null,
    val minimo: String? = null,
    val maximo: String? = null
)