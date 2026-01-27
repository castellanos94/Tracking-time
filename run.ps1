$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.7.6-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$ErrorActionPreference = "Stop"

try {
    Write-Host "Using JDK at: $env:JAVA_HOME"
    mvn clean javafx:run
}
catch {
    Write-Host "Error occurred: $_" -ForegroundColor Red
    Read-Host "Press Enter to exit..."
}
