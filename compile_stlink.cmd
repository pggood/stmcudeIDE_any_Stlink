@echo off
setlocal enabledelayedexpansion

set PLUGIN_DIR=D:\ST\STM32CubeIDE_1.18.1\STM32CubeIDE\plugins
set SOURCE_STLINK=D:\com.st.stm32cube.ide.mcu.debug.stlink
set SOURCE_DEBUG=D:\com.st.stm32cube.ide.mcu.debug
set OUTPUT_DIR=D:\bin
set JAR_NAME_STLINK=D:\stlink_modified_%date:~10,4%%date:~4,2%%date:~7,2%.jar
set JAR_NAME_DEBUG=D:\debug_modified_%date:~10,4%%date:~4,2%%date:~7,2%.jar

echo ============================================
echo SAFE COMPILATION - No Directory Overwrite
echo ============================================
echo.

if not exist "%SOURCE_STLINK%" ( echo ERROR: ST-LINK source folder not found! & pause & exit /b 1 )
if not exist "%SOURCE_DEBUG%"  ( echo ERROR: Main Debug source folder not found! & pause & exit /b 1 )

mkdir %OUTPUT_DIR% 2>nul

REM ------------------------------------------------------------------
REM Build a javac args file with the FULL classpath.
REM NOTE: javac does NOT expand the "*" wildcard inside an @argfile,
REM so we enumerate every .jar explicitly, then add every exploded
REM (dir-shaped) bundle. An @argfile has no command-line length limit.
REM ------------------------------------------------------------------
set "CPFILE=%OUTPUT_DIR%\cp.args"
if exist "%CPFILE%" del "%CPFILE%"

> "%CPFILE%" echo -classpath

echo Enumerating jars...
set /a NJARS=0
for %%J in ("%PLUGIN_DIR%\*.jar") do (
    >> "%CPFILE%" <nul set /p "=%%J;"
    set /a NJARS+=1
)

echo Enumerating exploded bundle dirs...
set /a NDIRS=0
for /d %%D in ("%PLUGIN_DIR%\*") do (
    >> "%CPFILE%" <nul set /p "=%%D;"
    set /a NDIRS+=1
)

>> "%CPFILE%" echo.
echo Classpath: !NJARS! jars + !NDIRS! exploded dirs   (args file: %CPFILE%)
echo.

echo Step 1: Compiling StLinkFwUtil.java (from Main Debug plugin)...
javac @"%CPFILE%" -d %OUTPUT_DIR% "%SOURCE_DEBUG%\com\st\stm32cube\ide\mcu\debug\stlinkfwutil\StLinkFwUtil.java"
if errorlevel 1 goto error

echo Step 2: Compiling StLinkDebugHardware.java (from ST-LINK plugin)...
javac @"%CPFILE%" -d %OUTPUT_DIR% "%SOURCE_STLINK%\com\st\stm32cube\ide\mcu\debug\stlink\StLinkDebugHardware.java"
if errorlevel 1 goto error

echo.
echo Step 3: Copying compiled classes back to source directories...
xcopy /E /I /Y "%OUTPUT_DIR%\com\st\stm32cube\ide\mcu\debug\stlink"       "%SOURCE_STLINK%\com\st\stm32cube\ide\mcu\debug\stlink\" 2>nul
xcopy /E /I /Y "%OUTPUT_DIR%\com\st\stm32cube\ide\mcu\debug\stlinkfwutil" "%SOURCE_DEBUG%\com\st\stm32cube\ide\mcu\debug\stlinkfwutil\" 2>nul

echo.
echo Step 4: Creating JAR files...
echo Creating ST-LINK JAR: %JAR_NAME_STLINK%
cd /d "%SOURCE_STLINK%"
jar cf "%JAR_NAME_STLINK%" *
echo Creating Main Debug JAR: %JAR_NAME_DEBUG%
cd /d "%SOURCE_DEBUG%"
jar cf "%JAR_NAME_DEBUG%" *

echo.
echo ============================================
echo SUCCESS!
echo ============================================
echo ST-LINK JAR: %JAR_NAME_STLINK%
echo Debug JAR:   %JAR_NAME_DEBUG%
echo.
echo To install:
echo 1. Backup the originals from %PLUGIN_DIR%\
echo 2. Copy the modified JARs to %PLUGIN_DIR%\
echo 3. Restart STM32CubeIDE
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