@echo off
chcp 65001 > nul
echo ================================================================================
echo          СЕРВИС ЗАЯВОК НА ВЫВОЗ ОТХОДОВ «МУСОР ДРОП» (МИРЭА - ИПТИП)
echo ================================================================================

cd /d "%~dp0\.."

echo [*] Сборка проекта через Maven...
call mvn clean package -DskipTests=false
if %errorlevel% neq 0 (
    echo [!] Ошибка при сборке проекта. Проверьте вывод Maven выше.
    pause
    exit /b %errorlevel%
)

echo.
echo [*] Запуск приложения...
echo.
java -Dfile.encoding=UTF-8 -jar target\waste-management-system-1.0.0-jar-with-dependencies.jar

pause
