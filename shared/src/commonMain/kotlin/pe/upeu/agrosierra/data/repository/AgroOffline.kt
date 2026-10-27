package pe.upeu.agrosierra.data.repository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.*
import kotlin.uuid.Uuid
class AgroOffline(private val api: AgroApi,private val local: BaseLocal,private val sesion: SesionRepository,private val reloj: Reloj): AgroRepository {
 private val mutex=Mutex()
 override fun lotes(valle: String,busqueda: String,soloAbiertas: Boolean)=local.lotes(valle,busqueda,soloAbiertas)
 override fun labores(id: Long)=local.labores(id)
 override fun conteos()=local.conteos()
 override fun ultima()=local.ultima()
 private fun permiso(p: String): Sesion { val s=sesion.sesion.value ?: throw ErrorApi.NoAutorizado();if(!s.puede(p))throw ErrorApi.SinPrivilegio("No tienes autorización para esta operación");return s }
 override suspend fun guardar(l: Labor): Labor = mutex.withLock {
  val s=permiso(if(l.id==0L)"LABOR_CREAR" else "LABOR_EDITAR")
  val campos=mutableMapOf<String,String>()
  if(l.cantidad<=0 || !l.cantidad.isFinite())campos["cantidad"]="Ingresa una cantidad mayor que cero"
  if(!Regex("""\d{4}-\d{2}-\d{2}""").matches(l.fecha))campos["fecha"]="Usa el formato AAAA-MM-DD"
  if(!l.cantidadJornales.isFinite() || l.cantidadJornales<0)campos["cantidadJornales"]="Los jornales no pueden ser negativos"
  if(l.observacion.length>500)campos["observacion"]="Máximo 500 caracteres"
  if(campos.isNotEmpty())throw ErrorApi.Validacion(campos,"Revisa los campos")
  val nuevo=l.copy(id=if(l.id==0L)minOf(-1L,(local.q.siguienteIdLocal().executeAsOne() ?: 0L)-1) else l.id,pendiente=true,responsable=if(l.id==0L)s.nombre else l.responsable,responsableId=if(l.id==0L)s.id else l.responsableId)
  local.db.transaction { local.guardar(nuevo);val cola=local.q.pendientes().executeAsList().find { it.laborId==nuevo.id };local.q.guardarEnvio(nuevo.id,if(nuevo.id<0)"POST" else "PUT",cola?.clave ?: Uuid.random().toString()) }
  nuevo
 }
 override suspend fun eliminar(l: Labor) = mutex.withLock {
  permiso("LABOR_ELIMINAR")
  local.db.transaction { if(l.id<0) {local.q.borrarEnvio(l.id);local.q.borrarLabor(l.id)} else {local.guardar(l.copy(pendiente=true));local.q.guardarEnvio(l.id,"DELETE",Uuid.random().toString())} }
 }
 override suspend fun sincronizar(): ResumenSync=mutex.withLock {
  if(sesion.sesion.value==null)throw ErrorApi.NoAutorizado()
  api.actual()
  var enviados=0
  for(e in local.q.pendientes().executeAsList()) {
   val l=local.dominio(local.q.obtenerLabor(e.laborId).executeAsOne())
   val remoto=when(e.operacion) {
    "POST"->api.crear(l.peticion(),e.clave).dominio()
    "PUT"->api.editar(l.id,l.peticion()).dominio()
    else->{try{api.eliminar(l.id)}catch(x: ErrorApi.NoEncontrado){ /* Borrado ya confirmado por el estado del servidor. */ };null}
   }
   local.db.transaction { local.q.borrarEnvio(l.id);local.q.borrarLabor(l.id);remoto?.let(local::guardar) };enviados++
  }
  val lotes=mutableListOf<Lote>();var p=0
  do { val pagina=api.lotes(p++);lotes+=pagina.contenido.map { it.dominio() } } while(!pagina.ultima)
  val labores=mutableListOf<Labor>()
  for(l in lotes) { p=0;do { val pagina=api.labores(l.id,p++);labores+=pagina.contenido.map { it.dominio() } } while(!pagina.ultima) }
  local.db.transaction { local.q.borrarLotes();lotes.forEach(local::guardar);local.q.borrarSincronizadas();labores.forEach(local::guardar);local.q.guardarMeta("ultima",reloj.ahora().toString()) }
  ResumenSync(enviados,lotes.size+labores.size)
 }
 override suspend fun cerrar(id: Long) { permiso("CAMPANIA_CERRAR");mutex.withLock { local.guardar(api.cerrar(id).dominio()) } }
}
