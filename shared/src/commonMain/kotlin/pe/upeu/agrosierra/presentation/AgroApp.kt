package pe.upeu.agrosierra.presentation
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import pe.upeu.agrosierra.domain.model.*
@Composable fun AgroApp(vm: AgroViewModel=koinInject()) {
 val e by vm.ui.collectAsState()
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF9C4D27),secondary=Color(0xFF31565B),background=Color(0xFFF7F4ED),surface=Color(0xFFFFFCF5))) {
 Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
  Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=20.dp)) {
   Spacer(Modifier.height(16.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
    Column { Text("AGROSIERRA",fontSize=11.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary);Text("AgroCampo",fontSize=27.sp,fontWeight=FontWeight.Bold) }
    if(e.sesion!=null)TextButton(onClick=vm::perfil) { Text("Mi perfil") }
   }
   Text("El trabajo de campo, al día.",color=Color(0xFF66645E),fontSize=13.sp)
   Spacer(Modifier.height(20.dp))
   if(e.cargando || e.sincronizando)LinearProgressIndicator(Modifier.fillMaxWidth())
   e.mensaje?.let { mensaje -> Card(Modifier.fillMaxWidth().padding(vertical=8.dp),colors=CardDefaults.cardColors(containerColor=if(e.error==null)Color(0xFFE9EFE8) else Color(0xFFFCEAE4))) { Column(Modifier.padding(12.dp)) { Text(mensaje,fontSize=13.sp);if(e.error is ErrorApi.Servidor || e.error is ErrorApi.TiempoAgotado)TextButton(onClick=vm::sync){Text("Reintentar")} } } }
   when {
    e.sesion==null->Login(e,vm)
    e.formulario!=null->Formulario(e,vm)
    e.perfil->Perfil(e.sesion!!,vm)
    e.seleccionado!=null->Detalle(e,vm)
    else->Listado(e,vm)
   }
  }
 }
 }
}
@Composable private fun Login(e: UiState,vm: AgroViewModel) {
 var usuario by remember { mutableStateOf("") };var clave by remember { mutableStateOf("") }
 Column(Modifier.fillMaxWidth().padding(top=35.dp)) {
  Text("Bienvenido al campo",fontSize=24.sp,fontWeight=FontWeight.SemiBold)
  Text("Ingresa con tu cuenta de la cooperativa.",Modifier.padding(top=8.dp,bottom=24.dp))
  OutlinedTextField(usuario,{usuario=it},label={Text("Usuario")},singleLine=true,modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(12.dp));OutlinedTextField(clave,{clave=it},label={Text("Contraseña")},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(24.dp));Button({vm.entrar(usuario,clave)},enabled=!e.cargando,modifier=Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(12.dp)){Text("Ingresar")}
  Text("Tus lotes y labores, también sin señal después de sincronizar.",Modifier.padding(top=28.dp),fontSize=13.sp,color=Color.Gray)
 }
}
@Composable private fun Listado(e: UiState,vm: AgroViewModel) {
 Text("Padrón de lotes",fontSize=23.sp,fontWeight=FontWeight.SemiBold)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
  Text(if(e.ultima==null)"Nunca sincronizado" else "Última sincronización guardada",fontSize=11.sp)
  TextButton(vm::sync,enabled=!e.sincronizando){Text("Sincronizar")}
 }
 OutlinedTextField(e.busqueda,{vm.filtrar(busqueda=it)},label={Text("Buscar nombre o código")},singleLine=true,modifier=Modifier.fillMaxWidth())
 var menu by remember { mutableStateOf(false) }
 Box { TextButton({menu=true}) { Text("Valle: ${e.valle.ifBlank { "Todos" }} ▾") };DropdownMenu(menu,{menu=false}) { listOf("","Mantaro","Sagrado","Tambo").forEach { valle->DropdownMenuItem(text={Text(valle.ifBlank {"Todos"})},onClick={menu=false;vm.filtrar(valle=valle)}) } } }
 Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(e.soloAbiertas,vm::soloAbiertas);Text("Solo campa�as abiertas",fontSize=13.sp) }
 if(e.lotes.isEmpty() && !e.sincronizando)Text("No hay lotes para mostrar. Ajusta los filtros o sincroniza con conexión.",Modifier.padding(vertical=24.dp))
 LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=20.dp)) { items(e.lotes,key={it.id}) { l ->
  Card(onClick={vm.abrir(l)},shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) { Column(Modifier.fillMaxWidth().padding(16.dp)) {
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(l.codigo,color=MaterialTheme.colorScheme.primary,fontSize=12.sp,fontWeight=FontWeight.Bold);Text(if(l.campaniaCerrada)"CERRADA" else "ABIERTA",fontSize=11.sp,color=if(l.campaniaCerrada)Color.Gray else MaterialTheme.colorScheme.secondary)}
   Text(l.nombre,fontSize=20.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(vertical=6.dp));Text("${l.cultivo} · ${l.hectareas} ha · Valle ${l.valle}",fontSize=13.sp)
  } }
 } }
}
@Composable private fun Detalle(e: UiState,vm: AgroViewModel) {
 val l=e.seleccionado!!;val s=e.sesion!!
 TextButton(vm::volver){Text("‹ Volver a lotes")}
 Text(l.nombre,fontSize=24.sp,fontWeight=FontWeight.Bold);Text("${l.codigo} · ${l.cultivo} · ${l.hectareas} ha",fontSize=13.sp)
 Text("Valle ${l.valle} · ${if(l.campaniaCerrada)"Campaña cerrada" else "Campaña abierta"}",Modifier.padding(vertical=6.dp),fontSize=13.sp)
 Text("Dosis máxima: ${l.dosisMaximaPorHectarea} por ha",fontSize=12.sp)
 Row {
  if(s.puede("LABOR_CREAR") && !l.campaniaCerrada)Button(vm::nueva,modifier=Modifier.padding(vertical=12.dp)){Text("Registrar labor")}
  if(s.puede("CAMPANIA_CERRAR") && !l.campaniaCerrada)TextButton(vm::cerrar,enabled=!e.cargando){Text("Cerrar campaña")}
 }
 Text("Labores",fontWeight=FontWeight.Bold,fontSize=18.sp,modifier=Modifier.padding(bottom=8.dp))
 if(e.labores.isEmpty())Text("Este lote todavía no tiene labores guardadas.")
 LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=18.dp)) { items(e.labores,key={it.id}) { labor->
  Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) { Column(Modifier.fillMaxWidth().padding(14.dp)) {
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(labor.tipo.name,fontWeight=FontWeight.Bold);Text(labor.fecha,fontSize=12.sp)}
   Text("${labor.responsable} · Cantidad: ${labor.cantidad}",fontSize=13.sp,modifier=Modifier.padding(vertical=6.dp))
   Text("Jornales: ${labor.cantidadJornales}",fontSize=13.sp)
   if(labor.observacion.isNotBlank())Text(labor.observacion,fontSize=13.sp)
   Text(if(labor.pendiente)"Pendiente de sincronizar · ID ${labor.id}" else "Sincronizada · ID ${labor.id}",fontSize=11.sp,color=if(labor.pendiente)MaterialTheme.colorScheme.primary else Color.Gray,modifier=Modifier.padding(top=8.dp))
   Row {
    if(s.puede("LABOR_EDITAR") && (s.edicionGlobal || labor.responsableId==s.id) && !l.campaniaCerrada)TextButton({vm.editar(labor)}){Text("Corregir")}
    if(s.puede("LABOR_ELIMINAR"))TextButton({vm.eliminar(labor)}){Text("Eliminar")}
   }
  } }
 } }
}
@Composable private fun Formulario(e: UiState,vm: AgroViewModel) {
 val l=e.formulario!!
 var tipo by remember(l.id) { mutableStateOf(l.tipo) };var fecha by remember(l.id) { mutableStateOf(l.fecha) };var cantidad by remember(l.id) { mutableStateOf(if(l.cantidad==0.0)"" else l.cantidad.toString()) };var nota by remember(l.id) { mutableStateOf(l.observacion) }
 Column(Modifier.verticalScroll(rememberScrollState())) {
  TextButton(vm::volver){Text("‹ Volver al lote")};Text(if(l.id==0L)"Registrar labor" else "Corregir labor",fontSize=24.sp,fontWeight=FontWeight.Bold)
  Text("Se guardará en el dispositivo y se enviará al sincronizar.",fontSize=12.sp,modifier=Modifier.padding(vertical=10.dp))
  var menu by remember { mutableStateOf(false) };Box { OutlinedButton({menu=true}) { Text("Tipo: ${tipo.name} ▾") };DropdownMenu(menu,{menu=false}){TipoLabor.entries.forEach { t->DropdownMenuItem(text={Text(t.name)},onClick={tipo=t;menu=false}) }} }
  Campo(fecha,{fecha=it},"Fecha (AAAA-MM-DD)",e.campos["fecha"])
  Campo(cantidad,{cantidad=it},"Cantidad aplicada",e.campos["cantidad"])
  Campo(jornales,{jornales=it},"Cantidad de jornales",e.campos["cantidadJornales"])
  Campo(nota,{nota=it},"Observación",e.campos["observacion"])
  Spacer(Modifier.height(20.dp));Button({vm.guardarLabor(tipo,fecha,cantidad,nota,jornales)},enabled=!e.cargando,modifier=Modifier.fillMaxWidth().height(50.dp)){Text("Guardar labor")};Spacer(Modifier.height(20.dp))
 }
}
@Composable private fun Campo(v: String,c: (String)->Unit,n: String,error: String?) { OutlinedTextField(v,c,label={Text(n)},isError=error!=null,supportingText={if(error!=null)Text(error)},modifier=Modifier.fillMaxWidth().padding(top=8.dp)) }
@Composable private fun Perfil(s: Sesion,vm: AgroViewModel) {
 Column(Modifier.verticalScroll(rememberScrollState())) {
  TextButton(vm::volver){Text("‹ Volver")};Text(s.nombre,fontSize=24.sp,fontWeight=FontWeight.Bold);Text("${s.usuario} · ${s.rol}",Modifier.padding(vertical=12.dp));HorizontalDivider();Text("Privilegios",fontSize=18.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=16.dp));s.privilegios.sorted().forEach { Text("• $it",Modifier.padding(vertical=6.dp)) };Spacer(Modifier.height(24.dp));OutlinedButton(vm::salir,modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")}
 }
}
