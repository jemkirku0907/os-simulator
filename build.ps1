$ErrorActionPreference = "Stop"

Write-Host "1. Cleaning build directories..."
if (Test-Path "out-app") { Remove-Item -Recurse -Force "out-app" }
if (Test-Path "out-test") { Remove-Item -Recurse -Force "out-test" }
New-Item -ItemType Directory -Force -Path "out-app" | Out-Null
New-Item -ItemType Directory -Force -Path "out-test" | Out-Null

Write-Host "2. Compiling application sources..."
$srcFiles = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out-app $srcFiles

Write-Host "3. Packaging runnable JAR (OSSimulator.jar)..."
jar cfe OSSimulator.jar ossim.Main -C out-app .

Write-Host "4. Compiling and running EngineTestSuite..."
$testFiles = Get-ChildItem -Recurse -Filter *.java test | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp out-app -d out-test $testFiles
java -cp "out-app;out-test" ossim.EngineTestSuite

Write-Host "Build and verification complete! Run with: java -jar OSSimulator.jar"
