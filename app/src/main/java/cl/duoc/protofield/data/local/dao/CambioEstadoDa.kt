package cl.duoc.protofield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.protofield.data.local.entity.CambioEstadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CambioEstadoDao {

    @Query("SELECT * FROM cambios_estado WHERE equipoId = :equipoId ORDER BY fecha DESC")
    fun observarPorEquipo(equipoId: Long): Flow<List<CambioEstadoEntity>>

    @Insert
    suspend fun insertar(cambio: CambioEstadoEntity): Long
}