param(
    [string]$JavaBin = 'C:\Program Files\Java\jdk-25.0.3\bin',
    [string]$Libraries = "$env:USERPROFILE\.cleanroom\relauncher\libraries",
    [string]$MinecraftJar = "$env:USERPROFILE\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.12.2-14.23.5.2864_mapped_stable_39-1.12\forge-1.12.2-14.23.5.2864_mapped_stable_39-1.12.jar"
)
$ErrorActionPreference = 'Stop'
$taskDependencies = @(
    "$Libraries\com\cleanroommc\cleanroom\0.6.13-alpha\cleanroom-0.6.13-alpha.jar",
    "$Libraries\top\outlands\foundation\0.19.11\foundation-0.19.11.jar",
    "$Libraries\org\ow2\asm\asm\9.10.1\asm-9.10.1.jar",
    "$Libraries\org\ow2\asm\asm-tree\9.10.1\asm-tree-9.10.1.jar"
)
foreach ($taskFile in ($taskDependencies + @($MinecraftJar))) {
    if (!(Test-Path -LiteralPath $taskFile)) { throw "Missing dependency: $taskFile" }
}
$taskClassPath = $taskDependencies -join ';'
# A fresh build directory avoids stale classes without deleting existing work.
$taskBuild = Join-Path $PSScriptRoot ('build\' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
$taskClasses = Join-Path $taskBuild 'classes'
$taskTests = Join-Path $taskBuild 'test-classes'
New-Item -ItemType Directory -Path $taskClasses,$taskTests -Force | Out-Null
$taskSources = @(Get-ChildItem -LiteralPath "$PSScriptRoot\src\main\java" -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName)
& "$JavaBin\javac.exe" --release 8 -encoding UTF-8 -cp $taskClassPath -d $taskClasses $taskSources
if ($LASTEXITCODE -ne 0) { throw 'Main compilation failed' }
$taskTestSources = @(Get-ChildItem -LiteralPath "$PSScriptRoot\src\test\java" -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName)
& "$JavaBin\javac.exe" --release 8 -encoding UTF-8 -cp "$taskClasses;$taskClassPath" -d $taskTests $taskTestSources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
& "$JavaBin\java.exe" -Xverify:all -cp "$taskTests;$taskClasses;$taskClassPath" dev.beanpack.memory.ResourcePathSelfTest $MinecraftJar
if ($LASTEXITCODE -ne 0) { throw 'Resource path verification failed' }
$taskJar = Join-Path $taskBuild 'beanpack-resource-path-memoryfix-1.0.jar'
& "$JavaBin\jar.exe" --create --file $taskJar --manifest "$PSScriptRoot\MANIFEST.MF" -C $taskClasses .
if ($LASTEXITCODE -ne 0) { throw 'Jar packaging failed' }
Write-Output "Built and verified: $taskJar"
