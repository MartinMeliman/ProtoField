package cl.duoc.protofield.model

import cl.duoc.protofield.data.local.entity.ClienteEntity
import cl.duoc.protofield.data.local.entity.EquipoEntity
import cl.duoc.protofield.data.local.entity.InstalacionConCliente
import cl.duoc.protofield.data.local.entity.UsuarioEntity

/**
 * PANTALLA 11 · NUEVA VISITA (supervisor) — MODELO.
 * Listas dependientes: cliente → instalación → equipos.
 */
data class NuevaVisitaUiState(
    // Opciones para los selectores
    val clientes: List<ClienteEntity> = emptyList(),
    val instalaciones: List<InstalacionConCliente> = emptyList(),  // solo las del cliente elegido
    val tecnicos: List<UsuarioEntity> = emptyList(),
    val equiposInstalacion: List<EquipoEntity> = emptyList(),

    // Campos
    val clienteId: Long? = null,
    val instalacionId: Long? = null,
    val fecha: Long? = null,              // milisegundos (lo entrega el DatePicker)
    val hora: String = "09:00",
    val tecnicoId: Long? = null,
    val equiposSeleccionados: Set<Long> = emptySet(),

    val errores: NuevaVisitaErrores = NuevaVisitaErrores()
)

data class NuevaVisitaErrores(
    val cliente: String? = null,
    val instalacion: String? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val tecnico: String? = null,
    val equipos: String? = null
)

