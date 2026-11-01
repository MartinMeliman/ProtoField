package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    // Aquí se conecta la vista con el ViewModel: observa el "estado" público.
    val estado by viewModel.estado.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
            Text("ProtoField", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Protocolos de pruebas eléctricas en terreno",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            CampoTexto(
                valor = estado.rut,
                onValorChange = viewModel::onRutChange,   // al escribir → el ViewModel lo formatea
                etiqueta = "RUT",
                error = estado.errores.rut,
                icono = Icons.Filled.Badge,
                teclado = KeyboardType.Text              // Text porque el dígito puede ser K
            )
            CampoTexto(
                valor = estado.clave,
                onValorChange = viewModel::onClaveChange,
                etiqueta = "Contraseña",
                error = estado.errores.clave,
                icono = Icons.Filled.Lock,
                teclado = KeyboardType.Password,
                esClave = true
            )

            Button(
                onClick = {
                    // Si el ViewModel dice que el formato está bien, busca el RUT y navega.
                    if (viewModel.validarFormulario()) {
                        viewModel.iniciarSesion(onListo = {
                            navController.navigate(Rutas.VISITAS) {
                                popUpTo(Rutas.LOGIN) { inclusive = true } // no volver al login con "atrás"
                            }
                        })
                    }
                },
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Ingresar")
            }

            // Ayuda para la demo: usuarios FICTICIOS
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Usuarios de prueba (ficticios)", style = MaterialTheme.typography.labelLarge)
                    Text("Técnico: 12.345.678-5", style = MaterialTheme.typography.bodySmall)
                    Text("Supervisora: 15.432.876-9", style = MaterialTheme.typography.bodySmall)
                    Text("Contraseña: cualquiera de 6 o más caracteres", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}