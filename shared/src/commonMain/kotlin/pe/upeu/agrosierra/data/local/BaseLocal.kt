package pe.upeu.agrosierra.data.local
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import pe.upeu.agrosierra.db.AgroDb
import pe.upeu.agrosierra.domain.model.*
class BaseLocal(driver: SqlDriver) {
 val db=AgroDb(driver);val q=db.agroDbQueries
 fun lotes(valle: String,busqueda: String): Flow<List<Lote>> = q.observarLotes(valle,busqueda).asFlow().mapToList(Dispatchers.Default).map { filas->filas.map { Lote(it.id,it.codigo,it.nombre,it.hectareas,it.cultivo,it.valle,it.campaniaCerrada==1L,it.dosisMaxima) } }
 fun labores(id: Long): Flow<List<Labor>> = q.observarLaboresPorLote(id).asFlow().mapToList(Dispatchers.Default).map { it.map(::dominio) }
 fun dominio(l: pe.upeu.agrosierra.Labor)=Labor(l.id,l.loteId,TipoLabor.valueOf(l.tipo),l.fecha,l.responsable,l.cantidad,l.observacion,l.pendiente==1L,l.responsableId,l.creadoEn)
 fun guardar(l: Labor) { q.guardarLabor(l.id,l.loteId,l.tipo.name,l.fecha,l.responsable,l.cantidad,l.observacion,if(l.pendiente)1L else 0L,l.responsableId,l.creadoEn) }
 fun guardar(l: Lote) { q.guardarLote(l.id,l.codigo,l.nombre,l.hectareas,l.cultivo,l.valle,if(l.campaniaCerrada)1L else 0L,l.dosisMaximaPorHectarea) }
 fun usuario(nombre: String) { if(q.meta("usuario").executeAsOneOrNull()!=nombre) db.transaction { q.borrarCola();q.borrarTodo();q.borrarLotes();q.borrarMeta();q.guardarMeta("usuario",nombre) } }
 fun ultima() = q.meta("ultima").asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.toLongOrNull() }
}

