package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_prueba")
data class TipoPruebaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipoEquipo: String,
    val nombre: String,
    val unidad: String,
    val minimo: Double?,
    val maximo: Double?,
    val activo: Boolean = true
)