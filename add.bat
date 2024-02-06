@echo off

set URL=http://192.168.8.69:8080/passGate/a/0
set REQUESTS=10000
set DELAY=500

echo Sending %REQUESTS% requests to %URL%...

for /l %%i in (1, 1, %REQUESTS%) do (
    curl -s -o nul %URL%
    ping -n 2 127.0.0.1 >nul
)

echo All requests sent.
