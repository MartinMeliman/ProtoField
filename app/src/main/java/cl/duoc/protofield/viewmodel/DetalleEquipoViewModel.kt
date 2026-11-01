package cl.duoc.protofield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.local.entity.CambioEstadoEntity
import cl.duoc.protofield.data.local.entity.EquipoConUbicacion
import cl.duoc.protofield.data.local.entity.PruebaEntity
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.EstadosEquipo
import cl.duoc.protofield.model.PuntoMedicion
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.Roles
import cl.duoc.protofield.model.Tendencia
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Una prueba del historial + cuánto cambió respecto a la anterior del mismo tipo. */
data class PruebaConComparacion(
    val prueba: PruebaEntity,
    val variacion: Double?,          // null = es la primera de su tipo
    val alerta: Boolean,             // true = cambió 25% o más
    val corregible: Boolean = false  // true = su visita no está cerrada y quien mira es técnico
)

enum class Pestana { PRUEBAS, ESTADOS }

/** Diálogo de eliminar: si tiene pruebas no se puede (trazabilidad). */
data class DialogoEliminar(val cantidadPruebas: Int) {
    val sePuede: Boolean get() = cantidadPruebas == 0
}

data class DetalleUiState(
    val equipo: EquipoConUbicacion? = null,
    val historial: List<PruebaConComparacion> = emptyList(),
    val cambios: List<CambioEstadoEntity> = emptyList(),
    val tendencia: Tendencia? = null,
    val rol: String = Roles.TECNICO,
    val pestana: Pestana = Pestana.PRUEBAS,
    val mostrarQr: Boolean = false,
    val dialogoEliminar: DialogoEliminar? = null
) {
    val esSupervisor: Boolean get() = rol == Roles.SUPERVISOR
    /** La última prueba salió mala y el equipo no está "Fuera de servicio" → avisar al supervisor. */
    val rechazadaSinRevisar: PruebaEntity?
        get() = historial.firstOrNull()?.prueba?.takeIf { !it.aprobada && equipo?.equipo?.estado != EstadosEquipo.FUERA }
}

/**
 * PANTALLA 5 · DETALLE DEL EQUIPO.
 * Responde a la "mejora esperada" del caso: comparar con mediciones anteriores.
 * La comparación y la tendencia se calculan AQUÍ (lógica), la pantalla solo las muestra.
 */
class DetalleEquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)

    private val _estado = MutableStateFlow(value = DetalleUiState())
    val estado: StateFlow<DetalleUiState> = _estado

    private var trabajo: Job? = null

    fun cargar(equipoId: Long) {
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            combine(
                repositorio.observarEquipo(equipoId),
                repositorio.observarPruebas(equipoId),
                repositorio.observarCambiosDeEstado(equipoId),
                preferencias.obtenerSesion(),
                repositorio.observarVisitasAbiertas()
            ) { equipo, pruebas, cambios, sesion, abiertas ->
                val rol = sesion?.rol ?: Roles.TECNICO
                _estado.value.copy(
                    equipo = equipo,
                    historial = compararConAnterior(pruebas, if (rol == Roles.TECNICO) abiertas.toSet() else emptySet()),
                    cambios = cambios,
                    tendencia = calcularTendencia(pruebas),
                    rol = rol
                )
            }.collect { nuevo -> _estado.value = nuevo }
        }
    }

    fun cambiarPestana(pestana: Pestana) = _estado.update { it.copy(pestana = pestana) }

    fun mostrarQr(mostrar: Boolean) = _estado.update { it.copy(mostrarQr = mostrar) }

    /** Antes de mostrar el diálogo revisa cuántas pruebas tiene. */
    fun pedirEliminar() {
        val equipo = _estado.value.equipo?.equipo ?: return
        viewModelScope.launch {
            val cantidad = repositorio.contarPruebasDeEquipo(equipo.id)
            _estado.update { it.copy(dialogoEliminar = DialogoEliminar(cantidad)) }
        }
    }

    fun cancelarEliminar() = _estado.update { it.copy(dialogoEliminar = null) }

    fun confirmarEliminar(onEliminado: () -> Unit) {
        val equipo = _estado.value.equipo?.equipo ?: return
        viewModelScope.launch {
            trabajo?.cancel() // deja de observar el equipo que se va a borrar
            if (repositorio.eliminarEquipo(equipo)) onEliminado()
            _estado.update { it.copy(dialogoEliminar = null) }
        }
    }

    // ---------------- Lógica interna ----------------

    /**
     * La lista viene de la más nueva a la más antigua: compara cada una con la siguiente del mismo tipo.
     * visitasAbiertas: las pruebas de esas visitas todavía se pueden corregir.
     */
    private fun compararConAnterior(pruebas: List<PruebaEntity>, visitasAbiertas: Set<Long>): List<PruebaConComparacion> =
        pruebas.mapIndexed { indice, actual ->
            val anterior = pruebas.drop(indice + 1).firstOrNull { it.tipoPrueba == actual.tipoPrueba }
            val variacion = Reglas.variacionPorcentual(actual.valorMedido, anterior?.valorMedido)
            val corregible = actual.visitaId != null && actual.visitaId in visitasAbiertas
            PruebaConComparacion(actual, variacion, Reglas.esAlerta(variacion), corregible)
        }

    /** Usa la prueba con más mediciones del equipo y el criterio de su última medición. */
    private fun calcularTendencia(pruebas: List<PruebaEntity>): Tendencia? {
        val grupo = pruebas
            .filter { it.minimoUsado != null || it.maximoUsado != null }
            .groupBy { it.tipoPrueba }
            .maxByOrNull { it.value.size } ?: return null
        val ultima = grupo.value.first()
        val puntos = grupo.value.reversed().map { PuntoMedicion(it.fecha, it.valorMedido) }
        return Tendencia.calcular(grupo.key, ultima.unidad, puntos, ultima.minimoUsado, ultima.maximoUsado)
    }
}