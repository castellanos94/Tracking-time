$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.7.6-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$ErrorActionPreference = "Stop"

Write-Host "Building Time Tracker..." -ForegroundColor Cyan

try {
    # Clean and compile
    Write-Host "Compiling..."
    mvn clean compile
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed" }

    # Package using Maven Shade Plugin for Fat Jar
    Write-Host "Packaging (Fat Jar)..."
    mvn package
    if ($LASTEXITCODE -ne 0) { throw "Packaging failed" }
    
    Write-Host "Build Success! Fat Jar should be at target/tracking-time-1.0-SNAPSHOT.jar" -ForegroundColor Green
}
catch {
    Write-Host "Build Failed: $_" -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit 1
}
