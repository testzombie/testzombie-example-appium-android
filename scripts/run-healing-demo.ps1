param(
    [Parameter(Mandatory=$true)][string]$Apk,
    [string]$AppiumServer = "http://127.0.0.1:4723",
    [string]$DeviceName = "Android Emulator"
)

$ErrorActionPreference = "Stop"
& mvn "-Dapp.apk=$Apk" "-Dappium.server=$AppiumServer" "-Ddevice.name=$DeviceName" "-Dtest=BaselineHealingDemoTest" test
exit $LASTEXITCODE
