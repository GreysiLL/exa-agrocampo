package pe.upeu.agrosierra
import android.os.Bundle
import android.net.*
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pe.upeu.agrosierra.presentation.*
import org.koin.android.ext.android.inject
class MainActivity: ComponentActivity() {
 private val vm: AgroViewModel by inject()
 private lateinit var conectividad: ConnectivityManager
 private val callback=object: ConnectivityManager.NetworkCallback() { override fun onCapabilitiesChanged(n: Network,c: NetworkCapabilities) { if(c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))runOnUiThread { vm.sync() } } }
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);enableEdgeToEdge();conectividad=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;conectividad.registerDefaultNetworkCallback(callback);setContent { AgroApp(vm) } }
 override fun onDestroy() { conectividad.unregisterNetworkCallback(callback);if(isFinishing)vm.detener();super.onDestroy() }
}
