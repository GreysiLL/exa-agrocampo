package pe.upeu.agrosierra
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import pe.upeu.agrosierra.data.local.*
val platformModule=module {
 single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
 single<SqlDriver> { baseLocalAndroid(androidContext()) }
 single<HttpClientEngine> { OkHttp.create() }
 single<Reloj> { object: Reloj {
  override fun ahora()=System.currentTimeMillis()
  override fun hoy()=java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.ROOT).format(java.util.Date())
 } }
}
