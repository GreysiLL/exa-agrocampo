package pe.upeu.agrosierra.data.remote
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
class TokenActual { var valor: String?=null }
fun crearCliente(motor: HttpClientEngine,token: TokenActual): HttpClient = HttpClient(motor) {
 expectSuccess=false
 install(ContentNegotiation) { json(Json { ignoreUnknownKeys=true;encodeDefaults=true }) }
 install(HttpTimeout) { requestTimeoutMillis=15000;connectTimeoutMillis=8000;socketTimeoutMillis=15000 }
 install(Logging) { level=LogLevel.NONE;sanitizeHeader { it.equals("Authorization",true) } }
 defaultRequest { url(ApiConfig.BASE_URL);contentType(ContentType.Application.Json);token.valor?.let { bearerAuth(it) } }
}
