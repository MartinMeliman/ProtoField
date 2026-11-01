package cl.duoc.protofield.model

import cl.duoc.protofield.data.local.entity.InstalacionConCliente

/**
 * PANTALLA 7 · REGISTRAR / EDITAR EQUIPO — MODELO.
 * El mismo formulario sirve para crear y para editar (esEdicion).
 */
data class EquipoUiState(
    val esEdicion: Boolean = false,
    val equipoId: Long = 0,
    val visitaId: Long? = null,                 // si viene de una visita, se agrega a ella
    val instalaciones: List<InstalacionConCliente> = emptyList(),

    val codigo: String = "",
    val tipo: String = "",
    val instalacionId: Long? = null,
    val marca: String = "",
    val modelo: String = "",
    val serie: String = "",
    val ubicacion: String = "",                 // la llena el GPS (recurso nativo 1)
    val fotoPath: String? = null,               // la llena la cámara (recurso nativo 2)

    val cargandoUbicacion: Boolean = false,     // muestra el circulito mientras busca el GPS
    val aviso: String? = null,                  // permiso denegado, GPS apagado...
    val errores: EquipoErrores = EquipoErrores()
)