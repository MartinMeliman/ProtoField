package cl.duoc.protofield.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Lo que se sabe del usuario que inició sesión. */
data class Sesion(
    val usuarioId: Long,
    val nombre: String,
    val rol: String
)

val Context.dataStore by preferencesDataStore(name = "preferencias_protofield")

class UserPreferences(private val context: Context) {

    // Llaves: el "nombre" con que se guarda cada valor
    private val USUARIO_ID = longPreferencesKey(name = "usuario_id")
    private val NOMBRE = stringPreferencesKey(name = "nombre")
    private val ROL = stringPreferencesKey(name = "rol")

    /** Guardar (suspend, porque escribe en disco). */
    suspend fun iniciarSesion(usuarioId: Long, nombre: String, rol: String) {
        context.dataStore.edit { preferencias ->
            preferencias[USUARIO_ID] = usuarioId
            preferencias[NOMBRE] = nombre
            preferencias[ROL] = rol
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferencias -> preferencias.clear() }
    }

    /** Obtener (Flow): null si no hay sesión. */
    fun obtenerSesion(): Flow<Sesion?> =
        context.dataStore.data.map { preferencias ->
            val id = preferencias[USUARIO_ID]
            val rol = preferencias[ROL]
            if (id == null || rol == null) null
            else Sesion(usuarioId = id, nombre = preferencias[NOMBRE].orEmpty(), rol = rol)
        }
}