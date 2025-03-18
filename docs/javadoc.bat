@echo off
echo Generating Javadoc documentation...
pushd %~dp0\..
call gradlew.bat javadoc
if %ERRORLEVEL% neq 0 (
    echo Failed to generate Javadoc documentation.
    pause
    popd
    exit /b %ERRORLEVEL%
)
echo Opening Javadoc in browser...
start "" "build\docs\javadoc\index.html"
echo Done.
popd