package pe.upeu.agrosierra
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.upeu.agrosierra.db.AgroDb
fun baseLocalAndroid(contexto: Context)=AndroidSqliteDriver(AgroDb.Schema,contexto,"agrocampo.db")
