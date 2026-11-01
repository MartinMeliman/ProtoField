package cl.duoc.protofield.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


/** Título con subtítulo para la barra superior (se usa en varias pantallas). */
@Composable
fun TituloBarra(titulo: String, subtitulo: String? = null) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        if (!subtitulo.isNullOrBlank()) {
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

