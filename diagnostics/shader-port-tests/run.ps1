$ErrorActionPreference = 'Stop'
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '../..')
$javaBin = Join-Path $env:JAVA_HOME 'bin'
$jomlJar = Get-ChildItem (Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1/org.joml/joml/1.10.5') -Recurse -Filter '*.jar' | Select-Object -First 1
$outputDir = Join-Path $PSScriptRoot 'after-classes'
New-Item -ItemType Directory -Force $outputDir | Out-Null
$glRoot = Join-Path $repoRoot 'common-shaders/src/main/java/net/irisshaders/iris/gl'
$sources = @(
    (Join-Path $PSScriptRoot 'IrisRenderSystem.java'),
    (Join-Path $PSScriptRoot 'MutableUniformRegression.java'),
    (Join-Path $glRoot 'uniform/Uniform.java'),
    (Join-Path $glRoot 'uniform/Vector2Uniform.java'),
    (Join-Path $glRoot 'uniform/Vector4ArrayUniform.java'),
    (Join-Path $glRoot 'state/ValueUpdateNotifier.java')
)
& (Join-Path $javaBin 'javac.exe') -cp $jomlJar.FullName -d $outputDir @sources
if ($LASTEXITCODE -ne 0) { throw 'Regression harness compilation failed' }
& (Join-Path $javaBin 'java.exe') -cp "$outputDir;$($jomlJar.FullName)" net.irisshaders.iris.gl.uniform.MutableUniformRegression
if ($LASTEXITCODE -ne 0) { throw 'Uniform regression failed' }
