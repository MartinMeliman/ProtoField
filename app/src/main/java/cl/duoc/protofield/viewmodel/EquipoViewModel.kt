package cl.duoc.protofield.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.protofield.data.device.FotoStorage
import cl.duoc.protofield.data.device.UbicacionProvider
import cl.duoc.protofield.data.local.entity.EquipoEntity
import cl.duoc.protofield.data.local.storage.UserPreferences
import cl.duoc.protofield.data.repository.ProtocoloRepository
import cl.duoc.protofield.model.EquipoErrores
import cl.duoc.protofield.model.EquipoUiState
import cl.duoc.protofield.model.EstadosEquipo
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.TiposEquipo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

/**
 * PANTALLA 7 · REGISTRAR / EDITAR EQUIPO.
 * Aquí están los DOS RECURSOS NATIVOS: GPS (UbicacionProvider) y cámara (FotoStorage).
 * Es AndroidViewModel porque necesita el contexto para la base de datos, el GPS y los archivos.
 */
class EquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ProtocoloRepository.desde(application)
    private val preferencias = UserPreferences(application)
    private val ubicacionProvider = UbicacionProvider(application)
    private val fotoStorage = FotoStorage(application)

    private val _estado = MutableStateFlow(value = EquipoUiState())
    val estado: StateFlow<EquipoUiState> = _estado

    private var cargado = false

    /**
     * @param equipoId > 0 = editar ese equipo.
     * @param visitaId si viene de una visita, el equipo nuevo se agrega a ella.
     * @param codigo si viene del escáner QR, el código ya viene escrito.
     */
    fun cargar(equipoId: Long?, visitaId: Long?, codigo: String?) {
        if (cargado) return
        cargado = true
        viewModelScope.launch {
            val instalaciones = repositorio.observarInstalaciones().first()
            val equipo = equipoId?.let { repositorio.obtenerEquipo(it) }
            val visita = visitaId?.let { repositorio.obtenerVisita(it) }
            _estado.update {
                if (equipo != null) {
                    it.copy(
                        esEdicion = true, equipoId = equipo.id, instalaciones = instalaciones,
                        codigo = equipo.codigo, tipo = equipo.tipo, instalacionId = equipo.instalacionId,
                        marca = equipo.marca, modelo = equipo.modelo, serie = equipo.serie,
                        ubicacion = equipo.ubicacion.orEmpty(), fotoPath = equipo.fotoPath
                    )
                } else {
                    val cod = codigo.orEmpty().uppercase()
                    it.copy(
                        instalaciones = instalaciones,
                        visitaId = visita?.id,
                        instalacionId = visita?.instalacionId,   // si viene de una visita, ya se sabe dónde está
                        codigo = cod,
                        tipo = TiposEquipo.desdeCodigo(cod)
                    )
                }
            }
        }
    }

    // ----------------- Recursos nativos -----------------

    /** GPS: se llama cuando la pantalla ya tiene el permiso concedido. */
    fun obtenerUbicacion() {
        _estado.update { it.copy(cargandoUbicacion = true, aviso = null) }
        viewModelScope.launch {
            val ubicacion = ubicacionProvider.obtenerActual()
            _estado.update {
                if (ubicacion != null) {
                    val texto = String.format(Locale.US, "%.5f, %.5f", ubicacion.latitude, ubicacion.longitude)
                    it.copy(ubicacion = texto, cargandoUbicacion = false)
                } else {
                    it.copy(cargandoUbicacion = false, aviso = "No se pudo obtener la ubicación. Revisa que el GPS esté activo.")
                }
            }
        }
    }

    // Ruta del archivo que se le pasó a la cámara (todavía sin confirmar si sacó la foto)
    private var fotoPendiente: String? = null

    /** Cámara, paso 1: crea el archivo vacío y devuelve la Uri donde la cámara guardará la foto. */
    fun prepararFoto(): Uri {
        val archivo = fotoStorage.crearArchivoFoto()
        fotoPendiente = archivo.absolutePath
        return fotoStorage.obtenerUri(archivo)
    }

    /** Cámara, paso 2: true si sacó la foto, false si canceló. */
    fun onFotoResultado(exito: Boolean) {
        val ruta = fotoPendiente ?: return
        if (exito) _estado.update { it.copy(fotoPath = ruta, aviso = null) }
        else File(ruta).delete() // canceló: borramos el archivo vacío
        fotoPendiente = null
    }

    /** Si rechazan un permiso, la app sigue funcionando y explica qué pasa. */
    fun onPermisoDenegado(recurso: String) {
        _estado.update { it.copy(aviso = "Sin permiso de $recurso no se puede usar esta función. Puedes guardar el equipo igual.") }
    }

    // ----------------- Campos -----------------

    fun onCodigoChange(valor: String) {
        // Regla: el código siempre en mayúsculas (tr-0001 → TR-0001)
        _estado.update { it.copy(codigo = valor.uppercase().trim(), errores = it.errores.copy(codigo = null)) }
    }

    fun onTipoChange(valor: String) {
        _estado.update { it.copy(tipo = valor, errores = it.errores.copy(tipo = null, codigo = null)) }
    }

    fun onInstalacionChange(id: Long) {
        _estado.update { it.copy(instalacionId = id, errores = it.errores.copy(instalacion = null)) }
    }

    fun onMarcaChange(valor: String) = _estado.update { it.copy(marca = valor) }      // opcional
    fun onModeloChange(valor: String) = _estado.update { it.copy(modelo = valor) }    // opcional

    fun onSerieChange(valor: String) {
        _estado.update { it.copy(serie = valor, errores = it.errores.copy(serie = null)) }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val prefijo = TiposEquipo.prefijo(actual.tipo)
        val errores = EquipoErrores(
            codigo = when {
                actual.esEdicion -> null // al editar el código no cambia
                actual.codigo.isBlank() -> "El código es obligatorio"
                !Reglas.REGEX_CODIGO.matches(actual.codigo) -> "Formato: 2 letras, guion y 4 números (ej: TR-0004)"
                prefijo.isNotEmpty() && !actual.codigo.startsWith(prefijo) -> "Para ${actual.tipo.lowercase()} el código parte con $prefijo"
                else -> null
            },
            tipo = if (actual.tipo.isBlank()) "Selecciona el tipo de equipo" else null,
            instalacion = if (actual.instalacionId == null) "Elige dónde está el equipo" else null,
            serie = if (actual.serie.trim().length < 3) "Ingresa el n° de serie de la placa" else null
        )
        val hayErrores = listOfNotNull(errores.codigo, errores.tipo, errores.instalacion, errores.serie).isNotEmpty()
        _estado.update { it.copy(errores = errores) }
        return !hayErrores
    }

    /** Guarda en Room. Al crear, revisa que el código no exista (regla de negocio). */
    fun guardar(onGuardado: () -> Unit) {
        val actual = _estado.value
        val instalacionId = actual.instalacionId ?: return
        viewModelScope.launch {
            if (actual.esEdicion) {
                val original = repositorio.obtenerEquipo(actual.equipoId) ?: return@launch
                repositorio.actualizarEquipo(
                    original.copy(
                        instalacionId = instalacionId, marca = actual.marca.trim(), modelo = actual.modelo.trim(),
                        serie = actual.serie.trim(), ubicacion = actual.ubicacion.ifBlank { null }, fotoPath = actual.fotoPath
                    )
                )
            } else {
                if (repositorio.existeCodigo(actual.codigo)) {
                    _estado.update { it.copy(errores = it.errores.copy(codigo = "Ya existe un equipo con este código")) }
                    return@launch
                }
                val usuario = preferencias.obtenerSesion().first()?.nombre.orEmpty()
                repositorio.registrarEquipo(
                    EquipoEntity(
                        codigo = actual.codigo, tipo = actual.tipo, instalacionId = instalacionId,
                        estado = EstadosEquipo.PENDIENTE, marca = actual.marca.trim(), modelo = actual.modelo.trim(),
                        serie = actual.serie.trim(), ubicacion = actual.ubicacion.ifBlank { null }, fotoPath = actual.fotoPath
                    ),
                    usuario = usuario,
                    visitaId = actual.visitaId
                )
            }
            onGuardado()
        }
    }
}
