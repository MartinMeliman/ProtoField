package cl.duoc.protofield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.protofield.data.local.entity.TipoPruebaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoPruebaDao {

    @Query("SELECT * FROM tipos_prueba ORDER BY tipoEquipo, id")
    fun observarTodos(): Flow<List<TipoPruebaEntity>>

    /** Las que ve el técnico al registrar una prueba: solo las activas de ese tipo. */
    @Query("SELECT * FROM tipos_prueba WHERE tipoEquipo = :tipoEquipo AND activo = 1 ORDER BY id")
    suspend fun activasPara(tipoEquipo: String): List<TipoPruebaEntity>

    @Query("SELECT * FROM tipos_prueba WHERE id = :id")
    suspend fun obtener(id: Long): TipoPruebaEntity?

    /** Para no repetir nombres en el mismo tipo de equipo (sin importar mayúsculas). */
    @Query(
        "SELECT COUNT(*) FROM tipos_prueba WHERE tipoEquipo = :tipoEquipo " +
                "AND LOWER(nombre) = LOWER(:nombre) AND id != :excluirId"
    )
    suspend fun contarConNombre(tipoEquipo: String, nombre: String, excluirId: Long): Int

    @Insert
    suspend fun insertar(tipo: TipoPruebaEntity): Long

    @Update
    suspend fun actualizar(tipo: TipoPruebaEntity)
}