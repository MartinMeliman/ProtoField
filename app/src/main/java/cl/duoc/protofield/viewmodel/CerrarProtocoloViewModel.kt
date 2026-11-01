package cl.duoc.protofield.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.device.FotoStorage
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.CierreErrores
import cl.duoc.protofield.model.CierreUiState
import cl.duoc.protofield.model.EstadosVisita
import cl.duoc.protofield.model.Reglas
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CerrarProtocoloViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val fotoStorage = FotoStorage(application)

    private val _estado = MutableStateFlow(value = CierreUiState())
    val estado: StateFlow<CierreUiState> = _estado

    private var trabajo: Job? = null

    fun cargar(visitaId: Long) {
        if (trabajo != null) return
        trabajo = viewModelScope.launch {
            combine(
                repositorio.observarVisita(visitaId),
                repositorio.observarEquiposDeVisita(visitaId),
                repositorio.observarPruebasDeVisita(visitaId)
            ) { detalle, equipos, pruebas -> Triple(detalle, equipos, pruebas) }
                .collect { (detalle, equipos, pruebas) ->
                    _estado.update {
                        it.copy(
                            detalle = detalle,
                            totalEquipos = equipos.size,
                            pruebas = pruebas,
                            // el correo del cliente viene precargado (se puede cambiar)
                            correoCliente = it.correoCliente.ifBlank { detalle?.clienteCorreo.orEmpty() }
                        )
                    }
                }
        }
    }

    fun onObservacionChange(valor: String) {
        _estado.update { it.copy(observacion = valor, errores = it.errores.copy(observacion = null)) }
    }

    fun onRecibidoPorChange(valor: String) {
        _estado.update { it.copy(recibidoPor = valor, errores = it.errores.copy(recibidoPor = null)) }
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correoCliente = valor.trim(), errores = it.errores.copy(correoCliente = null)) }
    }

    // ---------------- Firma ----------------

    fun onTamanoFirma(ancho: Int, alto: Int) {
        _estado.update { it.copy(anchoFirma = ancho, altoFirma = alto) }
    }

    /** Empieza un trazo nuevo (el dedo toca la pantalla). */
    fun onFirmaInicio(punto: Offset) {
        _estado.update { it.copy(trazos = it.trazos + listOf(listOf(punto)), errores = it.errores.copy(firma = null)) }
    }

    /** El dedo se mueve: agrega el punto al último trazo. */
    fun onFirmaMovimiento(punto: Offset) {
        _estado.update {
            if (it.trazos.isEmpty()) it
            else it.copy(trazos = it.trazos.dropLast(1) + listOf(it.trazos.last() + punto))
        }
    }

    fun borrarFirma() {
        _estado.update { it.copy(trazos = emptyList()) }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val errores = CierreErrores(
            observacion = if (actual.observacion.trim().length < 10) "Escribe un resumen para el cliente (mín. 10 caracteres)" else null,
            recibidoPor = if (actual.recibidoPor.trim().length < 3) "Ingresa el nombre de quien recibe" else null,
            correoCliente = when {
                actual.correoCliente.isBlank() -> "Ingresa el correo para enviar el protocolo"
                !Reglas.REGEX_CORREO.matches(actual.correoCliente) -> "Correo inválido (ej: contacto@empresa.cl)"
                else -> null
            },
            firma = if (actual.trazos.isEmpty()) "Falta la firma de quien recibe" else null
        )
        val hayErrores = listOfNotNull(errores.observacion, errores.recibidoPor, errores.correoCliente, errores.firma).isNotEmpty()
        _estado.update { it.copy(errores = errores) }
        return !hayErrores
    }

    fun cerrar(onCerrado: () -> Unit) {
        val actual = _estado.value
        val visita = actual.detalle?.visita ?: return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val rutaFirma = fotoStorage.guardarFirma(dibujarFirma(actual), visita.numero)
            repositorio.actualizarVisita(
                visita.copy(
                    estado = EstadosVisita.CERRADA,
                    observacion = actual.observacion.trim(),
                    recibidoPor = actual.recibidoPor.trim(),
                    correoCliente = actual.correoCliente,
                    firmaPath = rutaFirma,
                    cerradaEn = System.currentTimeMillis()
                )
            )
            onCerrado()
        }
    }

    /** Convierte los trazos en una imagen (fondo blanco, línea azul oscuro). */
    private fun dibujarFirma(estado: CierreUiState): Bitmap {
        val ancho = estado.anchoFirma.coerceAtLeast(1)
        val alto = estado.altoFirma.coerceAtLeast(1)
        val imagen = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val lienzo = Canvas(imagen)
        lienzo.drawColor(Color.WHITE)
        val pincel = Paint().apply {
            color = Color.rgb(20, 33, 61)
            strokeWidth = 6f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        estado.trazos.forEach { trazo ->
            val camino = Path()
            trazo.firstOrNull()?.let { camino.moveTo(it.x, it.y) }
            trazo.drop(1).forEach { camino.lineTo(it.x, it.y) }
            lienzo.drawPath(camino, pincel)
        }
        return imagen
    }
}