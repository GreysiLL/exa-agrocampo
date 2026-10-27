package pe.upeu.agrosierra
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import pe.upeu.agrosierra.data.local.AlmacenSeguro
@Suppress("DEPRECATION")
class AlmacenSeguroAndroid(contexto: Context): AlmacenSeguro {
 private val key=MasterKey.Builder(contexto).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
 private val prefs=EncryptedSharedPreferences.create(contexto,"sesion_segura",key,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
 override fun leer()=prefs.getString("sesion",null)
 override fun guardar(valor: String) { check(prefs.edit().putString("sesion",valor).commit()) }
 override fun borrar() { check(prefs.edit().clear().commit()) }
}
