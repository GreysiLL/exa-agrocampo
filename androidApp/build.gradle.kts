plugins { id("com.android.application");id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace="pe.upeu.agrosierra";compileSdk=36
 defaultConfig { applicationId="pe.upeu.agrosierra";minSdk=24;targetSdk=36;versionCode=1;versionName="1.0" }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_11;targetCompatibility=JavaVersion.VERSION_11 }
 buildFeatures { compose=true }
}
kotlin { compilerOptions { jvmTarget=org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11 } }
dependencies {
 implementation(project(":shared"));implementation("androidx.activity:activity-compose:1.13.0")
 implementation("app.cash.sqldelight:android-driver:2.1.0")
 implementation("io.ktor:ktor-client-okhttp:3.6.0");implementation(enforcedPlatform("com.squareup.okhttp3:okhttp-bom:5.3.2"))
 implementation("androidx.security:security-crypto:1.1.0")
 implementation("io.insert-koin:koin-android:4.2.2")
}
