package cl.duoc.protofield.data.local.entity

import androidx.room.Embedded

/** Tarjeta de una visita: con nombre del cliente, instalación, técnico y avance. */
data class VisitaResumen(
    val id: Long,
    val numero: String,
    val fechaHora: Long,
    val estado: String,
    val tecnicoId: Long,
    val cerradaEn: Long?,
    val tecnicoNombre: String,
    val instalacionNombre: String,
    val clienteNombre: String,
    val totalEquipos: Int,
    val equiposProbados: Int
)

/** Encabezado de una visita / protocolo. */
data class VisitaDetalle(
    @Embedded val visita: VisitaEntity,
    val clienteNombre: String,
    val clienteRut: String,
    val clienteCorreo: String,
    val instalacionNombre: String,
    val comuna: String,
    val tecnicoNombre: String
)

/** Un equipo dentro de una visita + cuántas pruebas lleva en ESA visita. */
data class EquipoEnVisita(
    @Embedded val equipo: EquipoEntity,
    val pruebas: Int,
    val rechazadas: Int
)

/** Equipo con el nombre de su instalación y cliente (para listas y detalle). */
data class EquipoConUbicacion(
    @Embedded val equipo: EquipoEntity,
    val instalacionNombre: String,
    val clienteNombre: String,
    val ultimaAprobada: Boolean?,
    val ultimaFecha: Long?
)

/** Para los selectores de instalación. */
data class InstalacionConCliente(
    val id: Long,
    val nombre: String,
    val comuna: String,
    val clienteId: Long,
    val clienteNombre: String
)

/** Prueba con el código del equipo (para el informe). */
data class PruebaConEquipo(
    @Embedded val prueba: PruebaEntity,
    val codigoEquipo: String
)