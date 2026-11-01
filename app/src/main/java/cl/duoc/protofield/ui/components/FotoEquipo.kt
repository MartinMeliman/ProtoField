package cl.duoc.protofield.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Muestra una imagen guardada en el almacenamiento interno (foto de la placa o firma).
 * Usa Coil (AsyncImage), la misma librería del Ejemplo 8: carga la imagen
 * en segundo plano y la deja en caché, así la pantalla no se pega.
 */
@Composable
fun FotoEquipo(
    ruta: String,
    modifier: Modifier = Modifier,
    descripcion: String = "Foto de la placa del equipo",
    escala: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(File(ruta))
            .crossfade(true) // aparece con una transición suave
            .build(),
        contentDescription = descripcion,
        contentScale = escala,
        modifier = modifier // va primero: si quien lo usa pone otra altura (ej: la firma), esa gana
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}