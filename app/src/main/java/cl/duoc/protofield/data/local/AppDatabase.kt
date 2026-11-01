package cl.duoc.protofield.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.protofield.data.local.dao.CambioEstadoDao
import cl.duoc.protofield.data.local.dao.ClienteDao
import cl.duoc.protofield.data.local.dao.EquipoDao
import cl.duoc.protofield.data.local.dao.PruebaDao
import cl.duoc.protofield.data.local.dao.TipoPruebaDao
import cl.duoc.protofield.data.local.dao.UsuarioDao
import cl.duoc.protofield.data.local.dao.VisitaDao
import cl.duoc.protofield.data.local.entity.CambioEstadoEntity
import cl.duoc.protofield.data.local.entity.ClienteEntity
import cl.duoc.protofield.data.local.entity.EquipoEntity
import cl.duoc.protofield.data.local.entity.InstalacionEntity
import cl.duoc.protofield.data.local.entity.PruebaEntity
import cl.duoc.protofield.data.local.entity.TipoPruebaEntity
import cl.duoc.protofield.data.local.entity.UsuarioEntity
import cl.duoc.protofield.data.local.entity.VisitaEntity
import cl.duoc.protofield.data.local.entity.VisitaEquipoEntity

@Database(
    entities = [
        UsuarioEntity::class,
        ClienteEntity::class,
        InstalacionEntity::class,
        EquipoEntity::class,
        VisitaEntity::class,
        VisitaEquipoEntity::class,
        PruebaEntity::class,
        TipoPruebaEntity::class,
        CambioEstadoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun equipoDao(): EquipoDao
    abstract fun visitaDao(): VisitaDao
    abstract fun pruebaDao(): PruebaDao
    abstract fun tipoPruebaDao(): TipoPruebaDao
    abstract fun cambioEstadoDao(): CambioEstadoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "protofield.db"
                )
                    // En desarrollo: si cambia la versión, borra y vuelve a crear las tablas.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}