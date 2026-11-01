package cl.duoc.protofield.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * RECURSO NATIVO 1: GPS.
 * Obtiene la ubicación actual del teléfono para registrar dónde está la instalación del cliente.
 *
 * Seguridad: antes de usar el GPS revisa que el permiso esté concedido.
 * Si no lo está, devuelve null en vez de caerse.
 */
class UbicacionProvider(private val context: Context) {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // el permiso se revisa en tienePermiso()
    suspend fun obtenerActual(): Location? {
        if (!tienePermiso()) return null
        return suspendCancellableCoroutine { continuacion ->
            val cancelacion = CancellationTokenSource()
            cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelacion.token)
                .addOnSuccessListener { ubicacion -> continuacion.resume(ubicacion) }
                .addOnFailureListener { continuacion.resume(null) }
            continuacion.invokeOnCancellation { cancelacion.cancel() }
        }
    }
}