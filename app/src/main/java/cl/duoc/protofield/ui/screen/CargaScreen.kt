package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.viewmodel.SesionViewModel

/**
 * Pantalla de CARGA (apoyo, no cuenta en las 14).
 * Mientras se lee el DataStore muestra un CircularProgressIndicator (animación, igual que el Ejemplo 5).
 */
@Composable
fun CargaScreen(navController: NavController, viewModel: SesionViewModel = viewModel()) {
    val haySesion by viewModel.haySesion.collectAsState()

    // Cuando "haySesion" deja de ser null, se decide a dónde ir
    LaunchedEffect(haySesion) {
        val sesion = haySesion ?: return@LaunchedEffect
        navController.navigate(if (sesion) Rutas.VISITAS else Rutas.LOGIN) {
            popUpTo(Rutas.CARGA) { inclusive = true } // con "atrás" no se vuelve a esta pantalla
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text("ProtoField", style = MaterialTheme.typography.titleMedium)
        Text("Revisando si hay una sesión guardada…", style = MaterialTheme.typography.bodySmall)
    }
}