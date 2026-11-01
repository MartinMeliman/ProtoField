package cl.duoc.protofield.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.model.TiposEquipo
import cl.duoc.protofield.navigation.siExiste
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.FotoEquipo
import cl.duoc.protofield.ui.components.MensajeError
import cl.duoc.protofield.ui.components.SelectorLista
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.EquipoViewModel


/**
 * PANTALLA 7 · REGISTRAR / EDITAR EQUIPO.
 * Aquí se usan los DOS RECURSOS NATIVOS:
 *  1) GPS: el botón "Usar mi ubicación" (pide el permiso de ubicación)
 *  2) Cámara: "Tomar foto de la placa" (pide el permiso de cámara)
 * Los permisos están declarados en el AndroidManifest.xml.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EquipoFormScreen(
    navController: NavController,
    equipoId: Long,
    visitaId: Long,
    codigo: String,
    viewModel: EquipoViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.cargar(equipoId.siExiste(), visitaId.siExiste(), codigo.ifBlank { null }) }
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    fun tienePermiso(permiso: String) = ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED

    // ---------- RECURSO NATIVO: CÁMARA (igual que el Ejemplo 8) ----------
    // TakePicture guarda la foto en la Uri que le pasamos y devuelve true/false.
    val abrirCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        viewModel.onFotoResultado(exito)
    }
    // Pide el permiso de cámara; si lo conceden, abre la cámara
    val pedirPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara.launch(viewModel.prepararFoto()) else viewModel.onPermisoDenegado("cámara")
    }

    // ---------- RECURSO NATIVO: GPS ----------
    val pedirPermisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        if (resultado.values.any { it }) viewModel.obtenerUbicacion() else viewModel.onPermisoDenegado("ubicación")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TituloBarra(
                        if (estado.esEdicion) "Editar ${estado.codigo}" else "Registrar equipo",
                        if (estado.visitaId != null) "Se agrega a la visita" else null
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(
                valor = estado.codigo,
                onValorChange = viewModel::onCodigoChange,
                etiqueta = "Código del equipo (ej: TR-0004)",
                error = estado.errores.codigo,
                icono = Icons.Filled.Tag,
                soloLectura = estado.esEdicion   // al editar, el código no se cambia
            )

            Text("Tipo de equipo", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TiposEquipo.todos.forEach { tipo ->
                    FilterChip(
                        selected = estado.tipo == tipo,
                        onClick = { viewModel.onTipoChange(tipo) },
                        enabled = !estado.esEdicion,  // las pruebas dependen del tipo: no se cambia al editar
                        label = { Text(tipo) },
                        leadingIcon = if (estado.tipo == tipo) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null
                    )
                }
            }
            MensajeError(estado.errores.tipo)

            SelectorLista(
                etiqueta = "Cliente e instalación",
                opciones = estado.instalaciones,
                seleccionado = estado.instalaciones.firstOrNull { it.id == estado.instalacionId },
                textoDe = { "${it.clienteNombre} — ${it.nombre}" },
                onSeleccion = { viewModel.onInstalacionChange(it.id) },
                error = estado.errores.instalacion,
                icono = Icons.Filled.Business
            )

            CampoTexto(estado.marca, viewModel::onMarcaChange, "Marca (opcional)", null, icono = Icons.Filled.Badge)
            CampoTexto(estado.modelo, viewModel::onModeloChange, "Modelo (opcional)", null, icono = Icons.Filled.Badge)
            CampoTexto(estado.serie, viewModel::onSerieChange, "N° de serie de la placa", estado.errores.serie, icono = Icons.Filled.Numbers)

            // GPS
            CampoTexto(estado.ubicacion, {}, "Ubicación (GPS, opcional)", null, icono = Icons.Filled.LocationOn, soloLectura = true)
            OutlinedButton(
                onClick = {
                    val conPermiso = tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION) || tienePermiso(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (conPermiso) viewModel.obtenerUbicacion()
                    else pedirPermisoUbicacion.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                },
                enabled = !estado.cargandoUbicacion,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.cargandoUbicacion) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Filled.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (estado.cargandoUbicacion) "Buscando señal GPS…" else "Usar mi ubicación (GPS)")
            }

            // Cámara
            estado.fotoPath?.let { FotoEquipo(ruta = it) }
            OutlinedButton(
                onClick = {
                    if (tienePermiso(Manifest.permission.CAMERA)) abrirCamara.launch(viewModel.prepararFoto())
                    else pedirPermisoCamara.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (estado.fotoPath == null) "Tomar foto de la placa" else "Tomar otra foto")
            }

            // Aviso cuando un recurso nativo falla o se rechaza el permiso (con animación)
            AnimatedVisibility(visible = estado.aviso != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Text(estado.aviso.orEmpty(), modifier = Modifier.padding(12.dp))
                }
            }

            Button(
                onClick = { if (viewModel.validarFormulario()) viewModel.guardar(onGuardado = { navController.popBackStack() }) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (estado.esEdicion) "Guardar cambios" else "Guardar equipo") }
        }
    }
}