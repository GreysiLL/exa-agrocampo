package pe.upeu.agrosierra.domain.repository
import kotlinx.coroutines.flow.*
import pe.upeu.agrosierra.domain.model.*
interface SesionRepository { val sesion: StateFlow<Sesion?>;suspend fun entrar(u: String,c: String);fun salir();fun recuperar() }
interface AgroRepository {
 fun lotes(valle: String,busqueda: String,soloAbiertas: Boolean=false): Flow<List<Lote>>
 fun labores(id: Long): Flow<List<Labor>>
 fun ultima(): Flow<Long?>
 suspend fun guardar(l: Labor): Labor
 suspend fun eliminar(l: Labor)
 suspend fun sincronizar(): ResumenSync
 suspend fun cerrar(id: Long)
}
