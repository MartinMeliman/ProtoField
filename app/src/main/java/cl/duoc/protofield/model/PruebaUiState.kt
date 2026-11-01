package cl.duoc.protofield.model

import cl.duoc.protofield.data.local.entity.TipoPruebaEntity

data class PruebaUiState(
    // Datos que se cargan al abrir la pantalla
    val equipoId: Long = 0,
    val visitaId: Long? = null,
    val numeroVisita: String = "",
    val codigoEquipo: String = "",
    val tipoEquipo: String = "",
    val opciones: List<TipoPruebaEntity> = emptyList(),
    val condicionesVisita: String? = null,
    val tecnico: String = "",

    // Campos del formulario
    val tipoPruebaId: Long? = null,
    val nombreLibre: String = "",
    val valorMedido: String = "",
    val temperatura: String = "",
    val resultadoManual: Boolean? = null,

    // Ayudas que calcula el ViewModel
    val valorAnterior: Double? = null,
    val aprobadaPreview: Boolean? = null,
    val variacion: Double? = null,

    val errores: PruebaErrores = PruebaErrores()
) {


    /** La prueba elegida del catálogo. */
    val seleccionada: TipoPruebaEntity? get() = opciones.firstOrNull { it.id == tipoPruebaId }

    /** Prueba libre = sin mínimo ni máximo: el técnico decide si cumple. */
    val esLibre: Boolean get() = seleccionada?.let { it.minimo == null && it.maximo == null } ?: false

    /** Si NO cumple, la observación pasa a ser obligatoria. */
    val esRechazada: Boolean get() = if (esLibre) resultadoManual == false else aprobadaPreview == false
}