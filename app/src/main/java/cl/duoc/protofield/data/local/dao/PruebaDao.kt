package cl.duoc.protofield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.protofield.data.local.entity.PruebaConEquipo
import cl.duoc.protofield.data.local.entity.PruebaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PruebaDao {

    /** Historial de un equipo, de la más nueva a la más antigua. */
    @Query("SELECT * FROM pruebas WHERE equipoId = :equipoId ORDER BY fecha DESC")
    fun observarPorEquipo(equipoId: Long): Flow<List<PruebaEntity>>

    /** Pruebas de una visita con el código del equipo (para el cierre y el informe). */
    @Query(
        """
        SELECT p.*, e.codigo AS codigoEquipo FROM pruebas p
        JOIN equipos e ON e.id = p.equipoId
        WHERE p.visitaId = :visitaId
        ORDER BY e.codigo, p.fecha
        """
    )
    fun observarPorVisita(visitaId: Long): Flow<List<PruebaConEquipo>>

    /** Última medición del mismo tipo, para comparar mientras se llena el formulario. */
    @Query("SELECT * FROM pruebas WHERE equipoId = :equipoId AND tipoPrueba = :tipoPrueba ORDER BY fecha DESC LIMIT 1")
    suspend fun ultimaPrueba(equipoId: Long, tipoPrueba: String): PruebaEntity?

    /** Nombres de las pruebas que este equipo ya tiene en esta visita. */
    @Query("SELECT tipoPrueba FROM pruebas WHERE equipoId = :equipoId AND visitaId = :visitaId")
    suspend fun tiposHechosEnVisita(equipoId: Long, visitaId: Long): List<String>

    /** La medición del mismo tipo justo ANTES de esta fecha (para comparar al corregir). */
    @Query("SELECT * FROM pruebas WHERE equipoId = :equipoId AND tipoPrueba = :tipoPrueba AND fecha < :fecha ORDER BY fecha DESC LIMIT 1")
    suspend fun anteriorA(equipoId: Long, tipoPrueba: String, fecha: Long): PruebaEntity?

    @Query("SELECT * FROM pruebas WHERE id = :id")
    suspend fun obtener(id: Long): PruebaEntity?

    @Insert
    suspend fun insertar(prueba: PruebaEntity): Long

    /** Corregir una medición mal ingresada (solo mientras la visita no esté cerrada). */
    @Update
    suspend fun actualizar(prueba: PruebaEntity)
}