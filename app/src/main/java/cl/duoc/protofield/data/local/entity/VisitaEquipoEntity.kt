package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "visita_equipo",
    primaryKeys = ["visitaId", "equipoId"],
    foreignKeys = [
        ForeignKey(entity = VisitaEntity::class, parentColumns = ["id"], childColumns = ["visitaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EquipoEntity::class, parentColumns = ["id"], childColumns = ["equipoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("equipoId")]
)
data class VisitaEquipoEntity(
    val visitaId: Long,
    val equipoId: Long
)