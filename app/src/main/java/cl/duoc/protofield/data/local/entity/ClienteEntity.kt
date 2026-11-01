package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "clientes")

data class ClienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val razonSocial: String,
    val rut: String,
    val correo: String,
    val telefono: String
)