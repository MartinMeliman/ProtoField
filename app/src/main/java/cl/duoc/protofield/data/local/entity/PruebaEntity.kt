package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pruebas",
    foreignKeys = [
        ForeignKey(entity = EquipoEntity::class, parentColumns = ["id"], childColumns = ["equipoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("equipoId"), Index("visitaId")]
)
data class PruebaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipoId: Long,
    val visitaId: Long? = null,
    val tipoPrueba: String,          // ej: Resistencia de aislamiento
    val valorMedido: Double,
    val unidad: String,              // ej: MΩ
    val minimoUsado: Double?,
    val maximoUsado: Double?,
    val temperaturaAmbiente: Double,
    val tecnico: String,
    val observaciones: String = "",
    val aprobada: Boolean,
    val fecha: Long = System.currentTimeMillis()
)