package cl.duoc.protofield.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.Tendencia

/**
 * Gráfico de TENDENCIA (mantenimiento predictivo). Solo DIBUJA: los cálculos
 * (recta, fecha de cruce) vienen hechos desde el ViewModel en el objeto Tendencia.
 *  - línea azul: mediciones reales
 *  - línea roja punteada: el límite del criterio
 *  - línea azul punteada: proyección hasta que saldría del criterio
 */
@Composable
fun GraficoTendencia(tendencia: Tendencia, modifier: Modifier = Modifier) {
    val azul = MaterialTheme.colorScheme.primary
    val rojo = MaterialTheme.colorScheme.error
    val grilla = MaterialTheme.colorScheme.outlineVariant

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LineaIcono(Icons.Filled.Timeline, "Tendencia · ${tendencia.nombrePrueba}")
            Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                val puntos = tendencia.puntos
                val x0 = puntos.first().fecha.toFloat()
                val x1 = maxOf(puntos.last().fecha, tendencia.fechaCruce ?: 0L).toFloat()
                val valores = puntos.map { it.valor } + tendencia.limite
                var y0 = valores.min().toFloat()
                var y1 = valores.max().toFloat()
                val margen = ((y1 - y0) * 0.15f).coerceAtLeast(1f)
                y0 -= margen; y1 += margen
                fun x(f: Long) = if (x1 == x0) 0f else (f - x0) / (x1 - x0) * size.width
                fun y(v: Double) = size.height - ((v.toFloat() - y0) / (y1 - y0)) * size.height

                // grilla
                for (i in 0..2) {
                    val yy = size.height * i / 2f
                    drawLine(grilla, Offset(0f, yy), Offset(size.width, yy), strokeWidth = 1f)
                }
                // límite del criterio
                drawLine(
                    rojo, Offset(0f, y(tendencia.limite)), Offset(size.width, y(tendencia.limite)),
                    strokeWidth = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                )
                // proyección
                tendencia.fechaCruce?.let { cruce ->
                    val ultimo = puntos.last()
                    drawLine(
                        azul, Offset(x(ultimo.fecha), y(ultimo.valor)), Offset(x(cruce), y(tendencia.limite)),
                        strokeWidth = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f))
                    )
                    drawCircle(Color.White, radius = 9f, center = Offset(x(cruce), y(tendencia.limite)))
                    drawCircle(rojo, radius = 9f, center = Offset(x(cruce), y(tendencia.limite)), style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
                }
                // mediciones reales
                for (i in 0 until puntos.size - 1) {
                    drawLine(azul, Offset(x(puntos[i].fecha), y(puntos[i].valor)), Offset(x(puntos[i + 1].fecha), y(puntos[i + 1].valor)), strokeWidth = 5f)
                }
                puntos.forEach { drawCircle(azul, radius = 8f, center = Offset(x(it.fecha), y(it.valor))) }
            }
            val unidad = tendencia.unidad.let { if (it.isBlank()) "" else " $it" }
            Text(
                text = when {
                    tendencia.yaFuera -> "La última medición ya está fuera del criterio."
                    tendencia.fechaCruce != null ->
                        "Si sigue ${if (tendencia.esMinimo) "bajando" else "subiendo"} a este ritmo, saldría del criterio " +
                                "(${if (tendencia.esMinimo) "mín." else "máx."} ${Reglas.formatearNumero(tendencia.limite)}$unidad) " +
                                "cerca de ${Formato.mesAnio(tendencia.fechaCruce)}. Conviene probarlo antes."
                    else -> "Tendencia estable: no se acerca al límite."
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (tendencia.yaFuera) FontWeight.Bold else FontWeight.Normal,
                color = if (tendencia.yaFuera) rojo else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}