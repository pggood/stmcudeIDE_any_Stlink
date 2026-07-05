@echo off
setlocal enabledelayedexpansion

set PLUGIN_DIR=D:\ST\STM32CubeIDE_1.18.1\STM32CubeIDE\plugins
set SOURCE_STLINK=D:\com.st.stm32cube.ide.mcu.debug.stlink
set SOURCE_DEBUG=D:\com.st.stm32cube.ide.mcu.debug
set OUTPUT_DIR=D:\bin
set MOD_DIR=D:\modified_plugins
REM Put PRISTINE original jars here (from a clean 1.18.1 install / backup):
REM   com.st.stm32cube.ide.mcu.debug.stlink_*.jar   and   com.st.stm32cube.ide.mcu.debug_*.jar
set MASTER_DIR=D:\plugin_masters
set NOSRC=%OUTPUT_DIR%\nosrc

echo ============================================
echo COMPILE + REPACKAGE (manifest-preserving)
echo ============================================
echo.

if not exist "%SOURCE_STLINK%" ( echo ERROR: ST-LINK source folder not found! & pause & exit /b 1 )
if not exist "%SOURCE_DEBUG%"  ( echo ERROR: Main Debug source folder not found! & pause & exit /b 1 )

mkdir %OUTPUT_DIR% 2>nul
mkdir "%MOD_DIR%" 2>nul
rmdir /s /q "%NOSRC%" 2>nul
mkdir "%NOSRC%" 2>nul

REM ---- classpath args file (explicit jar list; wildcards not expanded in @argfiles) ----
set "CPFILE=%OUTPUT_DIR%\cp.args"
if exist "%CPFILE%" del "%CPFILE%"
> "%CPFILE%" echo -classpath
echo Enumerating classpath...
for %%J in ("%PLUGIN_DIR%\*.jar") do >> "%CPFILE%" <nul set /p "=%%J;"
for /d %%D in ("%PLUGIN_DIR%\*")   do >> "%CPFILE%" <nul set /p "=%%D;"
>> "%CPFILE%" echo.
echo.

REM -proc:none      : no annotation processing
REM -implicit:none  : do not generate classes for implicitly-referenced sources
REM -sourcepath NOSRC (empty dir): NEVER pull .java off the classpath jars
set JAVAC_OPTS=-proc:none -implicit:none -sourcepath "%NOSRC%"

echo Step 1: Compiling StLinkFwUtil.java ...
javac @"%CPFILE%" %JAVAC_OPTS% -d %OUTPUT_DIR% "%SOURCE_DEBUG%\com\st\stm32cube\ide\mcu\debug\stlinkfwutil\StLinkFwUtil.java"
if errorlevel 1 goto error

echo Step 2: Compiling StLinkDebugHardware.java ...
javac @"%CPFILE%" %JAVAC_OPTS% -d %OUTPUT_DIR% "%SOURCE_STLINK%\com\st\stm32cube\ide\mcu\debug\stlink\StLinkDebugHardware.java"
if errorlevel 1 goto error

echo.
echo Step 3: Locating PRISTINE original bundle jars ...
set "SRC_LABEL=master"
set "ORIG_STLINK="
for %%J in ("%MASTER_DIR%\com.st.stm32cube.ide.mcu.debug.stlink_*.jar") do set "ORIG_STLINK=%%J"
set "ORIG_DEBUG="
for %%J in ("%MASTER_DIR%\com.st.stm32cube.ide.mcu.debug_*.jar") do set "ORIG_DEBUG=%%J"

if not defined ORIG_STLINK (
    echo   WARNING: no master debug.stlink jar in %MASTER_DIR% -- falling back to plugins ^(may be clobbered^).
    for %%J in ("%PLUGIN_DIR%\com.st.stm32cube.ide.mcu.debug.stlink_*.jar") do set "ORIG_STLINK=%%J"
    set "SRC_LABEL=plugins"
)
if not defined ORIG_DEBUG (
    echo   WARNING: no master debug jar in %MASTER_DIR% -- falling back to plugins ^(may be clobbered^).
    for %%J in ("%PLUGIN_DIR%\com.st.stm32cube.ide.mcu.debug_*.jar") do set "ORIG_DEBUG=%%J"
    set "SRC_LABEL=plugins"
)
if not defined ORIG_STLINK ( echo ERROR: debug.stlink_*.jar not found anywhere! & pause & exit /b 1 )
if not defined ORIG_DEBUG  ( echo ERROR: debug_*.jar not found anywhere!        & pause & exit /b 1 )
echo   Clone source: !SRC_LABEL!
echo   ST-LINK jar:  !ORIG_STLINK!
echo   Debug jar:    !ORIG_DEBUG!

echo.
echo Step 4: Cloning originals and injecting recompiled classes ...
for %%J in ("!ORIG_STLINK!") do set "OUT_STLINK=%MOD_DIR%\%%~nxJ"
for %%J in ("!ORIG_DEBUG!")  do set "OUT_DEBUG=%MOD_DIR%\%%~nxJ"
copy /Y "!ORIG_STLINK!" "!OUT_STLINK!" >nul
copy /Y "!ORIG_DEBUG!"  "!OUT_DEBUG!"  >nul

pushd "%OUTPUT_DIR%"
for %%C in (com\st\stm32cube\ide\mcu\debug\stlink\StLinkDebugHardware.class com\st\stm32cube\ide\mcu\debug\stlink\StLinkDebugHardware$*.class) do (
    echo   + %%C
    jar uf "!OUT_STLINK!" "%%C"
)
for %%C in (com\st\stm32cube\ide\mcu\debug\stlinkfwutil\StLinkFwUtil.class com\st\stm32cube\ide\mcu\debug\stlinkfwutil\StLinkFwUtil$*.class) do (
    echo   + %%C
    jar uf "!OUT_DEBUG!" "%%C"
)
popd

echo.
echo Step 5: Verifying OSGi manifests survived ...
set "MFTMP=%OUTPUT_DIR%\mfcheck"
rmdir /s /q "%MFTMP%" 2>nul
mkdir "%MFTMP%" 2>nul
pushd "%MFTMP%"
jar xf "!OUT_STLINK!" META-INF/MANIFEST.MF 2>nul
findstr /i "Bundle-SymbolicName" "META-INF\MANIFEST.MF" >nul && (echo   [OK] stlink: Bundle-SymbolicName present) || (echo   [!!] stlink: NO Bundle-SymbolicName -- clone source is not a valid bundle^!)
del /q "META-INF\MANIFEST.MF" 2>nul
jar xf "!OUT_DEBUG!" META-INF/MANIFEST.MF 2>nul
findstr /i "Bundle-SymbolicName" "META-INF\MANIFEST.MF" >nul && (echo   [OK] debug:  Bundle-SymbolicName present) || (echo   [!!] debug:  NO Bundle-SymbolicName -- clone source is not a valid bundle^!)
popd
rmdir /s /q "%MFTMP%" 2>nul

echo.
echo ============================================
echo DONE
echo ============================================
echo Patched jars (original names, manifests preserved):
echo   !OUT_STLINK!
echo   !OUT_DEBUG!
echo.
echo If both manifest checks say [OK], install:
echo   1. Close STM32CubeIDE.
echo   2. Copy both jars from %MOD_DIR%\ over the same-named files in %PLUGIN_DIR%\
echo   3. Relaunch once:  stm32cubeide.exe -clean
echo If either says [!!], restore a PRISTINE jar into %MASTER_DIR% and re-run.
echo ============================================
pause
goto end

:error
echo.
echo ============================================
echo ERROR: Compilation failed!
echo ============================================
pause

:end
endlocal
