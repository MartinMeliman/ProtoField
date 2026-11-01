package cl.duoc.protofield.model

import java.util.Locale
import kotlin.math.abs
/**
 * REGLAS DE NEGOCIO puras (sin Android): RUT, números, criterio de aceptación.
 * Las usan varios ViewModels. Como no dependen de Android, se pueden probar
 * con pruebas unitarias (ver app/src/test/.../ReglasTest.kt).
 */
object Reglas {

    val REGEX_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")
    val REGEX_CODIGO = Regex("^[A-Z]{2}-\\d{4}$")          // ej: TR-0001
    val REGEX_HORA = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")    // ej: 09:30

    // ---------------- RUT ----------------

    /** "12.345.678-5" → "123456785" (solo números y la K). */
    fun limpiarRut(rut: String): String = rut.filter { it.isDigit() || it == 'k' || it == 'K' }.uppercase()

    /**
     * Formatea mientras se escribe: "123456785" → "12.345.678-5".
     * Máximo 9 caracteres (8 números + dígito verificador).
     */
    fun formatearRut(rut: String): String {
        val limpio = limpiarRut(rut).take(9)
        if (limpio.length < 2) return limpio
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$dv"
    }

    /** Lo que se guarda en Room: sin puntos y con guion → "12345678-5". */
    fun rutParaGuardar(rut: String): String {
        val limpio = limpiarRut(rut)
        return if (limpio.length < 2) limpio else limpio.dropLast(1) + "-" + limpio.last()
    }

    /**
     * Valida el dígito verificador con el algoritmo módulo 11:
     * se multiplican los dígitos de derecha a izquierda por 2,3,4,5,6,7,2,3...
     * se suman, y el dígito es 11 - (suma % 11). 11 → "0", 10 → "K".
     */
    fun rutValido(rut: String): Boolean {
        val limpio = limpiarRut(rut)
        if (limpio.length < 8 || limpio.length > 9) return false
        val cuerpo = limpio.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return false
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += (digito - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        val esperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + resto)
        }
        return limpio.last() == esperado
    }

    // ---------------- Números ----------------

    /** Acepta "12,5" y "12.5" (en Chile se usa coma). Devuelve null si no es número. */
    fun aNumero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    /** 1380.0 → "1380" ; 0.35 → "0,35" */
    fun formatearNumero(valor: Double): String {
        val texto = if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()
        return texto.replace('.', ',')
    }

    // ---------------- Criterio de aceptación ----------------

    /** ¿El valor cumple? min y max pueden ser null (sin límite por ese lado). */
    fun cumpleCriterio(valor: Double, minimo: Double?, maximo: Double?): Boolean {
        val sobreMinimo = minimo?.let { valor >= it } ?: true
        val bajoMaximo = maximo?.let { valor <= it } ?: true
        return sobreMinimo && bajoMaximo
    }

    /** Texto para mostrar: "mín. 1000 MΩ", "máx. 100 ms", "entre 20 y 60 ms" o "libre". */
    fun textoCriterio(minimo: Double?, maximo: Double?, unidad: String): String {
        val u = if (unidad.isBlank()) "" else " $unidad"
        return when {
            minimo != null && maximo != null -> "entre ${formatearNumero(minimo)} y ${formatearNumero(maximo)}$u"
            minimo != null -> "mín. ${formatearNumero(minimo)}$u"
            maximo != null -> "máx. ${formatearNumero(maximo)}$u"
            else -> "lo decide el técnico"
        }
    }

    // ---------------- Comparación con la medición anterior ----------------

    const val UMBRAL_ALERTA = 25.0 // % de cambio que se marca en rojo

    /** (actual - anterior) / anterior * 100. null si no hay anterior o es 0. */
    fun variacionPorcentual(actual: Double, anterior: Double?): Double? =
        anterior?.takeIf { it != 0.0 }?.let { (actual - it) / it * 100 }

    fun esAlerta(variacion: Double?): Boolean = variacion != null && abs(variacion) >= UMBRAL_ALERTA

    fun textoVariacion(variacion: Double): String {
        val signo = if (variacion >= 0) "+" else "−"
        return signo + String.format(Locale.US, "%.1f", abs(variacion)).replace('.', ',') + "%"
    }
}