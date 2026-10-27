package pe.upeu.agrosierra.domain.model
sealed class ErrorApi(override val message: String): Exception(message) {
 class Validacion(val campos: Map<String,String>,m: String): ErrorApi(m)
 class NoAutorizado(m: String="Tu sesión terminó. Inicia sesión nuevamente."): ErrorApi(m)
 class SinPrivilegio(m: String): ErrorApi(m)
 class NoEncontrado(m: String): ErrorApi(m)
 class Conflicto(m: String): ErrorApi(m)
 class Servidor(m: String="El servicio no pudo completar la operación."): ErrorApi(m)
 class SinConexion: ErrorApi("Sin conexión. Los cambios quedan guardados en este dispositivo.")
 class TiempoAgotado: ErrorApi("El servicio tardó demasiado. Puedes reintentar.")
}
