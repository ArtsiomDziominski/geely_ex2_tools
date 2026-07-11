# Builds system flavor and resigns with AOSP platform testkey
# (same cert as CentralEXAuto-geely-platform-signed.apk).
# Usage: .\scripts\build-system-apk.ps1
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root

$keysDir = Join-Path $root "keys\aosp-platform"
$pk8 = Join-Path $keysDir "platform.pk8"
$pem = Join-Path $keysDir "platform.x509.pem"
if (-not (Test-Path $pk8) -or -not (Test-Path $pem)) {
    throw "Missing AOSP platform keys in keys\aosp-platform (platform.pk8 / platform.x509.pem)"
}

Write-Host "==> assembleSystemRelease"
& .\gradlew.bat :app:assembleSystemRelease --quiet
if ($LASTEXITCODE -ne 0) { throw "Gradle assembleSystemRelease failed" }

$built = Get-ChildItem "app\build\outputs\apk\system\release\*.apk" |
    Where-Object { $_.Name -notlike "*-unsigned*" } |
    Select-Object -First 1
if (-not $built) {
    $built = Get-ChildItem "app\build\outputs\apk\system\release\*.apk" | Select-Object -First 1
}
if (-not $built) { throw "No system release APK found" }

$outDir = Join-Path $root "install\out"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$signed = Join-Path $outDir "geely-ex2-tools-system-platform-signed.apk"

$sdk = $env:ANDROID_HOME
if (-not $sdk) { $sdk = "$env:LOCALAPPDATA\Android\Sdk" }
$apksigner = Get-ChildItem "$sdk\build-tools" -Recurse -Filter "apksigner.bat" |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName
$zipalign = Get-ChildItem "$sdk\build-tools" -Recurse -Filter "zipalign.exe" |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $apksigner) { throw "apksigner.bat not found under $sdk\build-tools" }

$aligned = Join-Path $outDir "geely-ex2-tools-system-aligned.apk"
if (Test-Path $aligned) { Remove-Item $aligned -Force }
if (Test-Path $signed) { Remove-Item $signed -Force }

# Strip existing signatures so platform cert is the only signer
$tmpUnsigned = Join-Path $outDir "geely-ex2-tools-system-stripped.apk"
Add-Type -AssemblyName System.IO.Compression.FileSystem
if (Test-Path $tmpUnsigned) { Remove-Item $tmpUnsigned -Force }
[IO.Compression.ZipFile]::OpenRead($built.FullName).Dispose()
Copy-Item $built.FullName $tmpUnsigned -Force
$zip = [IO.Compression.ZipFile]::Open($tmpUnsigned, [IO.Compression.ZipArchiveMode]::Update)
$toRemove = @($zip.Entries | Where-Object { $_.FullName -like "META-INF/*" })
foreach ($e in $toRemove) { $e.Delete() }
$zip.Dispose()

if ($zipalign) {
    & $zipalign -f -p 4 $tmpUnsigned $aligned
    if ($LASTEXITCODE -ne 0) { throw "zipalign failed" }
} else {
    Copy-Item $tmpUnsigned $aligned -Force
}

& $apksigner sign `
    --key $pk8 `
    --cert $pem `
    --v1-signing-enabled true `
    --v2-signing-enabled true `
    --out $signed `
    $aligned
if ($LASTEXITCODE -ne 0) { throw "apksigner sign failed" }

# apksigner writes warnings to stderr; don't treat as terminating errors
$ErrorActionPreference = "Continue"
cmd /c "`"$apksigner`" verify --print-certs `"$signed`"" | Select-String "Signer #1 certificate"
$ErrorActionPreference = "Stop"
Write-Host ""
Write-Host "OK: $signed"
Write-Host "Install (replace user build first if needed):"
Write-Host "  adb uninstall com.geely.ex2.tools"
Write-Host "  adb install -r `"$signed`""
Write-Host "Or as priv-app + push install\privapp-permissions-com.geely.ex2.tools.xml"
