package cl.duoc.protofield.data.device

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * ESCÁNER QR (usa la cámara del teléfono).
 * Usamos el escáner de Google (Google Code Scanner): abre su propia pantalla de cámara,
 * lee el código y devuelve el texto. Ventaja: NO necesita pedir el permiso de cámara,
 * porque la cámara la maneja Google Play Services.
 *
 * Las etiquetas de ProtoField tienen el texto: protofield://equipo/TR-0001
 */
class LectorQr(private val context: Context) {

    fun escanear(onResultado: (String?) -> Unit) {
        val opciones = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE) // solo códigos QR
            .enableAutoZoom()
            .build()

        GmsBarcodeScanning.getClient(context, opciones)
            .startScan()
            .addOnSuccessListener { codigo -> onResultado(codigo.rawValue) }
            .addOnCanceledListener { onResultado(null) }   // el usuario cerró el escáner
            .addOnFailureListener { onResultado(null) }    // falló (ej: sin Play Services)
    }
}