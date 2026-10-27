package pe.upeu.agrosierra.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.*
import pe.upeu.agrosierra.domain.usecase.*
import pe.upeu.agrosierra.data.local.Reloj
data class UiState(val sesion: Sesion?=null,val cargando: Boolean=false,val sincronizando: Boolean=false,val lotes: List<Lote> = emptyList(),val labores: List<Labor> = emptyList(),val seleccionado: Lote?=null,val formulario: Labor?=null,val perfil: Boolean=false,val valle: String="",val soloAbiertas: Boolean=false,val busqueda: String="",val ultima: Long?=null,val mensaje: String?=null,val error: ErrorApi?=null,val campos: Map<String,String> = emptyMap())
class AgroViewModel(private val sesiones: SesionRepository,private val repo: AgroRepository,private val sincronizar: SincronizarUseCase,private val guardar: GuardarLaborUseCase,private val observar: ObservarLotesUseCase,private val reloj: Reloj): ViewModel() {
 private val estado=MutableStateFlow(UiState());val ui=estado.asStateFlow()
 private var listado: Job?=null;private var detalle: Job?=null;private var meta: Job?=null
 init { viewModelScope.launch { sesiones.sesion.collect { s ->
  listado?.cancel();detalle?.cancel();meta?.cancel();estado.value=UiState(sesion=s)
  if(s!=null) { observarListado();meta=viewModelScope.launch { repo.ultima().collect { fecha->estado.update { it.copy(ultima=fecha) } } };sync() }
 } };sesiones.recuperar() }
 private fun observarListado() { listado?.cancel();val e=ui.value;listado=viewModelScope.launch { observar(e.valle,e.busqueda,e.soloAbiertas).collect { filas->estado.update { it.copy(lotes=filas,seleccionado=it.seleccionado?.let { l->filas.find { f->f.id==l.id } ?: l }) } } } }
 fun filtrar(valle: String=ui.value.valle,busqueda: String=ui.value.busqueda) { estado.update { it.copy(valle=valle,busqueda=busqueda) };observarListado() }
 fun soloAbiertas(valor: Boolean) { estado.update { it.copy(soloAbiertas=valor) };observarListado() }
 fun entrar(u: String,c: String) { if(u.isBlank() || c.isBlank()) { estado.update { it.copy(mensaje="Completa usuario y contraseña") };return };viewModelScope.launch { estado.update { it.copy(cargando=true,mensaje=null,error=null) };try { sesiones.entrar(u.trim(),c) }catch(e: Exception){error(e)}finally {estado.update { it.copy(cargando=false) }} } }
 fun salir() { sesiones.salir() }
 fun perfil() { estado.update { it.copy(perfil=true,mensaje=null,error=null) } }
 fun abrir(l: Lote) { detalle?.cancel();estado.update { it.copy(seleccionado=l,mensaje=null,error=null) };detalle=viewModelScope.launch { repo.labores(l.id).collect { filas->estado.update { it.copy(labores=filas) } } } }
 fun volver() { estado.update { if(it.formulario!=null)it.copy(formulario=null,campos=emptyMap(),error=null,mensaje=null) else if(it.perfil)it.copy(perfil=false) else it.copy(seleccionado=null,labores=emptyList(),mensaje=null,error=null) } }
 fun nueva() { val l=ui.value.seleccionado ?: return;estado.update { it.copy(formulario=Labor(0,l.id,TipoLabor.RIEGO,reloj.hoy(),"",0.0,"",false),campos=emptyMap(),mensaje=null,error=null) } }
 fun editar(l: Labor) { estado.update { it.copy(formulario=l,campos=emptyMap(),mensaje=null,error=null) } }
 fun guardarLabor(tipo: TipoLabor,fecha: String,cantidad: String,observacion: String,jornales: String) { val l=ui.value.formulario ?: return;viewModelScope.launch {
  estado.update { it.copy(cargando=true,campos=emptyMap(),error=null,mensaje=null) }
  try {
   val guardada=guardar(l.copy(tipo=tipo,fecha=fecha,cantidad=cantidad.toDoubleOrNull() ?: 0.0,observacion=observacion,cantidadJornales=jornales.toDoubleOrNull() ?: -1.0))
   estado.update { it.copy(formulario=guardada) }
   try { val r=sincronizar();estado.update { it.copy(mensaje="Guardado. Enviados: ${r.enviados}; recibidos: ${r.recibidos}",formulario=null) } }
   catch(e: ErrorApi.SinConexion){estado.update { it.copy(formulario=null,mensaje="Labor guardada sin conexión. Pendiente de sincronizar.") }}
   catch(e: ErrorApi.TiempoAgotado){estado.update { it.copy(formulario=null,mensaje="Labor guardada. El servidor tardó demasiado; sincroniza después.") }}
  }catch(e: Exception){error(e)}finally{estado.update { it.copy(cargando=false) }}
 } }
 fun eliminar(l: Labor) { viewModelScope.launch { try { repo.eliminar(l);estado.update { it.copy(mensaje="Eliminación pendiente de sincronizar") };sync() }catch(e: Exception){error(e)} } }
 fun cerrar() { val l=ui.value.seleccionado ?: return;viewModelScope.launch { estado.update { it.copy(cargando=true,error=null,mensaje=null) };try { repo.cerrar(l.id);estado.update { it.copy(seleccionado=l.copy(campaniaCerrada=true),mensaje="Campaña cerrada") } }catch(e: Exception){error(e)}finally{estado.update { it.copy(cargando=false) }} } }
 fun sync() { if(ui.value.sincronizando || sesiones.sesion.value==null)return;estado.update { it.copy(sincronizando=true,error=null,mensaje=null) };viewModelScope.launch { try {val r=sincronizar();estado.update { it.copy(mensaje="Sincronizado · ${r.enviados} enviados · ${r.recibidos} recibidos") }}catch(e: Exception){error(e)}finally{estado.update { it.copy(sincronizando=false) }} } }
 private fun error(e: Exception) { if(e is CancellationException)throw e;val x=e as? ErrorApi ?: ErrorApi.Servidor();estado.update { it.copy(error=x,mensaje=x.message,campos=(x as? ErrorApi.Validacion)?.campos ?: emptyMap()) };if(x is ErrorApi.NoEncontrado) { estado.update { it.copy(seleccionado=null,formulario=null) };viewModelScope.launch { runCatching { sincronizar() } } } }
 fun detener() { viewModelScope.cancel() }
}
