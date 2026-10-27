import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
 kotlin("multiplatform"); kotlin("plugin.serialization")
 id("com.android.kotlin.multiplatform.library")
 id("org.jetbrains.compose"); id("org.jetbrains.kotlin.plugin.compose")
 id("app.cash.sqldelight")
}
kotlin {
 android { namespace="pe.upeu.agrosierra.shared";compileSdk=36;minSdk=24;compilerOptions { jvmTarget=JvmTarget.JVM_11 };withHostTest { isIncludeAndroidResources=true } }
 sourceSets {
  commonMain.dependencies {
   implementation("app.cash.sqldelight:runtime:2.1.0");implementation("app.cash.sqldelight:coroutines-extensions:2.1.0")
   implementation("io.ktor:ktor-client-core:3.6.0");implementation("io.ktor:ktor-client-content-negotiation:3.6.0");implementation("io.ktor:ktor-serialization-kotlinx-json:3.6.0");implementation("io.ktor:ktor-client-logging:3.6.0")
   api("io.insert-koin:koin-core:4.2.2");implementation("io.insert-koin:koin-compose:4.2.2")
   implementation("org.jetbrains.compose.runtime:runtime:1.11.1");implementation("org.jetbrains.compose.foundation:foundation:1.11.1");implementation("org.jetbrains.compose.material3:material3:1.11.0-alpha07")
   api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.11.0-beta01")
  }
  commonTest.dependencies { implementation(kotlin("test"));implementation("io.ktor:ktor-client-mock:3.6.0");implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2") }
  getByName("androidHostTest").dependencies { implementation("app.cash.sqldelight:sqlite-driver:2.1.0") }
 }
}
sqldelight { databases { create("AgroDb") { packageName.set("pe.upeu.agrosierra.db") } } }
tasks.withType<Test>().configureEach { testLogging { events("passed","failed","skipped") } }
