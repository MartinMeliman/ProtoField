package cl.duoc.protofield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.protofield.data.local.entity.EquipoEnVisita
import cl.duoc.protofield.data.local.entity.VisitaDetalle
import cl.duoc.protofield.data.local.entity.VisitaEntity
import cl.duoc.protofield.data.local.entity.VisitaEquipoEntity
import cl.duoc.protofield.data.local.entity.VisitaResumen
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaDao {

    /**
     * Todas las visitas con los nombres (JOIN de 4 tablas) y el avance:
     * cuántos equipos tiene y cuántos ya tienen al menos una prueba en esa visita.
     */
    @Query(
        """
        SELECT v.id, v.numero, v.fechaHora, v.estado, v.tecnicoId, v.cerradaEn,
          u.nombre AS tecnicoNombre, i.nombre AS instalacionNombre, c.razonSocial AS clienteNombre,
          (SELECT COUNT(*) FROM visita_equipo ve WHERE ve.visitaId = v.id) AS totalEquipos,
          (SELECT COUNT(DISTINCT p.equipoId) FROM pruebas p
             JOIN visita_equipo ve2 ON ve2.equipoId = p.equipoId AND ve2.visitaId = v.id
             WHERE p.visitaId = v.id) AS equiposProbados
        FROM visitas v
        JOIN usuarios u ON u.id = v.tecnicoId
        JOIN instalaciones i ON i.id = v.instalacionId
        JOIN clientes c ON c.id = i.clienteId
        ORDER BY v.fechaHora
        """
    )
    fun observarResumenes(): Flow<List<VisitaResumen>>

    @Query(
        """
        SELECT v.*, c.razonSocial AS clienteNombre, c.rut AS clienteRut, c.correo AS clienteCorreo,
          i.nombre AS instalacionNombre, i.comuna AS comuna, u.nombre AS tecnicoNombre
        FROM visitas v
        JOIN instalaciones i ON i.id = v.instalacionId
        JOIN clientes c ON c.id = i.clienteId
        JOIN usuarios u ON u.id = v.tecnicoId
        WHERE v.id = :id
        """
    )
    fun observarDetalle(id: Long): Flow<VisitaDetalle?>

    @Query("SELECT * FROM visitas WHERE id = :id")
    suspend fun obtener(id: Long): VisitaEntity?

    /** Ids de las visitas que todavía no se cierran: sus pruebas se pueden corregir. */
    @Query("SELECT id FROM visitas WHERE estado != 'Cerrada'")
    fun observarAbiertas(): Flow<List<Long>>

    /** Equipos de la visita con cuántas pruebas (y rechazadas) llevan en ESA visita. */
    @Query(
        """
        SELECT e.*,
          (SELECT COUNT(*) FROM pruebas p WHERE p.equipoId = e.id AND p.visitaId = :visitaId) AS pruebas,
          (SELECT COUNT(*) FROM pruebas p WHERE p.equipoId = e.id AND p.visitaId = :visitaId AND p.aprobada = 0) AS rechazadas
        FROM equipos e JOIN visita_equipo ve ON ve.equipoId = e.id
        WHERE ve.visitaId = :visitaId
        ORDER BY e.codigo
        """
    )
    fun observarEquipos(visitaId: Long): Flow<List<EquipoEnVisita>>

    @Query("SELECT MAX(id) FROM visitas")
    suspend fun ultimoId(): Long?

    @Insert
    suspend fun insertar(visita: VisitaEntity): Long

    @Update
    suspend fun actualizar(visita: VisitaEntity)

    /** IGNORE: si el equipo ya estaba en la visita, no hace nada (no se duplica). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarEquipo(relacion: VisitaEquipoEntity)
}