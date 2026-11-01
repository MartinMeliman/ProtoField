package cl.duoc.protofield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.protofield.data.local.entity.ClienteEntity
import cl.duoc.protofield.data.local.entity.InstalacionConCliente
import cl.duoc.protofield.data.local.entity.InstalacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes ORDER BY razonSocial")
    fun observarClientes(): Flow<List<ClienteEntity>>

    /** Todas las instalaciones con el nombre de su cliente (JOIN). */
    @Query(
        """
        SELECT i.id, i.nombre, i.comuna, i.clienteId, c.razonSocial AS clienteNombre
        FROM instalaciones i JOIN clientes c ON c.id = i.clienteId
        ORDER BY c.razonSocial, i.nombre
        """
    )
    fun observarInstalaciones(): Flow<List<InstalacionConCliente>>

    @Query("SELECT * FROM instalaciones WHERE id = :id")
    suspend fun obtenerInstalacion(id: Long): InstalacionEntity?

    @Insert
    suspend fun insertarCliente(cliente: ClienteEntity): Long

    @Insert
    suspend fun insertarInstalacion(instalacion: InstalacionEntity): Long
}