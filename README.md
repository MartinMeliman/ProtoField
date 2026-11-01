# ProtoField

App Android para **Induztek Ingeniería SpA** (caso DSY1105): digitaliza el protocolo de pruebas eléctricas en terreno. El técnico registra las pruebas en el teléfono, sin internet, y entrega al cliente el protocolo firmado **el mismo día de la visita**. Hoy ese protocolo se pasa a Excel en la oficina días después.

> Todos los datos de la app son ficticios.

## Integrantes

- Integrante A: Martín Meliman
- Integrante B: Ignacio Lapierre

## Funcionalidades (14 pantallas)

| N° | Pantalla | Rol |
| --- | --- | --- |
| 1 | Login con RUT (dígito verificador módulo 11) | Todos |
| 2 | Visitas: el técnico ve las suyas, el supervisor todas | Ambos |
| 3 | Escanear la etiqueta QR del equipo | Ambos |
| 4 | Visita y protocolo, con condiciones (temperatura, humedad, instrumentos) y botón "Siguiente" | Técnico |
| 5 | Detalle del equipo: historial comparado, tendencia predictiva, estados y etiqueta QR | Ambos |
| 6 | Nueva prueba con veredicto en vivo según el catálogo; corregir una prueba mientras la visita no esté cerrada | Técnico |
| 7 | Registrar o editar equipo con **GPS** y **foto de la placa (cámara)** | Ambos |
| 8 | Cerrar protocolo con firma del cliente en pantalla | Técnico |
| 9 | Protocolo para el cliente | Ambos |
| 10 | Equipos con buscador y filtro por estado | Ambos |
| 11 | Nueva visita (asignar técnico y equipos) | Supervisor |
| 12 | Cambiar estado del equipo con motivo | Supervisor |
| 13 | Catálogo de pruebas por tipo de equipo | Supervisor |
| 14 | Nueva o editar prueba del catálogo | Supervisor |

**Requisitos de la pauta:**

- MVVM: Data (`model`, `data`) → ViewModel (`viewmodel`) → Vista (`ui/screen`).
- Formularios validados desde el ViewModel, con íconos, mensajes por campo y animación.
- Persistencia local con **Room** (9 tablas con relaciones) y sesión con **DataStore**.
- Recursos nativos: **GPS** (FusedLocationProvider) y **cámara** (TakePicture + FileProvider). Además, escáner QR (Google Code Scanner).
- Animaciones: sacudón de errores, veredicto en vivo, transiciones entre pantallas, barra de avance, escáner y listas animadas.
- Pruebas unitarias de las reglas de negocio (`app/src/test/.../ReglasTest.kt`).

## Requisitos cubiertos

| Requisito | Dónde |
| --- | --- |
| MVVM | Modelo: `model/` y `data/` · ViewModel: `viewmodel/` · Vista: `ui/screen/` |
| Room con relaciones | `data/local/AppDatabase.kt` (9 tablas), `entity/` (FK con CASCADE, tabla puente con clave compuesta), `dao/` (consultas con JOIN) |
| Repositorio y transacciones | `data/repository/ProtocoloRepository.kt` (`withTransaction`) |
| Corrutinas | DAO `suspend` + `Flow`; `viewModelScope.launch`, `combine`, `stateIn` en los ViewModels |
| DataStore | `data/local/storage/UserPreferences.kt` (sesión: id, nombre y rol) |
| Login contra Room | `UsuarioDao.buscarPorRut`, `LoginViewModel`, `LoginScreen` (RUT con módulo 11) |
| Formularios validados en el ViewModel | `LoginViewModel`, `PruebaViewModel`, `EquipoViewModel`, `NuevaVisitaViewModel`, `TipoPruebaViewModel`… (UiState + Errores por campo) |
| Recurso nativo 1: GPS | Manifest (`ACCESS_FINE_LOCATION`) → `data/device/UbicacionProvider.kt` → `EquipoViewModel` → `EquipoFormScreen` |
| Recurso nativo 2: cámara | Manifest (`CAMERA` + FileProvider) → `data/device/FotoStorage.kt` → `EquipoViewModel` → `EquipoFormScreen` (`TakePicture`) |
| Escáner y etiqueta QR | `data/device/LectorQr.kt`, `GeneradorQr.kt`, `EscanerScreen` |
| Navegación con argumentos | `navigation/Rutas.kt` (obligatorios y opcionales), `navigation/AppNavigation.kt` |
| Pruebas unitarias | `app/src/test/.../ReglasTest.kt` (7 pruebas) |
| Editar (Update) con regla de negocio | Corregir prueba: `PruebaDao.actualizar`, `ProtocoloRepository.sePuedeCorregir`, `PruebaViewModel.cargarCorreccion` |

## Animaciones

| # | Animación | Dónde |
| --- | --- | --- |
| 1 | Transición entre pantallas (`slideInHorizontally` + `fadeIn`) | `navigation/AppNavigation.kt` |
| 2 | Sacudón del campo con error (`Animatable`) y mensaje que aparece (`AnimatedVisibility`) | `ui/components/CampoTexto.kt` |
| 3 | Veredicto en vivo: escala y color verde/rojo (`scaleIn`, `animateColorAsState`) | `ui/screen/PruebaFormScreen.kt` |
| 4 | Línea que barre el visor del escáner (`rememberInfiniteTransition`) | `ui/screen/EscanerScreen.kt` |
| 5 | Barra de avance de la visita (`animateFloatAsState`) | `ui/components/TarjetaVisita.kt` |
| 6 | Listas animadas (`animateItem`) y pestañas (`AnimatedContent`) | `VisitasScreen`, `EquiposScreen`, `DetalleEquipoScreen` |
| 7 | Foto con fundido (Coil `crossfade`) | `ui/components/FotoEquipo.kt` |

## Cómo ejecutar

1. Abrir la carpeta en **Android Studio** y esperar el **Gradle Sync**. Gradle usa Java 21 (`gradle/gradle-daemon-jvm.properties`); si no está instalado, lo descarga solo.
2. Usar un emulador con **Google Play** (lo necesita el escáner QR) o un teléfono con Android 7.0 o más.
3. Ejecutar ▶ `app`. La primera vez se cargan los datos de ejemplo.
4. Entrar con un usuario de prueba:
   - Técnico: **12.345.678-5**
   - Supervisora: **15.432.876-9**
   - Contraseña: cualquiera de 6 o más caracteres
5. Pruebas unitarias: clic derecho en `ReglasTest` → **Run**.

Para el GPS en el emulador: *Extended controls → Location → Set location*.

## Tecnologías

Kotlin 2.0.21 · Jetpack Compose · Material 3 · Navigation Compose 2.9.5 · Room 2.6.1 (KSP) · DataStore 1.1.1 · Coil 2.7.0 · Play Services Location 21.3.0 · Play Services Code Scanner 16.1.0 · ZXing 3.5.3

## Próxima etapa (EFT)

Backend con Spring Boot y API REST, sincronización con Retrofit, PDF del protocolo para compartir.
