package cl.duoc.protofield.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "equipos",
    foreignKeys = [
        ForeignKey(
            entity = InstalacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["instalacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["codigo"], unique = true), Index("instalacionId")]
)
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val tipo: String,
    val instalacionId: Long,
    val estado: String,
    val marca: String = "",
    val modelo: String = "",
    val serie: String,
    val ubicacion: String? = null,   // recurso nativo 1: GPS
    val fotoPath: String? = null,    // recurso nativo 2: cámara (solo la ruta del archivo)
    val fechaRegistro: Long = System.currentTimeMillis()
)