package cl.duoc.protofield.data.device

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Genera la imagen de la ETIQUETA QR de un equipo (librería ZXing).
 * El QR contiene "protofield://equipo/<código>". Esa etiqueta se imprime y se pega en el equipo.
 */
object GeneradorQr {

    const val PREFIJO = "protofield://equipo/"

    fun textoPara(codigoEquipo: String) = PREFIJO + codigoEquipo

    fun generar(texto: String, tamano: Int = 512): Bitmap {
        val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano)
        val imagen = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.RGB_565)
        for (x in 0 until tamano) {
            for (y in 0 until tamano) {
                imagen.setPixel(x, y, if (matriz[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return imagen
    }
}