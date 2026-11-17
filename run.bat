@echo off
cd /d "%~dp0"
if not exist OSSimulator.jar (
    echo OSSimulator.jar not found. Running build.ps1...
    powershell -ExecutionPolicy Bypass -File build.ps1
)
java -jar OSSimulator.jar
