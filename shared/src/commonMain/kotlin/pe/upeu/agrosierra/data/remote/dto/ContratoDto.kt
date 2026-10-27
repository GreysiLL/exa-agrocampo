package pe.upeu.agrosierra.data.remote.dto
import kotlinx.serialization.Serializable
import pe.upeu.agrosierra.domain.model.*
@Serializable data class LoteDto(val id: Long,val codigo: String,val nombre: String,val hectareas: Double,val cultivo: String,val valle: String,val campaniaCerrada: Boolean,val dosisMaximaPorHectarea: Double)
@Serializable data class LaborDto(val id: Long,val loteId: Long,val tipo: String,val fecha: String,val responsable: String,val cantidad: Double,val observacion: String,val responsableId: Long=0,val creadoEn: String="")
@Serializable data class LaborPeticion(val loteId: Long,val tipo: String,val fecha: String,val cantidad: Double,val observacion: String)
@Serializable data class PaginaDto<T>(val contenido: List<T>,val pagina: Int,val tamanio: Int,val totalElementos: Long,val totalPaginas: Int,val ultima: Boolean)
@Serializable data class LoginPeticion(val usuario: String,val contrasena: String)
@Serializable data class SesionDto(val token: String="",val usuario: String,val rol: String,val privilegios: Set<String>,val nombre: String="",val id: Long=0,val edicionGlobal: Boolean=false)
@Serializable data class ErrorDto(val message: String="No se pudo completar la operación",val validationErrors: Map<String,String> = emptyMap())
fun LoteDto.dominio() = Lote(id,codigo,nombre,hectareas,cultivo,valle,campaniaCerrada,dosisMaximaPorHectarea)
fun LaborDto.dominio() = Labor(id,loteId,TipoLabor.valueOf(tipo),fecha,responsable,cantidad,observacion,false,responsableId,creadoEn)
fun SesionDto.dominio(tokenActual: String=token) = Sesion(tokenActual,usuario,rol,privilegios,nombre,id,edicionGlobal)
fun Labor.peticion() = LaborPeticion(loteId,tipo.name,fecha,cantidad,observacion)
