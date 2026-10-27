# AgroCampo - Aplicación Android de AgroSierra

Grease Duran Castro. Aplicación KMP nueva para el Examen Parcial II, caso A. Plataforma evaluada: Android; este examen no exige una ejecución iOS.

## Abrir y ejecutar

Abrir esta carpeta en Android Studio, sincronizar Gradle, encender Pixel 9 (2) y ejecutar `androidApp`. Requisitos: Android SDK 36, JDK 21 o superior y AgroAPI en el puerto 8081. El emulador accede al equipo anfitrión mediante `http://10.0.2.2:8081/api/v1/`, centralizado en ApiConfig. Para otro dispositivo debe configurarse una dirección de red accesible y su configuración de transporte local.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\verificar.ps1
```

Genera `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, ejecuta commonTest sobre Android Host y guarda `verificacion-ultima.log`. No es necesario mantener PharmaMobil ni PharmaSoft abiertos. Credenciales: tecnico1, jefe1, admin o calidad1; contraseña `Agro2026*`.

## Funciones

- Login con mensajes distintos para credenciales incorrectas y falta de conexión.
- Recuperación de sesión guardada si el JWT no venció; vencido regresa al login. Un 401 del servidor borra la sesión; un 403 la mantiene.
- Padrón de lotes leído de SQLDelight con buscador por nombre/código y filtro por valle.
- Detalle y labores del lote, formulario, corrección y eliminación según privilegios.
- Cierre de campaña contra el servicio para quien posee CAMPANIA_CERRAR.
- Perfil con nombre, usuario, rol y privilegios; cierre que elimina el token cifrado.
- Escrituras locales sin red, ID provisional negativo, marca pendiente, sincronización manual y al recuperar conectividad.
- Indicador de progreso, resumen de enviados/recibidos y antigüedad de la última sincronización exitosa.

## Capas y recorrido de un dato

`shared/src/commonMain/kotlin/pe/upeu/agrosierra/`:

- `domain/model`: Lote, Labor, TipoLabor, Sesion y jerarquía sellada ErrorApi, sin serialización.
- `domain/repository`: contratos; `domain/usecase`: observar lotes, guardar labor y sincronizar.
- `data/remote`: cliente único Ktor, ApiConfig, AgroApi, traducción de errores y DTO separados del dominio.
- `data/local`: interfaz AlmacenSeguro, Reloj y BaseLocal; esquema en commonMain/sqldelight.
- `data/repository`: sesión REST y repositorio offline-first.
- `presentation`: ViewModel con StateFlow/UiState y pantallas Compose.
- `di`: redModule, dataModule, domainModule y presentationModule.

`androidApp/src/main/kotlin/pe/upeu/agrosierra/` proporciona el driver Android de SQLDelight, almacenamiento cifrado respaldado por Android Keystore, motor OkHttp, reloj y platformModule. La aplicación arranca Koin con una única instancia del cliente HTTP.

Recorrido de lectura: tabla relacional del servidor -> respuesta paginada -> DTO Ktor -> mapper a dominio -> transacción SQLDelight -> consulta observable Flow -> UiState del ViewModel -> Compose. La pantalla no toma sus listas de la respuesta HTTP.

## Sincronización y datos locales

La base `agrocampo.db` contiene lote, labor, envio y metadato. `minOf(-1, MIN(id)-1)` reserva IDs fuera del rango positivo del servidor. La cola persiste POST, PUT y DELETE con una clave por operación. Primero se envían los pendientes; cada confirmación sustituye la fila provisional dentro de una transacción. Después se descargan todas las páginas de lotes y labores. El catálogo solo se reemplaza tras completar la descarga; los pendientes no se borran por un fallo de red. Un Mutex evita carreras entre guardado y sincronización.

El POST usa una clave de idempotencia conservada por el servidor. DELETE 204 no se deserializa. Si el servidor ya eliminó una fila pendiente de borrado, 404 confirma su ausencia y permite limpiar la cola. En consultas de recursos, 404 informa y refresca; no ofrece reintento.

La sesión se guarda con EncryptedSharedPreferences y una MasterKey de Android Keystore; la cabecera Bearer se añade en un solo punto. El análisis local de exp decide únicamente si puede reabrirse la sesión guardada: la firma y los permisos de cada operación se verifican obligatoriamente en AgroAPI. No hay modo de sesión vencida de solo lectura: el examen exige volver al login.

La caché identifica a su usuario. Al cambiar de cuenta se elimina únicamente una caché sin pendientes; si hay cambios pendientes de otra cuenta se exige volver a ella y sincronizarlos primero. No se descartan escrituras silenciosamente.

## Estados y errores

400 muestra validationErrors debajo de su campo; 401 borra sesión y vuelve al login; 403 informa y conserva sesión; 404 informa y refresca; 409 muestra el motivo del servidor; 500 ofrece reintento; falta de conexión conserva el guardado y avisa; timeout se distingue de validación y de falta de privilegio. Las pantallas muestran progreso, contenido, vacío y mensajes de error.

## Cuatro solicitudes de cambio

- `sc-a-duran`: Solo campañas abiertas, combinado con valle y búsqueda en SQL, también sin conexión.
- `sc-b-duran`: pruebas del supervisor de calidad. La interfaz ya interpreta privilegios y alcance recibidos, sin condicionales por nombre del rol.
- `sc-c-duran`: cantidadJornales recorre contrato, mapper, modelo, esquema, formulario y detalle. Migración 1.sqm conserva filas anteriores.
- `sc-d-duran`: distintivo por lote desde un COUNT observable de la cola SQL, sin contador manual.

Los detalles están en `cambios/`. Cada solicitud puede identificarse por su rama y por las pruebas que incorpora.

## Pruebas

commonTest utiliza MockEngine y un driver SQLite en memoria independiente por caso. Verifica DTO, errores, registro sin conexión, orden de sincronización, ausencia de duplicados del escenario, conservación ante fallo, 204, filtros combinados, permisos del supervisor, jornales, migración y contador de pendientes. Resultados en `evidencias/` y en `shared/build/reports/tests/testAndroidHostTest/index.html` después de verificar.

Repositorio: https://github.com/GreysiLL/exa-agrocampo
Backend: https://github.com/GreysiLL/ExamenS2
Colección de pruebas HTTP en `postman/`.

## Trazabilidad real

Proyecto iniciado desde cero el 27 de octubre de 2026, con ramas y commits reales, sin alterar fechas. El requisito de historial distribuido durante la unidad no se puede recuperar iniciando hoy. Las evidencias y la etiqueta final solo deben considerarse completas cuando conste su verificación y publicación.

## Informe de evidencias

[Informe del examen con capturas](evidencias/Examen_Parcial_II_AgroSierra_DuranCastro.pdf). Verificación final: backend 9 pruebas; Android 13 pruebas; Postman/Newman 27 solicitudes y 30 comprobaciones aprobadas.
