package cl.duoc.protofield.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatos de fecha para mostrar en pantalla (formato chileno). */
object Formato {
    private val chile = Locale.forLanguageTag("es-CL")

    fun fechaHora(millis: Long): String = SimpleDateFormat("dd-MM-yyyy HH:mm", chile).format(
        Date(
            millis
        )
    )
    fun fecha(millis: Long): String = SimpleDateFormat("dd-MM-yyyy", chile).format(Date(millis))
    fun hora(millis: Long): String = SimpleDateFormat("HH:mm", chile).format(Date(millis))
    fun diaCorto(millis: Long): String = SimpleDateFormat("EEE d MMM", chile).format(Date(millis))
    fun mesAnio(millis: Long): String = SimpleDateFormat("MMMM yyyy", chile).format(Date(millis))
}