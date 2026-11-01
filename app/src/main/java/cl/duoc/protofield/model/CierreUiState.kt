package cl.duoc.protofield.model

import androidx.compose.ui.geometry.Offset
import cl.duoc.protofield.data.local.entity.PruebaConEquipo
import cl.duoc.protofield.data.local.entity.VisitaDetalle

data class CierreUiState(
    val detalle: VisitaDetalle? = null,
    val totalEquipos: Int = 0,
    val pruebas: List<PruebaConEquipo> = emptyList(),

    val observacion: String = "",
    val recibidoPor: String = "",
    val correoCliente: String = "",
    val trazos: List<List<Offset>> = emptyList(),  // la firma: cada trazo es una lista de puntos
    val anchoFirma: Int = 0,
    val altoFirma: Int = 0,

    val guardando: Boolean = false,
    val errores: CierreErrores = CierreErrores()
) {
    val rechazadas: List<PruebaConEquipo> get() = pruebas.filter { !it.prueba.aprobada }
}

data class CierreErrores(
    val observacion: String? = null,
    val recibidoPor: String? = null,
    val correoCliente: String? = null,
    val firma: String? = null
)