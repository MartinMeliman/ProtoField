package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visitas",
    foreignKeys = [
        ForeignKey(entity = InstalacionEntity::class, parentColumns = ["id"], childColumns = ["instalacionId"]),
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"], childColumns = ["tecnicoId"])
    ],
    indices = [Index(value = ["numero"], unique = true), Index("instalacionId"), Index("tecnicoId")]
)
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numero: String,              // PT-2026-0142
    val instalacionId: Long,
    val tecnicoId: Long,
    val fechaHora: Long,
    val estado: String,              // EstadosVisita
    // Condiciones de la visita (se anotan una sola vez al llegar)
    val temperatura: Double? = null,
    val humedad: Double? = null,
    val instrumentos: String = "",   // ids separados por coma: "MG-5,AR-2"
    // Datos del cierre (protocolo)
    val observacion: String = "",
    val recibidoPor: String = "",
    val correoCliente: String = "",
    val firmaPath: String? = null,   // imagen PNG de la firma del cliente
    val cerradaEn: Long? = null
)