package cl.duoc.protofield.model

import cl.duoc.protofield.data.local.entity.TipoPruebaEntity

data class PruebaUiState(
    // Datos que se cargan al abrir la pantalla
    val pruebaId: Long? = null,                         // null = prueba nueva; con valor = corregir esa prueba
    val equipoId: Long = 0,
    val visitaId: Long? = null,
    val numeroVisita: String = "",
    val codigoEquipo: String = "",
    val tipoEquipo: String = "",
    val opciones: List<TipoPruebaEntity> = emptyList(), // las pruebas ACTIVAS del catálogo para este tipo
    val condicionesVisita: String? = null,              // "18 °C · 55 % · MG-5" si la visita las tiene
    val tecnico: String = "",                           // sale de la sesión (no se escribe)

    // Campos del formulario
    val tipoPruebaId: Long? = null,
    val nombreLibre: String = "",          // solo si la prueba es "libre" (tipo Otro)
    val valorMedido: String = "",
    val temperatura: String = "",
    val resultadoManual: Boolean? = null,  // solo prueba libre: true cumple, false no cumple
    val observaciones: String = "",

    // Ayudas que calcula el ViewModel
    val valorAnterior: Double? = null,
    val aprobadaPreview: Boolean? = null,  // veredicto en vivo mientras escribe
    val variacion: Double? = null,         // % de cambio respecto a la anterior

    val errores: PruebaErrores = PruebaErrores()
) {
    /** true = se está corrigiendo una prueba ya guardada. */
    val esCorreccion: Boolean get() = pruebaId != null

    /** La prueba elegida del catálogo. */
    val seleccionada: TipoPruebaEntity? get() = opciones.firstOrNull { it.id == tipoPruebaId }

    /** Prueba libre = sin mínimo ni máximo: el técnico decide si cumple. */
    val esLibre: Boolean get() = seleccionada?.let { it.minimo == null && it.maximo == null } ?: false

    /** Si NO cumple, la observación pasa a ser obligatoria. */
    val esRechazada: Boolean get() = if (esLibre) resultadoManual == false else aprobadaPreview == false
}