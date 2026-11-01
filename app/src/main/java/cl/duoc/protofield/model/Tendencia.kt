package cl.duoc.protofield.model

/**
 * TENDENCIA (mantenimiento predictivo): con las mediciones de un equipo se calcula
 * una recta (regresión lineal) y se proyecta cuándo saldría del criterio.
 * Es lógica pura: la calcula el ViewModel y la pantalla solo la dibuja.
 */
data class PuntoMedicion(val fecha: Long, val valor: Double)

data class Tendencia(
    val nombrePrueba: String,
    val unidad: String,
    val puntos: List<PuntoMedicion>,   // de la más antigua a la más nueva
    val limite: Double,                // el mínimo o el máximo del criterio
    val esMinimo: Boolean,             // true = el valor no debe bajar del límite
    val fechaCruce: Long?,             // cuándo saldría del criterio (null = no se acerca)
    val yaFuera: Boolean               // la última medición ya no cumple
) {
    companion object {
        private const val DIA = 24L * 60 * 60 * 1000
        private const val MAX_PROYECCION = 4 * 365 * DIA // no proyectar más de 4 años

        /**
         * @param puntos mediciones de UNA misma prueba, de la más antigua a la más nueva.
         * Devuelve null si hay menos de 2 puntos o la prueba no tiene límite.
         */
        fun calcular(
            nombrePrueba: String,
            unidad: String,
            puntos: List<PuntoMedicion>,
            minimo: Double?,
            maximo: Double?
        ): Tendencia? {
            if (puntos.size < 2) return null
            val limite = minimo ?: maximo ?: return null
            val esMinimo = minimo != null

            // Regresión lineal: pendiente = Σ(x-x̄)(y-ȳ) / Σ(x-x̄)²   (x en días)
            val xs = puntos.map { it.fecha.toDouble() / DIA }
            val ys = puntos.map { it.valor }
            val mx = xs.average()
            val my = ys.average()
            var sxy = 0.0
            var sxx = 0.0
            for (i in xs.indices) {
                sxy += (xs[i] - mx) * (ys[i] - my)
                sxx += (xs[i] - mx) * (xs[i] - mx)
            }
            val pendientePorDia = if (sxx == 0.0) 0.0 else sxy / sxx

            val ultimo = puntos.last()
            val yaFuera = !Reglas.cumpleCriterio(ultimo.valor, minimo, if (esMinimo) null else maximo)
            val vaHaciaElLimite = (esMinimo && pendientePorDia < 0) || (!esMinimo && pendientePorDia > 0)

            // Desde la última medición, ¿cuántos días faltan para llegar al límite?
            val fechaCruce = if (!yaFuera && vaHaciaElLimite) {
                val dias = (limite - ultimo.valor) / pendientePorDia
                (ultimo.fecha + (dias * DIA).toLong()).takeIf { it - ultimo.fecha <= MAX_PROYECCION }
            } else null

            return Tendencia(nombrePrueba, unidad, puntos, limite, esMinimo, fechaCruce, yaFuera)
        }
    }
}