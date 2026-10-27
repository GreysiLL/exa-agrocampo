package pe.upeu.agrosierra.domain.model
data class Lote(val id: Long,val codigo: String,val nombre: String,val hectareas: Double,val cultivo: String,val valle: String,val campaniaCerrada: Boolean,val dosisMaximaPorHectarea: Double)
enum class TipoLabor { RIEGO,FUMIGACION,COSECHA }
data class Labor(val id: Long,val loteId: Long,val tipo: TipoLabor,val fecha: String,val responsable: String,val cantidad: Double,val observacion: String,val pendiente: Boolean,val responsableId: Long=0,val creadoEn: String="",val cantidadJornales: Double=0.0)
data class Sesion(val token: String,val usuario: String,val rol: String,val privilegios: Set<String>,val nombre: String="",val id: Long=0,val edicionGlobal: Boolean=false) { fun puede(p: String)=p in privilegios }
data class ResumenSync(val enviados: Int,val recibidos: Int)
