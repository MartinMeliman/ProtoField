package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.VisitaEntity
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.EstadosVisita
import cl.duoc.protofield.model.NuevaVisitaErrores
import cl.duoc.protofield.model.NuevaVisitaUiState
import cl.duoc.protofield.model.Reglas
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/**
 * PANTALLA 11 · NUEVA VISITA (supervisor).
 * Listas DEPENDIENTES: al elegir el cliente se cargan sus instalaciones,
 * y al elegir la instalación se cargan sus equipos (los pendientes primero).
 */
class NuevaVisitaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)

    private val _estado = MutableStateFlow(value = NuevaVisitaUiState())
    val estado: StateFlow<NuevaVisitaUiState> = _estado

    private var trabajoEquipos: Job? = null
    private var cargado = false

    /** @param equipoId si viene de "Programar prueba preventiva", ese equipo ya viene elegido. */
    fun cargar(equipoId: Long?) {
        if (cargado) return
        cargado = true
        viewModelScope.launch {
            val clientes = repositorio.observarClientes().first()
            val tecnicos = repositorio.observarTecnicos().first()
            _estado.update { it.copy(clientes = clientes, tecnicos = tecnicos, fecha = mananaUtc()) }
            val equipo = equipoId?.let { repositorio.obtenerEquipo(it) } ?: return@launch
            val instalacion = repositorio.obtenerInstalacion(equipo.instalacionId) ?: return@launch
            seleccionarCliente(instalacion.clienteId)
            onInstalacionChange(instalacion.id)
            _estado.update { it.copy(equiposSeleccionados = setOf(equipo.id)) }
        }
    }

    fun onClienteChange(id: Long) {
        viewModelScope.launch { seleccionarCliente(id) }
    }

    /** Al cambiar el cliente se cargan SUS instalaciones y se borra lo que dependía del anterior. */
    private suspend fun seleccionarCliente(id: Long) {
        trabajoEquipos?.cancel()
        val instalaciones = repositorio.observarInstalaciones().first().filter { it.clienteId == id }
        _estado.update {
            it.copy(
                clienteId = id, instalaciones = instalaciones,
                instalacionId = null, equiposInstalacion = emptyList(), equiposSeleccionados = emptySet(),
                errores = it.errores.copy(cliente = null)
            )
        }
    }

    fun onInstalacionChange(id: Long) {
        _estado.update { it.copy(instalacionId = id, equiposSeleccionados = emptySet(), errores = it.errores.copy(instalacion = null)) }
        trabajoEquipos?.cancel()
        trabajoEquipos = viewModelScope.launch {
            repositorio.observarEquiposDeInstalacion(id).collect { equipos ->
                _estado.update { it.copy(equiposInstalacion = equipos) }
            }
        }
    }

    /** El DatePicker entrega la fecha en milisegundos (UTC). */
    fun onFechaChange(millis: Long?) {
        _estado.update { it.copy(fecha = millis, errores = it.errores.copy(fecha = null)) }
    }

    fun onHoraChange(valor: String) {
        _estado.update { it.copy(hora = valor.trim(), errores = it.errores.copy(hora = null)) }
    }

    fun onTecnicoChange(id: Long) {
        _estado.update { it.copy(tecnicoId = id, errores = it.errores.copy(tecnico = null)) }
    }

    fun onEquipoToggle(id: Long) {
        _estado.update {
            val actuales = it.equiposSeleccionados
            it.copy(
                equiposSeleccionados = if (id in actuales) actuales - id else actuales + id,
                errores = it.errores.copy(equipos = null)
            )
        }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val errores = NuevaVisitaErrores(
            cliente = if (actual.clienteId == null) "Elige el cliente" else null,
            instalacion = if (actual.instalacionId == null) "Elige la instalación" else null,
            fecha = when {
                actual.fecha == null -> "Elige la fecha"
                actual.fecha < hoyUtc() -> "No puede ser anterior a hoy"
                else -> null
            },
            hora = if (!Reglas.REGEX_HORA.matches(actual.hora)) "Hora inválida (ej: 09:30)" else null,
            tecnico = if (actual.tecnicoId == null) "Asigna un técnico" else null,
            equipos = if (actual.equiposSeleccionados.isEmpty()) "Elige al menos un equipo" else null
        )
        val hayErrores = listOfNotNull(
            errores.cliente, errores.instalacion, errores.fecha, errores.hora, errores.tecnico, errores.equipos
        ).isNotEmpty()
        _estado.update { it.copy(errores = errores) }
        return !hayErrores
    }

    fun guardar(onGuardado: () -> Unit) {
        val actual = _estado.value
        val instalacionId = actual.instalacionId ?: return
        val tecnicoId = actual.tecnicoId ?: return
        val fecha = actual.fecha ?: return
        viewModelScope.launch {
            repositorio.programarVisita(
                VisitaEntity(
                    numero = "", // lo asigna el repositorio
                    instalacionId = instalacionId,
                    tecnicoId = tecnicoId,
                    fechaHora = unirFechaYHora(fecha, actual.hora),
                    estado = EstadosVisita.PROGRAMADA
                ),
                actual.equiposSeleccionados.toList()
            )
            onGuardado()
        }
    }

    // ---------------- Fechas ----------------

    /** El DatePicker trabaja en UTC: hoy a las 00:00 UTC. */
    private fun hoyUtc(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    private fun mananaUtc(): Long = hoyUtc() + 24L * 60 * 60 * 1000

    /** Fecha del DatePicker (UTC) + "09:30" → milisegundos en la hora de Chile. */
    private fun unirFechaYHora(fechaUtc: Long, hora: String): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = fechaUtc }
        val (h, m) = hora.split(":").map { it.toInt() }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), h, m)
        }.timeInMillis
    }
}