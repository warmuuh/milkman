@echo off
setlocal

rem Per-user, absolute cache path so the AOT cache survives relaunches from any CWD.
if not exist "%LOCALAPPDATA%\Milkman" mkdir "%LOCALAPPDATA%\Milkman"
set "AOT_CACHE=%LOCALAPPDATA%\Milkman\milkman.aot"
set "AOT_CONF=%LOCALAPPDATA%\Milkman\milkman.aot.config"

rem Invalidate the cache when the shipped milkman.jar changes.
if exist "%AOT_CACHE%" (
  for %%A in ("%AOT_CACHE%") do set "CACHE_MTIME=%%~tA"
  for %%A in ("milkman.jar") do set "JAR_MTIME=%%~tA"
  if "%JAR_MTIME%" GTR "%CACHE_MTIME%" (
    del /q "%AOT_CACHE%" "%AOT_CONF%" 2>nul
  )
)

if exist "%AOT_CACHE%" (
  set "AOT_OPT=-XX:AOTCache=%AOT_CACHE%"
) else (
  set "AOT_OPT=-XX:AOTMode=record -XX:AOTConfiguration=%AOT_CONF%"
)

set "JAVA_OPTS=-Xmx2G -XX:+UseCompressedOops ^
 --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED ^
 --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
 --add-exports javafx.controls/com.sun.javafx.scene.control.skin=ALL-UNNAMED ^
 --add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED ^
 --add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED ^
 --add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED ^
 --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED ^
 --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED ^
 --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED ^
 --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
 --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
 --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED ^
 --add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED ^
 --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
 -Dprism.dirtyopts=false"

@start /wait .\jre-win64\bin\javaw.exe %AOT_OPT% %JAVA_OPTS% -cp plugins\*;milkman.jar milkman.MilkmanApplication

if exist "%AOT_CONF%" if not exist "%AOT_CACHE%" (
  start "" /b .\jre-win64\bin\javaw.exe -XX:AOTMode=create -XX:AOTConfiguration="%AOT_CONF%" -XX:AOTCache="%AOT_CACHE%" %JAVA_OPTS% -cp plugins\*;milkman.jar milkman.MilkmanApplication
)
