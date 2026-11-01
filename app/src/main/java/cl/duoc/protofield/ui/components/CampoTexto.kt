package cl.duoc.protofield.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Campo de texto reutilizable para TODOS los formularios de la app.
 *
 * Por dentro es el MISMO OutlinedTextField del Ejemplo 4 del profe:
 *   value          → el valor que viene del estado del ViewModel
 *   onValueChange  → la función on...Change del ViewModel
 *   isError        → true si el error NO es null
 *   supportingText → el mensaje de error en rojo
 *
 * Lo que agregamos para cumplir la rúbrica:
 *  - Ícono a la izquierda y ícono de advertencia si hay error (IE 2.1.2).
 *  - El mensaje de error aparece con animación y el campo se "sacude" (IE 2.2.2).
 *
 * El componente NO valida nada: solo muestra el "error" que le manda el ViewModel (IE 2.2.1).
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    teclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false,
    sufijo: String? = null,
    unaLinea: Boolean = true,
    soloLectura: Boolean = false
) {
    // Animación "shake": desplaza el campo a izquierda y derecha cuando aparece un error
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(error) {
        if (error != null) {
            for (x in listOf(14f, -14f, 9f, -9f, 4f, 0f)) {
                desplazamiento.animateTo(x, animationSpec = tween(durationMillis = 45))
            }
        }
    }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(text = etiqueta) },
        leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = if (error != null) {
            { Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
        } else null,
        suffix = sufijo?.let { { Text(text = it) } },
        isError = error != null,
        supportingText = {
            // Igual que el profe: error?.let { Text(...) }, pero con animación
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            }
        },
        singleLine = unaLinea,
        readOnly = soloLectura,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
    )
}

/**
 * Mensaje de error animado para los campos que NO son de texto (los chips de selección).
 */
@Composable
fun MensajeError(error: String?) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = error.orEmpty(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}