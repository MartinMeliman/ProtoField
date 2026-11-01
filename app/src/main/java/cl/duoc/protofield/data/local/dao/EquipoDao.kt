package cl.duoc.protofield.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.protofield.data.local.entity.EquipoConUbicacion
import cl.duoc.protofield.data.local.entity.EquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {

    /** Lista de equipos con instalación, cliente y resultado de su última prueba. */
    @Query(
        """
        SELECT e.*, i.nombre AS instalacionNombre, c.razonSocial AS clienteNombre,
          (SELECT p.aprobada FROM pruebas p WHERE p.equipoId = e.id ORDER BY p.fecha DESC LIMIT 1) AS ultimaAprobada,
          (SELECT p.fecha FROM pruebas p WHERE p.equipoId = e.id ORDER BY p.fecha DESC LIMIT 1) AS ultimaFecha
        FROM equipos e
        JOIN instalaciones i ON i.id = e.instalacionId
        JOIN clientes c ON c.id = i.clienteId
        ORDER BY e.codigo
        """
    )
    fun observarTodos(): Flow<List<EquipoConUbicacion>>

    @Query(
        """
        SELECT e.*, i.nombre AS instalacionNombre, c.razonSocial AS clienteNombre,
          (SELECT p.aprobada FROM pruebas p WHERE p.equipoId = e.id ORDER BY p.fecha DESC LIMIT 1) AS ultimaAprobada,
          (SELECT p.fecha FROM pruebas p WHERE p.equipoId = e.id ORDER BY p.fecha DESC LIMIT 1) AS ultimaFecha
        FROM equipos e
        JOIN instalaciones i ON i.id = e.instalacionId
        JOIN clientes c ON c.id = i.clienteId
        WHERE e.id = :id
        """
    )
    fun observarDetalle(id: Long): Flow<EquipoConUbicacion?>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun obtener(id: Long): EquipoEntity?

    @Query("SELECT * FROM equipos WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): EquipoEntity?

    /** Equipos de una instalación; los pendientes de prueba primero. */
    @Query(
        """
        SELECT * FROM equipos WHERE instalacionId = :instalacionId
        ORDER BY CASE WHEN estado = 'Pendiente de prueba' THEN 0 ELSE 1 END, codigo
        """
    )
    fun observarPorInstalacion(instalacionId: Long): Flow<List<EquipoEntity>>

    @Query("SELECT COUNT(*) FROM pruebas WHERE equipoId = :equipoId")
    suspend fun contarPruebas(equipoId: Long): Int

    @Insert
    suspend fun insertar(equipo: EquipoEntity): Long

    @Update
    suspend fun actualizar(equipo: EquipoEntity)

    @Delete
    suspend fun eliminar(equipo: EquipoEntity)
}