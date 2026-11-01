package cl.duoc.protofield.data.device

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RECURSO NATIVO 2: CÁMARA (la parte de los archivos).
 * Mismo enfoque del Ejemplo 8 del profe (crear archivo + Uri con FileProvider):
 *  1) Se crea un archivo vacío donde la cámara va a guardar la foto.
 *  2) Se obtiene una Uri segura con FileProvider para pasársela a la cámara.
 *
 * Diferencia: guardamos en filesDir (almacenamiento INTERNO permanente),
 * porque la foto de la placa es evidencia. Ninguna otra app puede leerla.
 * En Room solo se guarda la RUTA del archivo.
 *
 * También guarda la FIRMA del cliente como imagen PNG.
 */
class FotoStorage(private val context: Context) {

    private fun marcaDeTiempo() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    /** Crea el archivo donde la cámara guardará la foto. */
    fun crearArchivoFoto(): File {
        val carpeta = File(context.filesDir, "fotos").apply { if (!exists()) mkdirs() }
        return File(carpeta, "PLACA_${marcaDeTiempo()}.jpg")
    }

    /** Uri segura para compartir el archivo con la app de cámara (configurada en el Manifest). */
    fun obtenerUri(archivo: File): Uri {
        val autoridad = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, autoridad, archivo)
    }

    /** Guarda la firma dibujada en pantalla y devuelve la ruta del archivo. */
    fun guardarFirma(imagen: Bitmap, numeroVisita: String): String {
        val carpeta = File(context.filesDir, "firmas").apply { if (!exists()) mkdirs() }
        val archivo = File(carpeta, "FIRMA_${numeroVisita}_${marcaDeTiempo()}.png")
        FileOutputStream(archivo).use { salida -> imagen.compress(Bitmap.CompressFormat.PNG, 100, salida) }
        return archivo.absolutePath
    }
}