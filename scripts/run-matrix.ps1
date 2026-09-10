param(
    [Parameter(Mandatory=$true)][string]$Apk,
    [string]$AppiumServer = "http://127.0.0.1:4723",
    [string]$DeviceName = "Android Emulator"
)

$ErrorActionPreference = "Stop"
$mvnArgs = @(
    "-Dapp.apk=$Apk",
    "-Dappium.server=$AppiumServer",
    "-Ddevice.name=$DeviceName"
)

Write-Host "Kontrollmatrix: alle Level mit passenden Locators" -ForegroundColor Cyan
& mvn @mvnArgs "-Dtest=MutationLevelMatrixTest" test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
