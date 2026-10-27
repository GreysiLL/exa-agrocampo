package pe.upeu.agrosierra.data.repository
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import kotlin.io.encoding.Base64
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.*
class SesionRest(private val api: AgroApi,private val seguro: AlmacenSeguro,private val token: TokenActual,private val reloj: Reloj,private val local: BaseLocal): SesionRepository {
 private val estado=MutableStateFlow<Sesion?>(null)
 override val sesion=estado.asStateFlow()
 init { api.alNoAutorizado={ salir() } }
 override suspend fun entrar(u: String,c: String) {
  val dto=api.login(u,c);token.valor=dto.token
  try { val me=api.actual().copy(token=dto.token);local.usuario(me.usuario);seguro.guardar(Json.encodeToString(me));estado.value=me.dominio() } catch(e: Exception) { token.valor=null;throw e }
 }
 override fun recuperar() {
  val dto=runCatching { seguro.leer()?.let { Json { ignoreUnknownKeys=true }.decodeFromString<SesionDto>(it) } }.getOrNull()
  val exp=runCatching { val p=dto!!.token.split('.')[1];val txt=Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(p).decodeToString();Json.parseToJsonElement(txt).jsonObject["exp"]!!.jsonPrimitive.long }.getOrDefault(0)
  if(dto==null || exp*1000<=reloj.ahora()) { salir();return }
  local.usuario(dto.usuario);token.valor=dto.token;estado.value=dto.dominio()
 }
 override fun salir() { token.valor=null;seguro.borrar();estado.value=null }
}
