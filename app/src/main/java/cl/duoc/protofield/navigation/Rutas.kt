package cl.duoc.protofield.navigation

import android.net.Uri

/**
 * Nombres de las rutas (pantallas). Tenerlos en un solo lugar evita errores de tipeo.
 * - {x} = parámetro obligatorio.  ?x={x} = parámetro opcional (con valor por defecto).
 * Así se pasa información entre pantallas (ej: qué equipo abrir).
 */
object Rutas {
    const val CARGA = "carga"
    const val LOGIN = "login"                                                   // 1
    const val VISITAS = "visitas"                                               // 2
    const val ESCANER = "escaner?visitaId={visitaId}"                           // 3
    const val VISITA = "visita/{visitaId}"                                      // 4
    const val DETALLE = "detalle/{equipoId}?visitaId={visitaId}"                // 5
    const val PRUEBA = "prueba/{equipoId}?visitaId={visitaId}&pruebaId={pruebaId}" // 6
    const val EQUIPO_FORM = "equipo_form?equipoId={equipoId}&visitaId={visitaId}&codigo={codigo}" // 7
    const val CERRAR = "cerrar/{visitaId}"                                      // 8
    const val INFORME = "informe/{visitaId}"                                    // 9
    const val EQUIPOS = "equipos"                                               // 10
    const val NUEVA_VISITA = "nueva_visita?equipoId={equipoId}"                 // 11
    const val CAMBIAR_ESTADO = "cambiar_estado/{equipoId}"                      // 12
    const val CATALOGO = "catalogo"                                             // 13
    const val TIPO_PRUEBA = "tipo_prueba?id={id}&tipoEquipo={tipoEquipo}"       // 14

    // Funciones para armar la ruta con los valores reales. -1 = "no viene".
    fun escaner(visitaId: Long = -1) = "escaner?visitaId=$visitaId"
    fun visita(visitaId: Long) = "visita/$visitaId"
    fun detalle(equipoId: Long, visitaId: Long = -1) = "detalle/$equipoId?visitaId=$visitaId"
    fun prueba(equipoId: Long, visitaId: Long = -1) = "prueba/$equipoId?visitaId=$visitaId&pruebaId=-1"
    fun corregirPrueba(equipoId: Long, pruebaId: Long) = "prueba/$equipoId?visitaId=-1&pruebaId=$pruebaId"
    fun equipoNuevo(visitaId: Long = -1, codigo: String = "") = "equipo_form?equipoId=-1&visitaId=$visitaId&codigo=${Uri.encode(codigo)}"
    fun equipoEditar(equipoId: Long) = "equipo_form?equipoId=$equipoId&visitaId=-1&codigo="
    fun cerrar(visitaId: Long) = "cerrar/$visitaId"
    fun informe(visitaId: Long) = "informe/$visitaId"
    fun nuevaVisita(equipoId: Long = -1) = "nueva_visita?equipoId=$equipoId"
    fun cambiarEstado(equipoId: Long) = "cambiar_estado/$equipoId"
    // Uri.encode: "Relé de protección" tiene espacios y tildes, hay que codificarlo para la ruta
    fun tipoPruebaNueva(tipoEquipo: String) = "tipo_prueba?id=-1&tipoEquipo=${Uri.encode(tipoEquipo)}"
    fun tipoPruebaEditar(id: Long) = "tipo_prueba?id=$id&tipoEquipo="
}

/** -1 significa "no viene": lo convertimos a null para los ViewModels. */
fun Long.siExiste(): Long? = takeIf { it > 0 }