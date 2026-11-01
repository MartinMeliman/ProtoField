package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["rut"], unique = true)]
)

data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rut: String, // sin puntos y con guion
    val nombre: String,
    val rol: String,
    val correo: String,
    val telefono: String
)