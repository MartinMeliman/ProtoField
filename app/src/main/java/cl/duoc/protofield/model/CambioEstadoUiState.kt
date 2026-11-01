package cl.duoc.protofield.model

import cl.duoc.protofield.data.local.entity.EquipoConUbicacion
import cl.duoc.protofield.data.local.entity.PruebaEntity

/** PANTALLA 12 · CAMBIAR ESTADO (supervisor) — MODELO. */
data class CambioEstadoUiState(
    val equipo: EquipoConUbicacion? = null,
    val ultimaPrueba: PruebaEntity? = null,
    val nuevoEstado: String = "",
    val motivo: String = "",
    val errores: CambioEstadoErrores = CambioEstadoErrores()
)

data class CambioEstadoErrores(
    val nuevoEstado: String? = null,
    val motivo: String? = null
)