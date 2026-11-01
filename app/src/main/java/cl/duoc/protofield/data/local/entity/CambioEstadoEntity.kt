package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cambios_estado",
    foreignKeys = [
        ForeignKey(entity = EquipoEntity::class, parentColumns = ["id"], childColumns = ["equipoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("equipoId")]
)
data class CambioEstadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipoId: Long,
    val estadoAnterior: String,
    val estadoNuevo: String,
    val motivo: String,
    val usuario: String,
    val fecha: Long = System.currentTimeMillis()
)