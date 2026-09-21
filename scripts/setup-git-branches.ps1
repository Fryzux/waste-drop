# ==========================================================
# Скрипт инициализации Git-репозитория и веток команды
# Запускать в корневой папке проекта: waste-management-system
# ==========================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host ">>> Инициализация локального Git-репозитория..." -ForegroundColor Cyan

# 1. Инициализация и первый коммит
git init
git add .
git commit -m "chore: initial project setup (contracts, config, db schema, defensive core)"
git branch -M main

# 2. Создание базовой ветки интеграции develop
git checkout -b develop

# 3. Нарезка рабочих веток для всех 4 участников
Write-Host ">>> Создание изолированных веток разработки..." -ForegroundColor Green

git branch feature/infra-and-db
git branch feature/domain-and-repositories
git branch feature/service-logic-and-excel
git branch feature/cli-ui-controller

Write-Host ""
Write-Host "[OK] Репозиторий успешно инициализирован!" -ForegroundColor Yellow
Write-Host "Текущие ветки:"
git branch -a

Write-Host ""
Write-Host "Инструкция для Андрея (Team Lead):" -ForegroundColor Cyan
Write-Host "Каждый участник переключается в свою ветку командой:"
Write-Host "  git checkout feature/<название-ветки>"
Write-Host "В main и develop пушить только через Pull Request после ревью!"
