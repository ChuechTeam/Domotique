@echo off
setlocal enabledelayedexpansion

REM Collect all arguments into a single string
set args=
for %%i in (%*) do (
    if defined args (
        set args=!args! %%i
    ) else (
        set args=%%i
    )
)

REM Execute the gradle task with the collected arguments
if defined args (
echo Running Liquibase via gradle: gradle updateDatabase --args="%args%"
) else (
    echo Running Liquibase via gradle: gradle updateDatabase
)
if defined args (
call gradlew updateDatabase --args="%args%"
) else (
    call gradlew updateDatabase
)

endlocal