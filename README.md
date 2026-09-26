# 🚛 Мусор Дроп (Waste Drop) — Сервис управления заявками на вывоз отходов

Проект разработан студенческой командой для демонстрации и защиты в **РТУ МИРЭА (Институт перспективных технологий и индустриального программирования — ИПТИП)**.

Приложение представляет собой надежную многослойную консольную систему на стеке **Java 25+, PostgreSQL JDBC, Apache POI и Stream API** со встроенным контуром защиты от интеграционных сбоев, платформенных различий ОС (Windows vs macOS/Linux) и коллизий данных.

---

## 👥 Роли в команде и зоны ответственности

| Роль | Участник | Ветка в Git | Зона ответственности |
| :--- | :--- | :--- | :--- |
| **Dev 1 (Team Lead / Data Architect)** | Андрей | `feature/infra-and-db` | Архитектура проекта, `pom.xml`, `.gitignore`, `DatabaseManager`, DDL `schema.sql`, `data.sql`, ER-модель |
| **Dev 2 (Backend / Persistence)** | Участник 2 | `feature/domain-and-repositories` | Доменные модели (`Client`, `WasteRequest`), `CrudRepository`, JDBC-репозитории, маппинг SQLState `23503` |
| **Dev 3 (Core / Business Logic)** | Участник 3 | `feature/service-logic-and-excel` | `WasteRequestService`, 5 бизнес-правил, Stream API (поиск/сортировка/статистика), `ExcelExporter` (Apache POI) |
| **Dev 4 (UI / Presentation & QA)** | Участник 4 | `feature/cli-ui-controller` | `InputReader` (защита Scanner и локалей), `ConsoleMenu`, мастер расчета объема, сборка `Main.java` |

---

## 🛡 Защитные механизмы системы (Anti-Bug & Anti-Crash)

1. **Кроссплатформенность локалей и кодировок:**
   - Строгая фиксация UTF-8 в `pom.xml` (`project.build.sourceEncoding`).
   - `InputReader` автоматически выполняет нормализацию разделителя дробной части (`.replace(',', '.')`), предотвращая сбои `Double.parseDouble()` на русской Windows-локали.
2. **Изоляция учетных записей БД:**
   - Файл `db.properties` вынесен в `.gitignore`. В репозитории хранится только нейтральный `db.properties.example`.
   - `DatabaseManager` поддерживает переменные окружения (`DB_URL`, `DB_USER`, `DB_PASSWORD`), что позволяет запускать код без правок чужих файлов.
3. **Защита от блокировки Excel процессом ОС:**
   - При попытке экспорта в открытый в MS Excel файл программа не падает с `FileNotFoundException (Access Denied)`, а выводит вежливое предупреждение с просьбой закрыть окно Excel.
4. **Контроль ссылочной целостности и утечек ресурсов:**
   - Все JDBC-соединения, стейтменты и курсоры обернуты в `try-with-resources`.
   - Внешний ключ `client_id` защищен правилом `ON DELETE RESTRICT`. Ошибка СУБД с кодом `23503` мапится в бизнес-исключение без показа сырого стектрейса.
5. **Мастер расчета объема отходов:**
   - Поддержка клиентов, не знающих кубометры: расчет по габаритам ($Д \times Ш \times В$) и по типовой таре (мешки 120л, евробаки 1.1 м³, бункеры «Лодочка» 8 м³, ГАЗель 9 м³, ПУХТО 20 м³).

---

## 🚀 Требования к окружению и запуск

### Требования:
* **Java Development Kit (JDK):** версия 17 или новее.
* **Apache Maven:** версия 3.8+.
* **PostgreSQL:** версия 14-18 (порт 5432).

### Настройка базы данных:
1. Создайте базу данных в PostgreSQL (например, через pgAdmin или `psql`):
   ```sql
   CREATE DATABASE waste_db;
   ```
2. Скопируйте файл конфигурации:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
3. Откройте `src/main/resources/db.properties` и укажите ваш пароль от пользователя `postgres`.
4. *Примечание:* Таблицы и тестовые данные загрузятся **автоматически** при первом запуске приложения!

---

### Сборка и запуск проекта:

#### Windows (PowerShell / CMD):
```powershell
# Запуск автоматических тестов
mvn test

# Сборка исполняемого jar-пакета
mvn clean package

# Запуск приложения
java -jar target/waste-management-system-1.0.0-jar-with-dependencies.jar
```
Или запустите готовый скрипт: `scripts\run.bat`.

#### macOS / Linux:
```bash
mvn clean test package
java -jar target/waste-management-system-1.0.0-jar-with-dependencies.jar
```

---

## 📁 Структура проекта

```
waste-management-system/
├── pom.xml                                  # Сборка и зависимости Maven
├── .gitignore                               # Исключения Git
├── README.md                                # Руководство к проекту
├── docs/
│   ├── ARCHITECTURE.md                      # Диаграммы Mermaid и архитектурный отчет
│   └── DEFENSE_GUIDE.md                     # Шпаргалка с вопросами комиссии для защиты
├── scripts/
│   ├── setup-git-branches.ps1               # Скрипт инициализации 4 веток разработки
│   └── run.bat                              # Быстрый запуск на Windows
├── src/
│   ├── main/
│   │   ├── java/ru/mirea/wastemanagement/
│   │   │   ├── Main.java                    # Точка входа (Manual Dependency Injection)
│   │   │   ├── exception/                   # Иерархия бизнес-исключений
│   │   │   ├── model/                       # Доменные сущности и FSM переходов
│   │   │   ├── repository/                  # Интерфейсы и JDBC-реализации
│   │   │   ├── service/                     # Бизнес-логика, валидация и Stream API
│   │   │   ├── ui/                          # Безопасный ввод и интерактивное меню
│   │   │   └── util/                        # DatabaseManager, POI ExcelExporter, Калькулятор
│   │   └── resources/
│   │       ├── db.properties.example        # Шаблон учетных данных
│   │       ├── db.properties                # Локальные учетные данные
│   │       ├── schema.sql                   # DDL схемы (таблицы, CHECK, RESTRICT)
│   │       └── data.sql                     # Демо-данные для сдачи
│   └── test/
│       └── java/ru/mirea/wastemanagement/   # Автономные модульные тесты JUnit 5
```
