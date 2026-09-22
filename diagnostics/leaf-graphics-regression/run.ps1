param(
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.11',
    [string]$ProductionJar
)
$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$cacheRoot = Join-Path $env:USERPROFILE '.gradle/caches'
$minecraft = Get-ChildItem (Join-Path $cacheRoot 'unimined') -Recurse -Filter '*fixForge-mcp+searge-at+a716df9.jar' | Select-Object -First 1
if (!$minecraft) { throw 'Cached Minecraft 1.12.2 MCP/Forge jar was not found' }
$asm = foreach ($artifact in @('asm', 'asm-tree', 'asm-commons')) {
    $jar = Get-ChildItem (Join-Path $cacheRoot "modules-2/files-2.1/org.ow2.asm/$artifact/9.7") -Recurse -Filter '*.jar' | Select-Object -First 1
    if (!$jar) { throw "Missing cached ASM dependency: $artifact 9.7" }
    $jar.FullName
}
if (!$ProductionJar) { $ProductionJar = Join-Path $repoRoot 'build/libs/2.4.2-dev/pintonium-forge-1.12.2-2.4.2-dev.jar' }
$dev = Join-Path $repoRoot 'forge122/versions/1.12.2/build/classes/java/main'
$classes = Join-Path $PSScriptRoot 'classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$classpath = $asm -join ';'
& (Join-Path $JavaHome 'bin/javac.exe') -cp $classpath -d $classes (Join-Path $PSScriptRoot 'LeafGraphicsCases.java') (Join-Path $PSScriptRoot 'LeafGraphicsRegression.java')
if ($LASTEXITCODE -ne 0) { throw 'Regression harness compilation failed' }
& (Join-Path $JavaHome 'bin/java.exe') -cp "$classes;$classpath" LeafGraphicsRegression $classes $minecraft.FullName $dev $ProductionJar
if ($LASTEXITCODE -ne 0) { throw 'Leaf graphics regression failed' }
