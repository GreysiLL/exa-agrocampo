package pe.upeu.agrosierra.data.remote
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.*
import io.ktor.client.network.sockets.*
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.domain.model.*
class AgroApi(val http: HttpClient) {
 var alNoAutorizado: ()->Unit = {}
 suspend inline fun <reified T> leer(respuesta: HttpResponse): T { revisar(respuesta);return respuesta.body() }
 suspend fun revisar(r: HttpResponse) { if(r.status.value==401) alNoAutorizado();if(r.status.value>=400)throw traducirError(r.status.value,runCatching { r.body<ErrorDto>() }.getOrDefault(ErrorDto())) }
 suspend fun <T> red(bloque: suspend ()->T): T = try { bloque() } catch(e: CancellationException){throw e} catch(e: ErrorApi){throw e} catch(e: HttpRequestTimeoutException){throw ErrorApi.TiempoAgotado()} catch(e: ConnectTimeoutException){throw ErrorApi.TiempoAgotado()} catch(e: SocketTimeoutException){throw ErrorApi.TiempoAgotado()} catch(e: IOException){throw ErrorApi.SinConexion()}
 suspend fun login(u: String,c: String): SesionDto=red { val r=http.post("auth/login") { setBody(LoginPeticion(u,c)) };if(r.status.value==401)throw ErrorApi.NoAutorizado("Usuario o contraseña incorrectos");leer(r) }
 suspend fun actual(): SesionDto=red { leer(http.get("usuarios/me")) }
 suspend fun lotes(p: Int): PaginaDto<LoteDto> = red { leer(http.get("lotes") { parameter("pagina",p);parameter("tamanio",20) }) }
 suspend fun labores(id: Long,p: Int): PaginaDto<LaborDto> = red { leer(http.get("lotes/$id/labores") { parameter("pagina",p);parameter("tamanio",20) }) }
 suspend fun crear(l: LaborPeticion,clave: String): LaborDto=red { leer(http.post("labores") { header("Idempotency-Key",clave);setBody(l) }) }
 suspend fun editar(id: Long,l: LaborPeticion): LaborDto=red { leer(http.put("labores/$id") { setBody(l) }) }
 suspend fun eliminar(id: Long) = red { revisar(http.delete("labores/$id"));Unit }
 suspend fun cerrar(id: Long): LoteDto=red { leer(http.post("lotes/$id/cerrar-campania")) }
}
