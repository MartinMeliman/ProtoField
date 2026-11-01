package cl.duoc.protofield.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MenuAnchorType

/**
 * Lista desplegable de Material 3 (ExposedDropdownMenuBox) con ícono y error,
 * igual que los campos de texto. Sirve para cliente, instalación, técnico...
 * "expandido" es estado de la VISTA (solo si el menú está abierto), por eso va con remember.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorLista(
    etiqueta: String,
    opciones: List<T>,
    seleccionado: T?,
    textoDe: (T) -> String,
    onSeleccion: (T) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }
    Column(modifier) {
        ExposedDropdownMenuBox(
            expanded = expandido && habilitado,
            onExpandedChange = { if (habilitado) expandido = it }
        ) {
            OutlinedTextField(
                value = seleccionado?.let(textoDe).orEmpty(),
                onValueChange = {},
                readOnly = true,
                enabled = habilitado,
                label = { Text(etiqueta) },
                leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
                trailingIcon = {
                    if (error != null) Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                },
                isError = error != null,
                singleLine = true,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = habilitado)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandido && habilitado, onDismissRequest = { expandido = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(textoDe(opcion)) },
                        onClick = {
                            onSeleccion(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
        MensajeError(error)
    }
}