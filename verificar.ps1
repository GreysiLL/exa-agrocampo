$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$logAgro=Join-Path $PSScriptRoot 'verificacion-ultima.log'
$ErrorActionPreference='Continue'
& .\gradlew.bat :androidApp:assembleDebug :shared:testAndroidHostTest --no-daemon --console=plain '-Pkotlin.compiler.execution.strategy=in-process' '-Pkotlin.incremental=false' 2>&1 | Tee-Object -FilePath $logAgro
$codigoAgro=$LASTEXITCODE
$ErrorActionPreference='Stop'
if($codigoAgro -ne 0){throw 'Fallo la verificacion de AgroCampo; resultado en verificacion-ultima.log'}
