package cl.duoc.protofield.data.repository

import android.content.Context
import androidx.room.withTransaction
import cl.duoc.protofield.data.local.AppDatabase
import cl.duoc.protofield.data.local.entity.CambioEstadoEntity
import cl.duoc.protofield.data.local.entity.ClienteEntity
import cl.duoc.protofield.data.local.entity.EquipoConUbicacion
import cl.duoc.protofield.data.local.entity.EquipoEnVisita
import cl.duoc.protofield.data.local.entity.EquipoEntity
import cl.duoc.protofield.data.local.entity.InstalacionConCliente
import cl.duoc.protofield.data.local.entity.InstalacionEntity
import cl.duoc.protofield.data.local.entity.PruebaConEquipo
import cl.duoc.protofield.data.local.entity.PruebaEntity
import cl.duoc.protofield.data.local.entity.TipoPruebaEntity
import cl.duoc.protofield.data.local.entity.UsuarioEntity
import cl.duoc.protofield.data.local.entity.VisitaDetalle
import cl.duoc.protofield.data.local.entity.VisitaEntity
import cl.duoc.protofield.data.local.entity.VisitaEquipoEntity
import cl.duoc.protofield.data.local.entity.VisitaResumen
import cl.duoc.protofield.model.EstadosEquipo
import cl.duoc.protofield.model.EstadosVisita
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.Roles
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ProtocoloRepository(private val db: AppDatabase) {

    private val usuarioDao = db.usuarioDao()
    private val clienteDao = db.clienteDao()
    private val equipoDao = db.equipoDao()
    private val visitaDao = db.visitaDao()
    private val pruebaDao = db.pruebaDao()
    private val tipoPruebaDao = db.tipoPruebaDao()
    private val cambioEstadoDao = db.cambioEstadoDao()

    // ---------------- Usuarios ----------------
    suspend fun buscarUsuarioPorRut(rut: String): UsuarioEntity? = usuarioDao.buscarPorRut(rut)
    fun observarTecnicos(): Flow<List<UsuarioEntity>> = usuarioDao.observarTecnicos()

    // ---------------- Clientes e instalaciones ----------------
    fun observarClientes(): Flow<List<ClienteEntity>> = clienteDao.observarClientes()
    fun observarInstalaciones(): Flow<List<InstalacionConCliente>> = clienteDao.observarInstalaciones()
    suspend fun obtenerInstalacion(id: Long): InstalacionEntity? = clienteDao.obtenerInstalacion(id)

    // ---------------- Equipos ----------------
    fun observarEquipos(): Flow<List<EquipoConUbicacion>> = equipoDao.observarTodos()
    fun observarEquipo(id: Long): Flow<EquipoConUbicacion?> = equipoDao.observarDetalle(id)
    fun observarEquiposDeInstalacion(instalacionId: Long): Flow<List<EquipoEntity>> =
        equipoDao.observarPorInstalacion(instalacionId)

    suspend fun obtenerEquipo(id: Long): EquipoEntity? = equipoDao.obtener(id)
    suspend fun buscarEquipoPorCodigo(codigo: String): EquipoEntity? = equipoDao.buscarPorCodigo(codigo)
    suspend fun existeCodigo(codigo: String): Boolean = equipoDao.buscarPorCodigo(codigo) != null
    suspend fun contarPruebasDeEquipo(equipoId: Long): Int = equipoDao.contarPruebas(equipoId)

    /**
     * Registra un equipo nuevo: lo guarda como "Pendiente de prueba", anota el primer
     * estado en el historial y, si viene de una visita, lo agrega a esa visita.
     * withTransaction = o se hace todo, o no se hace nada.
     */
    suspend fun registrarEquipo(equipo: EquipoEntity, usuario: String, visitaId: Long?): Long =
        db.withTransaction {
            val id = equipoDao.insertar(equipo)
            cambioEstadoDao.insertar(
                CambioEstadoEntity(
                    equipoId = id, estadoAnterior = "—", estadoNuevo = equipo.estado,
                    motivo = "Registro del equipo.", usuario = usuario
                )
            )
            if (visitaId != null) visitaDao.agregarEquipo(VisitaEquipoEntity(visitaId, id))
            id
        }

    suspend fun actualizarEquipo(equipo: EquipoEntity) = equipoDao.actualizar(equipo)

    /** Regla: solo se elimina si NO tiene pruebas (si tiene, se pierde la trazabilidad). */
    suspend fun eliminarEquipo(equipo: EquipoEntity): Boolean {
        if (equipoDao.contarPruebas(equipo.id) > 0) return false
        equipoDao.eliminar(equipo)
        return true
    }

    /** Cambia el estado y deja el registro en el historial (en una sola transacción). */
    suspend fun cambiarEstado(equipo: EquipoEntity, nuevoEstado: String, motivo: String, usuario: String) {
        db.withTransaction {
            cambioEstadoDao.insertar(
                CambioEstadoEntity(
                    equipoId = equipo.id, estadoAnterior = equipo.estado, estadoNuevo = nuevoEstado,
                    motivo = motivo, usuario = usuario
                )
            )
            equipoDao.actualizar(equipo.copy(estado = nuevoEstado))
        }
    }

    fun observarCambiosDeEstado(equipoId: Long): Flow<List<CambioEstadoEntity>> =
        cambioEstadoDao.observarPorEquipo(equipoId)

    // ---------------- Visitas ----------------
    fun observarVisitas(): Flow<List<VisitaResumen>> = visitaDao.observarResumenes()
    fun observarVisita(id: Long): Flow<VisitaDetalle?> = visitaDao.observarDetalle(id)
    fun observarEquiposDeVisita(visitaId: Long): Flow<List<EquipoEnVisita>> = visitaDao.observarEquipos(visitaId)
    suspend fun obtenerVisita(id: Long): VisitaEntity? = visitaDao.obtener(id)
    suspend fun actualizarVisita(visita: VisitaEntity) = visitaDao.actualizar(visita)

    /** Crea la visita y le agrega sus equipos. El número sale como PT-2026-0145. */
    suspend fun programarVisita(visita: VisitaEntity, equipos: List<Long>): Long =
        db.withTransaction {
            val siguiente = (visitaDao.ultimoId() ?: 0L) + 1
            val anio = Calendar.getInstance().get(Calendar.YEAR)
            val numero = "PT-$anio-" + (140 + siguiente).toString().padStart(4, '0')
            val id = visitaDao.insertar(visita.copy(numero = numero))
            equipos.forEach { visitaDao.agregarEquipo(VisitaEquipoEntity(id, it)) }
            id
        }

    /** Al registrar la primera prueba o las condiciones, la visita pasa a "En curso". */
    suspend fun marcarVisitaEnCurso(visitaId: Long) {
        val visita = visitaDao.obtener(visitaId) ?: return
        if (visita.estado == EstadosVisita.PROGRAMADA) visitaDao.actualizar(visita.copy(estado = EstadosVisita.EN_CURSO))
    }

    // ---------------- Pruebas ----------------
    fun observarPruebas(equipoId: Long): Flow<List<PruebaEntity>> = pruebaDao.observarPorEquipo(equipoId)
    fun observarPruebasDeVisita(visitaId: Long): Flow<List<PruebaConEquipo>> = pruebaDao.observarPorVisita(visitaId)
    suspend fun ultimaPrueba(equipoId: Long, tipoPrueba: String): PruebaEntity? = pruebaDao.ultimaPrueba(equipoId, tipoPrueba)
    suspend fun tiposHechosEnVisita(equipoId: Long, visitaId: Long): List<String> = pruebaDao.tiposHechosEnVisita(equipoId, visitaId)

    suspend fun guardarPrueba(prueba: PruebaEntity): Long {
        val id = pruebaDao.insertar(prueba)
        prueba.visitaId?.let { marcarVisitaEnCurso(it) }
        return id
    }

    suspend fun obtenerPrueba(id: Long): PruebaEntity? = pruebaDao.obtener(id)
    suspend fun pruebaAnteriorA(prueba: PruebaEntity): PruebaEntity? =
        pruebaDao.anteriorA(prueba.equipoId, prueba.tipoPrueba, prueba.fecha)
    fun observarVisitasAbiertas(): Flow<List<Long>> = visitaDao.observarAbiertas()

    /** Regla: una prueba se corrige solo si es de una visita que todavía NO está cerrada. */
    suspend fun sePuedeCorregir(prueba: PruebaEntity): Boolean {
        val visitaId = prueba.visitaId ?: return false
        val visita = visitaDao.obtener(visitaId) ?: return false
        return visita.estado != EstadosVisita.CERRADA
    }

    /** Corrige una medición mal ingresada. Devuelve false si la visita ya se cerró. */
    suspend fun corregirPrueba(prueba: PruebaEntity): Boolean {
        if (!sePuedeCorregir(prueba)) return false
        pruebaDao.actualizar(prueba)
        return true
    }

    // ---------------- Catálogo de pruebas ----------------
    fun observarCatalogo(): Flow<List<TipoPruebaEntity>> = tipoPruebaDao.observarTodos()
    suspend fun pruebasActivasPara(tipoEquipo: String): List<TipoPruebaEntity> = tipoPruebaDao.activasPara(tipoEquipo)
    suspend fun obtenerTipoPrueba(id: Long): TipoPruebaEntity? = tipoPruebaDao.obtener(id)
    suspend fun existeTipoPrueba(tipoEquipo: String, nombre: String, excluirId: Long): Boolean =
        tipoPruebaDao.contarConNombre(tipoEquipo, nombre, excluirId) > 0

    suspend fun guardarTipoPrueba(tipo: TipoPruebaEntity) {
        if (tipo.id == 0L) tipoPruebaDao.insertar(tipo) else tipoPruebaDao.actualizar(tipo)
    }

    // ---------------- Datos ficticios iniciales ----------------

    suspend fun cargarDatosIniciales() {
        if (usuarioDao.contar() > 0) return
        db.withTransaction {
            val dia = 24L * 60 * 60 * 1000
            val ahora = System.currentTimeMillis()
            fun hoyA(hora: Int, minuto: Int, masDias: Int = 0): Long = Calendar.getInstance().run {
                add(Calendar.DAY_OF_YEAR, masDias)
                set(Calendar.HOUR_OF_DAY, hora); set(Calendar.MINUTE, minuto)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                timeInMillis
            }

            // Usuarios: técnico 12.345.678-5, técnica 16.789.123-3, supervisora 15.432.876-9
            val juan = usuarioDao.insertar(UsuarioEntity(rut = "12345678-5", nombre = "Juan Pérez", rol = Roles.TECNICO, correo = "jperez@induztek.cl", telefono = "+56 9 1111 2222"))
            val camila = usuarioDao.insertar(UsuarioEntity(rut = "16789123-3", nombre = "Camila Rojas", rol = Roles.TECNICO, correo = "crojas@induztek.cl", telefono = "+56 9 3333 4444"))
            usuarioDao.insertar(UsuarioEntity(rut = "15432876-9", nombre = "Andrea Soto", rol = Roles.SUPERVISOR, correo = "asoto@induztek.cl", telefono = "+56 9 5555 6666"))

            // Clientes e instalaciones
            val c1 = clienteDao.insertarCliente(ClienteEntity(razonSocial = "Frigorífico Austral SpA", rut = "76.123.456-0", correo = "mantencion@frigoaustral.cl", telefono = "+56 9 8123 4567"))
            val c2 = clienteDao.insertarCliente(ClienteEntity(razonSocial = "Papelera del Maipo S.A.", rut = "77.654.321-7", correo = "planta@papelmaipo.cl", telefono = "+56 9 7654 3210"))
            val c3 = clienteDao.insertarCliente(ClienteEntity(razonSocial = "Agrícola Lampa Ltda.", rut = "78.900.111-1", correo = "operaciones@agrolampa.cl", telefono = "+56 9 6900 1112"))
            val i1 = clienteDao.insertarInstalacion(InstalacionEntity(clienteId = c1, nombre = "Subestación Quilicura", comuna = "Quilicura"))
            val i2 = clienteDao.insertarInstalacion(InstalacionEntity(clienteId = c2, nombre = "Sala eléctrica Planta Maipú", comuna = "Maipú"))
            val i3 = clienteDao.insertarInstalacion(InstalacionEntity(clienteId = c3, nombre = "Patio de cables Lampa", comuna = "Lampa"))

            // Catálogo de pruebas (valores REFERENCIALES ficticios)
            listOf(
                TipoPruebaEntity(tipoEquipo = "Transformador", nombre = "Resistencia de aislamiento", unidad = "MΩ", minimo = 1000.0, maximo = null),
                TipoPruebaEntity(tipoEquipo = "Transformador", nombre = "Relación de transformación (error)", unidad = "%", minimo = null, maximo = 0.5),
                TipoPruebaEntity(tipoEquipo = "Relé de protección", nombre = "Tiempo de operación", unidad = "ms", minimo = null, maximo = 100.0),
                TipoPruebaEntity(tipoEquipo = "Relé de protección", nombre = "Resistencia de contactos", unidad = "mΩ", minimo = null, maximo = 50.0),
                TipoPruebaEntity(tipoEquipo = "Celda", nombre = "Resistencia de aislamiento", unidad = "MΩ", minimo = 1000.0, maximo = null),
                TipoPruebaEntity(tipoEquipo = "Celda", nombre = "Resistencia de contactos del interruptor", unidad = "µΩ", minimo = null, maximo = 100.0),
                TipoPruebaEntity(tipoEquipo = "Cable de poder", nombre = "Resistencia de aislamiento", unidad = "MΩ", minimo = 100.0, maximo = null),
                TipoPruebaEntity(tipoEquipo = "Cable de poder", nombre = "Índice de polarización", unidad = "", minimo = 2.0, maximo = null),
                TipoPruebaEntity(tipoEquipo = "Otro", nombre = "Prueba libre", unidad = "", minimo = null, maximo = null)
            ).forEach { tipoPruebaDao.insertar(it) }

            // Equipos
            fun equipo(codigo: String, tipo: String, inst: Long, estado: String, marca: String, modelo: String, serie: String, gps: String?) =
                EquipoEntity(codigo = codigo, tipo = tipo, instalacionId = inst, estado = estado, marca = marca, modelo = modelo, serie = serie, ubicacion = gps, fechaRegistro = ahora - 400 * dia)
            val tr1 = equipoDao.insertar(equipo("TR-0001", "Transformador", i1, EstadosEquipo.OPERATIVO, "Voltrafo", "VT-500", "VT5-88213", "-33.36512, -70.73114"))
            val rp3 = equipoDao.insertar(equipo("RP-0003", "Relé de protección", i1, EstadosEquipo.PENDIENTE, "Protek", "PR-21", "PK-21-0091", "-33.36520, -70.73101"))
            val cl7 = equipoDao.insertar(equipo("CL-0007", "Celda", i1, EstadosEquipo.PENDIENTE, "Celmax", "CM-12", "CMX-12-554", null))
            val tr2 = equipoDao.insertar(equipo("TR-0002", "Transformador", i2, EstadosEquipo.MANTENCION, "Voltrafo", "VT-300", "VT3-10442", "-33.51040, -70.75812"))
            val rp10 = equipoDao.insertar(equipo("RP-0010", "Relé de protección", i2, EstadosEquipo.OPERATIVO, "Protek", "PR-30", "PK-30-1207", "-33.51022, -70.75830"))
            val cb12 = equipoDao.insertar(equipo("CB-0012", "Cable de poder", i3, EstadosEquipo.FUERA, "Cablenor", "XLPE 15 kV", "CN-15-7781", "-33.28110, -70.87760"))
            val cb13 = equipoDao.insertar(equipo("CB-0013", "Cable de poder", i3, EstadosEquipo.OPERATIVO, "Cablenor", "XLPE 15 kV", "CN-15-7782", "-33.28102, -70.87741"))

            // Visitas: hoy (en curso), pasado mañana, hoy de Camila y una cerrada hace 6 días
            val v142 = visitaDao.insertar(VisitaEntity(numero = "PT-2026-0142", instalacionId = i1, tecnicoId = juan, fechaHora = hoyA(9, 0), estado = EstadosVisita.EN_CURSO, temperatura = 18.0, humedad = 55.0, instrumentos = "MG-5,AR-2"))
            val v143 = visitaDao.insertar(VisitaEntity(numero = "PT-2026-0143", instalacionId = i2, tecnicoId = juan, fechaHora = hoyA(10, 30, 2), estado = EstadosVisita.PROGRAMADA))
            val v141 = visitaDao.insertar(VisitaEntity(numero = "PT-2026-0141", instalacionId = i2, tecnicoId = camila, fechaHora = hoyA(15, 0), estado = EstadosVisita.PROGRAMADA))
            val v139 = visitaDao.insertar(
                VisitaEntity(
                    numero = "PT-2026-0139", instalacionId = i3, tecnicoId = juan, fechaHora = hoyA(8, 30, -6), estado = EstadosVisita.CERRADA,
                    temperatura = 14.0, humedad = 80.0, instrumentos = "MG-5",
                    observacion = "Cable CB-0012 con aislamiento bajo el mínimo. Se recomienda reemplazo.",
                    recibidoPor = "Pedro Lagos", correoCliente = "operaciones@agrolampa.cl", cerradaEn = hoyA(13, 5, -6)
                )
            )
            listOf(v142 to tr1, v142 to rp3, v142 to cl7, v143 to tr2, v143 to rp10, v141 to rp10, v139 to cb12, v139 to cb13)
                .forEach { (v, e) -> visitaDao.agregarEquipo(VisitaEquipoEntity(v, e)) }

            // Pruebas (con historial para comparar y para la tendencia)
            fun prueba(eq: Long, vis: Long?, tipo: String, valor: Double, unidad: String, min: Double?, max: Double?, fecha: Long, temp: Double, tecnico: String, obs: String = "") =
                PruebaEntity(equipoId = eq, visitaId = vis, tipoPrueba = tipo, valorMedido = valor, unidad = unidad, minimoUsado = min, maximoUsado = max,
                    temperaturaAmbiente = temp, tecnico = tecnico, observaciones = obs,
                    aprobada = Reglas.cumpleCriterio(valor, min, max), fecha = fecha)
            listOf(
                prueba(tr1, null, "Resistencia de aislamiento", 2400.0, "MΩ", 1000.0, null, ahora - 365 * dia, 17.0, "Camila Rojas"),
                prueba(tr1, null, "Resistencia de aislamiento", 2150.0, "MΩ", 1000.0, null, ahora - 180 * dia, 21.0, "Juan Pérez"),
                prueba(tr1, null, "Relación de transformación (error)", 0.28, "%", null, 0.5, ahora - 180 * dia, 21.0, "Juan Pérez"),
                prueba(tr1, v142, "Resistencia de aislamiento", 1380.0, "MΩ", 1000.0, null, hoyA(9, 40), 18.0, "Juan Pérez"),
                prueba(tr1, v142, "Relación de transformación (error)", 0.3, "%", null, 0.5, hoyA(9, 55), 18.0, "Juan Pérez"),
                prueba(rp10, null, "Tiempo de operación", 82.0, "ms", null, 100.0, ahora - 195 * dia, 20.0, "Camila Rojas"),
                prueba(rp10, null, "Tiempo de operación", 130.0, "ms", null, 100.0, ahora - 14 * dia, 22.0, "Camila Rojas", "Operó lento en 3 de 3 disparos."),
                prueba(tr2, null, "Resistencia de aislamiento", 1240.0, "MΩ", 1000.0, null, ahora - 195 * dia, 20.0, "Camila Rojas"),
                prueba(cb12, v139, "Resistencia de aislamiento", 62.0, "MΩ", 100.0, null, hoyA(9, 15, -6), 14.0, "Juan Pérez", "Bajo el mínimo en fase B."),
                prueba(cb12, v139, "Índice de polarización", 1.4, "", 2.0, null, hoyA(9, 40, -6), 14.0, "Juan Pérez", "Coherente con humedad en el aislamiento."),
                prueba(cb13, v139, "Resistencia de aislamiento", 450.0, "MΩ", 100.0, null, hoyA(10, 20, -6), 14.0, "Juan Pérez"),
                prueba(cb13, v139, "Índice de polarización", 2.4, "", 2.0, null, hoyA(10, 45, -6), 14.0, "Juan Pérez")
            ).forEach { pruebaDao.insertar(it) }

            // Historial de estados
            listOf(
                CambioEstadoEntity(equipoId = tr1, estadoAnterior = EstadosEquipo.PENDIENTE, estadoNuevo = EstadosEquipo.OPERATIVO, motivo = "Pruebas aprobadas.", usuario = "Andrea Soto", fecha = ahora - 179 * dia),
                CambioEstadoEntity(equipoId = rp3, estadoAnterior = EstadosEquipo.OPERATIVO, estadoNuevo = EstadosEquipo.PENDIENTE, motivo = "Prueba anual vencida.", usuario = "Andrea Soto", fecha = ahora - 13 * dia),
                CambioEstadoEntity(equipoId = cl7, estadoAnterior = "—", estadoNuevo = EstadosEquipo.PENDIENTE, motivo = "Registro del equipo.", usuario = "Andrea Soto", fecha = ahora - 16 * dia),
                CambioEstadoEntity(equipoId = tr2, estadoAnterior = EstadosEquipo.OPERATIVO, estadoNuevo = EstadosEquipo.MANTENCION, motivo = "Cambio de aceite programado por el cliente.", usuario = "Andrea Soto", fecha = ahora - 9 * dia),
                CambioEstadoEntity(equipoId = cb12, estadoAnterior = EstadosEquipo.OPERATIVO, estadoNuevo = EstadosEquipo.FUERA, motivo = "Aislamiento bajo el mínimo en PT-2026-0139. Se pide reemplazo.", usuario = "Andrea Soto", fecha = ahora - 6 * dia + 8 * 60 * 60 * 1000)
            ).forEach { cambioEstadoDao.insertar(it) }
        }
    }

    companion object {
        /** Crea el repositorio a partir de la base de datos única de la app. */
        fun desde(context: Context): ProtocoloRepository = ProtocoloRepository(AppDatabase.obtener(context))
    }
}