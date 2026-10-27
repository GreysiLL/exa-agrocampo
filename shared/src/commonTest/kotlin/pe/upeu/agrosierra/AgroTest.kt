package pe.upeu.agrosierra
import app.cash.sqldelight.db.SqlDriver
import kotlin.test.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.data.repository.*
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.*
expect fun driverMemoria(): SqlDriver
class AgroTest {
 private val s=Sesion("token-prueba","tecnico1","TECNICO",setOf("LOTE_LEER","LABOR_LEER","LABOR_CREAR","LABOR_EDITAR"),"Técnico",1)
 private fun sesion()=object: SesionRepository { override val sesion=MutableStateFlow<Sesion?>(s);override suspend fun entrar(u: String,c: String){};override fun salir(){sesion.value=null};override fun recuperar(){} }
 private val reloj=object: Reloj { override fun ahora()=1793100000000L;override fun hoy()="2026-10-27" }
 private fun nueva()=Labor(0,1,TipoLabor.RIEGO,"2026-10-27","",2.0,"Prueba",false)
 private val loteJson="""{"id":1,"codigo":"AS-001","nombre":"Lote prueba","hectareas":2.0,"cultivo":"Papa","valle":"Mantaro","campaniaCerrada":false,"dosisMaximaPorHectarea":2.0}"""
 private val laborJson="""{"id":21,"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","responsable":"Técnico","cantidad":2.0,"observacion":"Prueba","responsableId":1}"""
 private fun pagina(items: String)="""{"contenido":[$items],"pagina":0,"tamanio":20,"totalElementos":1,"totalPaginas":1,"ultima":true}"""
 private val me="""{"usuario":"tecnico1","nombre":"Técnico","rol":"TECNICO","privilegios":["LABOR_CREAR"],"id":1}"""
 @Test fun convierteDtoSinSerializacionEnDominio() { val dto=LoteDto(1,"AS-001","Lote",2.0,"Papa","Mantaro",false,3.0);assertEquals(6.0,dto.dominio().hectareas*dto.dominio().dosisMaximaPorHectarea) }
 @Test fun traduceErroresPorCodigoYConservaCampos() {
  val e=ErrorDto("Dosis excedida",mapOf("cantidad" to "Máximo 4"));assertEquals("Máximo 4",(traducirError(400,e) as ErrorApi.Validacion).campos["cantidad"])
  assertIs<ErrorApi.NoAutorizado>(traducirError(401,e));assertIs<ErrorApi.SinPrivilegio>(traducirError(403,e));assertIs<ErrorApi.Conflicto>(traducirError(409,e))
 }
 @Test fun registroSinRedPersisteIdNegativoYEmiteFlow()=runTest {
  val d=driverMemoria();val local=BaseLocal(d);var llamadas=0
  val http=crearCliente(MockEngine { llamadas++;throw kotlinx.io.IOException("Sin red") },TokenActual())
  try { val repo=AgroOffline(AgroApi(http),local,sesion(),reloj);val l=repo.guardar(nueva());assertTrue(l.id<0);assertTrue(local.labores(1).first().single().pendiente);assertEquals(0,llamadas);assertEquals("POST",local.q.pendientes().executeAsOne().operacion) }finally{http.close();d.close()}
 }
 @Test fun enviaAntesDeDescargarYSincronizarDosVecesNoDuplica()=runTest {
  val d=driverMemoria();val local=BaseLocal(d);val orden=mutableListOf<String>()
  val http=crearCliente(MockEngine { r->orden+="${r.method.value} ${r.url.encodedPath}";respond(when {r.url.encodedPath.endsWith("usuarios/me")->me;r.method==HttpMethod.Post->laborJson;r.url.encodedPath.endsWith("/lotes")->pagina(loteJson);else->pagina(laborJson)},HttpStatusCode.OK,headersOf(HttpHeaders.ContentType,"application/json")) },TokenActual())
  try {val repo=AgroOffline(AgroApi(http),local,sesion(),reloj);repo.guardar(nueva());repo.sincronizar();repo.sincronizar();assertTrue(orden.indexOf("POST /api/v1/labores")<orden.indexOf("GET /api/v1/lotes"));assertEquals(1,orden.count {it.startsWith("POST")});assertEquals(21L,local.labores(1).first().single().id);assertFalse(local.labores(1).first().single().pendiente);assertTrue(local.q.pendientes().executeAsList().isEmpty())}finally{http.close();d.close()}
 }
 @Test fun falloDeEnvioNoBorraPendienteNiDescarga()=runTest {
  val d=driverMemoria();val local=BaseLocal(d);var descargas=0
  val http=crearCliente(MockEngine { r->if(r.url.encodedPath.endsWith("usuarios/me"))respond(me,headers=headersOf(HttpHeaders.ContentType,"application/json")) else {if(r.method==HttpMethod.Get)descargas++;throw kotlinx.io.IOException("Sin red")} },TokenActual())
  try {val repo=AgroOffline(AgroApi(http),local,sesion(),reloj);repo.guardar(nueva());assertFailsWith<ErrorApi.SinConexion>{repo.sincronizar()};assertTrue(local.labores(1).first().single().pendiente);assertEquals(0,descargas)}finally{http.close();d.close()}
 }
 @Test fun eliminar204NoIntentaLeerUnCuerpo()=runTest {
  val http=crearCliente(MockEngine { respond("",HttpStatusCode.NoContent) },TokenActual());try {AgroApi(http).eliminar(20)}finally{http.close()}
 }
 @Test fun filtroDeCampaniaSeCombinaConValleYBusquedaSinRed()=runTest {
  val d=driverMemoria();val local=BaseLocal(d)
  try { local.guardar(Lote(1,"AS-001","Pampa",2.0,"Papa","Mantaro",false,2.0));local.guardar(Lote(2,"AS-002","Pampa",2.0,"Papa","Mantaro",true,2.0));local.guardar(Lote(3,"AS-003","Pampa",2.0,"Papa","Tambo",false,2.0));assertEquals(listOf(1L),local.lotes("Mantaro","Pampa",true).first().map {it.id});assertEquals(2,local.lotes("Mantaro","",false).first().size) }finally{d.close()}
 }
 @Test fun supervisorRecibePrivilegiosYAlcanceDesdeElContrato() {
  val dto=SesionDto("token","calidad1","SUPERVISOR_CALIDAD",setOf("LOTE_LEER","LABOR_LEER","LABOR_EDITAR"),"Supervisor",4,true)
  val s=dto.dominio();assertTrue(s.edicionGlobal);assertTrue(s.puede("LABOR_EDITAR"));assertFalse(s.puede("CAMPANIA_CERRAR"));assertFalse(s.puede("LABOR_CREAR"))
 }
}

