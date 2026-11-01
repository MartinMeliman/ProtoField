package cl.duoc.protofield.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AzulElectrico80,
    secondary = GrisAcero80,
    tertiary = Ambar80
)

private val LightColorScheme = lightColorScheme(
    primary = AzulElectrico40,
    secondary = GrisAcero40,
    tertiary = Ambar40
)

@Composable
fun ProtoFieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}