package pe.upeu.agrosierra.data.local
interface AlmacenSeguro { fun leer(): String?;fun guardar(valor: String);fun borrar() }
interface Reloj { fun ahora(): Long;fun hoy(): String }
