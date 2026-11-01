package cl.duoc.protofield.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.protofield.data.local.entity.VisitaResumen
import cl.duoc.protofield.model.EstadosVisita


/** Tarjeta de una visita. La barra de avance se ANIMA cuando cambia (animateFloatAsState). */
@Composable
fun TarjetaVisita(
    visita: VisitaResumen,
    mostrarTecnico: Boolean,
    mostrarDia: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avance = if (visita.totalEquipos == 0) 0f else visita.equiposProbados.toFloat() / visita.totalEquipos
    val avanceAnimado by animateFloatAsState(targetValue = avance, animationSpec = tween(600), label = "avance")

    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (mostrarDia) "${Formato.diaCorto(visita.fechaHora)} · ${Formato.hora(visita.fechaHora)}" else Formato.hora(visita.fechaHora),
                    style = MaterialTheme.typography.titleSmall
                )
                ChipEstadoVisita(visita.estado)
            }
            Text(visita.clienteNombre, style = MaterialTheme.typography.titleMedium)
            LineaIcono(Icons.Filled.Place, visita.instalacionNombre)
            if (mostrarTecnico) LineaIcono(Icons.Filled.Person, visita.tecnicoNombre)
            if (visita.estado == EstadosVisita.CERRADA) {
                LineaIcono(Icons.Filled.Description, "${visita.numero} · entregado ${visita.cerradaEn?.let { Formato.fechaHora(it) }.orEmpty()}")
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(progress = { avanceAnimado }, modifier = Modifier.weight(1f))
                    Text("${visita.equiposProbados} de ${visita.totalEquipos} equipos", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun LineaIcono(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}