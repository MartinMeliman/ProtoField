package cl.duoc.protofield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.protofield.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao{
    /** Lo usa el login: busca al empleado por su RUT (sin puntos, con guion).*/
    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun buscarPorRut(rut: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rol= 'TECNICO' ORDER BY nombre")
    fun observarTecnicos(): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long
}