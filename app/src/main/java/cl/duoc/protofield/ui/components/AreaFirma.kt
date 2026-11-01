package cl.duoc.protofield.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

/**
 * Área para que el cliente FIRME con el dedo.
 * - pointerInput + detectDragGestures detecta cuando el dedo toca y se mueve.
 * - Canvas dibuja los trazos que guarda el ViewModel.
 * La vista no guarda nada: solo avisa al ViewModel (onInicio / onMovimiento).
 */
@Composable
fun AreaFirma(
    trazos: List<List<Offset>>,
    hayError: Boolean,
    onInicio: (Offset) -> Unit,
    onMovimiento: (Offset) -> Unit,
    onTamano: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(forma)
            .background(Color.White)
            .border(
                width = if (hayError) 2.dp else 1.dp,
                color = if (hayError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                shape = forma
            )
            .onSizeChanged { onTamano(it.width, it.height) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { punto -> onInicio(punto) },
                    onDrag = { cambio, _ -> onMovimiento(cambio.position) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (trazos.isEmpty()) {
            Text("Firme aquí con el dedo", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            trazos.forEach { trazo ->
                val camino = Path().apply {
                    trazo.firstOrNull()?.let { moveTo(it.x, it.y) }
                    trazo.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    camino,
                    color = Color(0xFF14213D),
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}