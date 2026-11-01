package cl.duoc.protofield.model
/**
 * Valores fijos de la app. Tenerlos en UN solo lugar evita errores de tipeo

 */

/** Los dos roles de la app . */
object Roles {
    const val TECNICO = "TECNICO"
    const val SUPERVISOR = "SUPERVISOR"

    fun nombre(rol: String) = if (rol == SUPERVISOR) "Supervisor" else "Técnico de terreno"
}

/** Estados de un EQUIPO. Solo el supervisor los cambia (con un motivo). */
object EstadosEquipo {
    const val PENDIENTE = "Pendiente de prueba"
    const val OPERATIVO = "Operativo"
    const val MANTENCION = "En mantención"
    const val FUERA = "Fuera de servicio"

    val todos = listOf(PENDIENTE, OPERATIVO, MANTENCION, FUERA)

    fun descripcion(estado: String) = when (estado) {
        PENDIENTE -> "Recién registrado o con prueba vencida. Hay que programarle una visita."
        OPERATIVO -> "Pasó sus pruebas y está funcionando."
        MANTENCION -> "Fuera de uso temporalmente por un trabajo programado."
        FUERA -> "No se puede usar. Falló una prueba o está dañado."
        else -> ""
    }
}

/** Estados de una VISITA (la visita, al cerrarse, es el protocolo del cliente). */
object EstadosVisita {
    const val PROGRAMADA = "Programada"
    const val EN_CURSO = "En curso"
    const val CERRADA = "Cerrada"
}

/** Tipos de equipo que nombra el caso de Induztek y la letra con que parte su código. */
object TiposEquipo {
    val todos = listOf("Transformador", "Relé de protección", "Celda", "Cable de poder", "Otro")

    private val prefijos = mapOf(
        "Transformador" to "TR",
        "Relé de protección" to "RP",
        "Celda" to "CL",
        "Cable de poder" to "CB",
        "Otro" to "OT"
    )

    fun prefijo(tipo: String): String = prefijos[tipo].orEmpty()

    /** Al revés: de "RP-0003" saca "Relé de protección" (lo usa el escáner QR). */
    fun desdeCodigo(codigo: String): String =
        prefijos.entries.firstOrNull { codigo.startsWith(it.value) }?.key.orEmpty()
}

/** Instrumentos de medición de Induztek (ficticios). Van en el protocolo con su calibración. */
data class Instrumento(val id: String, val nombre: String, val calibracion: String)

object Instrumentos {
    val todos = listOf(
        Instrumento("MG-5", "Megóhmetro MG-5", "03-2026"),
        Instrumento("AR-2", "Analizador de relés AR-2", "01-2026"),
        Instrumento("MO-1", "Micro-ohmímetro MO-1", "05-2026")
    )

    fun nombre(id: String) = todos.firstOrNull { it.id == id }?.nombre ?: id
}