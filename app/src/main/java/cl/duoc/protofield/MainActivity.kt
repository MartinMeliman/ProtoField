package cl.duoc.protofield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.protofield.navigation.AppNavigation
import cl.duoc.protofield.ui.theme.ProtoFieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProtoFieldTheme {
                AppNavigation()
            }
        }
    }
}