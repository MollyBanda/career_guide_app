[CmdletBinding()]
param(
    [string]$Serial,
    [string]$AdbPath = (Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe')
)

$ErrorActionPreference = 'Stop'
$packageName = 'com.pathfinder.app'
$activityName = "$packageName/.MainActivity"
$evidenceDirectory = Join-Path $PSScriptRoot '..\evidence'
New-Item -ItemType Directory -Force -Path $evidenceDirectory | Out-Null

if (-not (Test-Path -LiteralPath $AdbPath)) {
    throw "ADB was not found at '$AdbPath'. Pass -AdbPath or install Android platform tools."
}

$deviceLines = @(& $AdbPath devices -l | Select-Object -Skip 1 | Where-Object { $_ -match '\S+\s+device\b' })
if ($Serial) {
    $deviceLines = @($deviceLines | Where-Object { ($_ -split '\s+')[0] -eq $Serial })
}
if ($deviceLines.Count -ne 1) {
    throw "Expected exactly one authorized device. Found $($deviceLines.Count). Connect/unlock one device or pass -Serial."
}
$Serial = ($deviceLines[0] -split '\s+')[0]

function Invoke-DeviceAdb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)
    $result = & $AdbPath -s $Serial @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "ADB failed ($LASTEXITCODE): $($Arguments -join ' ')`n$($result | Out-String)"
    }
    return ($result | Out-String).Trim()
}

$model = Invoke-DeviceAdb shell getprop ro.product.model
$androidVersion = Invoke-DeviceAdb shell getprop ro.build.version.release
$androidApi = Invoke-DeviceAdb shell getprop ro.build.version.sdk
$packagePath = Invoke-DeviceAdb shell pm path $packageName
if ($packagePath -notmatch 'package:') {
    throw "Package '$packageName' is not installed on $Serial. Install the debug APK first."
}

$launchOutput = Invoke-DeviceAdb -Arguments @('shell', 'am', 'start', '-W', '-n', $activityName)
if ($launchOutput -notmatch 'Status:\s*ok') {
    throw "Activity launch did not return Status: ok.`n$launchOutput"
}
Start-Sleep -Seconds 2

$activityState = Invoke-DeviceAdb shell dumpsys activity activities
if ($activityState -notmatch 'topResumedActivity=.*com\.pathfinder\.app/\.MainActivity') {
    throw 'MainActivity is not the resumed activity after launch.'
}

$remoteHierarchy = '/sdcard/codex-pathwise-window.xml'
$null = Invoke-DeviceAdb shell uiautomator dump $remoteHierarchy
$hierarchyPath = Join-Path $evidenceDirectory 'CG_SYS_001_launch_hierarchy.xml'
$null = Invoke-DeviceAdb pull $remoteHierarchy $hierarchyPath
$hierarchy = Get-Content -LiteralPath $hierarchyPath -Raw
if ($hierarchy -notmatch 'package="com\.pathfinder\.app"') {
    throw 'The UI hierarchy does not contain the Pathwise application window.'
}

$screenshotPath = Join-Path $evidenceDirectory 'CG_SYS_001_launch.png'
& $AdbPath -s $Serial exec-out screencap -p > $screenshotPath
if ($LASTEXITCODE -ne 0 -or (Get-Item -LiteralPath $screenshotPath).Length -lt 10000) {
    throw 'Could not capture a valid device screenshot.'
}

$summaryPath = Join-Path $evidenceDirectory 'CG_SYS_001_execution.txt'
$summary = @(
    'Test Case ID: CG_SYS_001 (additional launch smoke test)'
    'Status: PASS'
    'Expected: MainActivity opens in the foreground and displays the Pathwise app window.'
    'Actual: ADB returned Status: ok; MainActivity was resumed; UI hierarchy contains com.pathfinder.app.'
    "Device serial: $Serial"
    "Device model: $model"
    "Android version/API: $androidVersion / $androidApi"
    "Package: $packageName"
    'Build variant under test: debug package already installed on device; this script does not install or clear app data. Normal app startup may seed the default catalogue if the database is empty.'
    "Screenshot: $screenshotPath"
    "UI hierarchy: $hierarchyPath"
) -join [Environment]::NewLine
Set-Content -LiteralPath $summaryPath -Value $summary -Encoding utf8

Write-Output $summary
