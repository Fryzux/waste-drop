# Персональные задачи участников команды (Sprint 1: КР 1)

Проект: **«Мусор Дроп» (Waste Drop)**  
Репозиторий: [github.com/Fryzux/waste-drop](https://github.com/Fryzux/waste-drop)

---

## 🎫 Задача №1: Dev 1 (Андрей — Team Lead / Data Architect & DevOps)
* **Ветка:** `feature/infra-and-db` -> `develop`
* **Статус:** ✅ ВЫПОЛНЕНО

### Выполненные работы:
1. Создан репозиторий и настроена архитектура GitFlow (`main`, `develop`, 4 функциональные ветки `feature/*`).
2. Написана схема БД `schema.sql` со строгими ограничениями `CHECK (volume_m3 >= 0.1 AND volume_m3 <= 100.0)` и `ON DELETE RESTRICT`.
3. Подготовлены сбалансированные тестовые данные `data.sql` (5 клиентов, 10 разнотипных заявок).
4. Разработан модуль `DatabaseManager.java` с паттерном Singleton, автонакатыванием таблиц при первом старте и поддержкой переменных окружения.
5. Настроен `pom.xml` (UTF-8, Java 17+, PostgreSQL JDBC, Apache POI, JUnit 5) и сборка исполняемого Fat-JAR.
6. Развернута база `waste_db` на локальном PostgreSQL 18.
7. Проведен сквозной Smoke Test и Code Review входящих Pull Request.

---

## 🎫 Задача №2: Dev 2 (Backend Developer / Persistence Layer)
* **Ветка:** `feature/domain-and-repositories`
* **Файлы:** `ClientRepository.java`, `JdbcClientRepository.java`, `Client.java`
* **Статус:** В работе

### Техническое задание:
1. В интерфейс `ClientRepository.java` добавить объявление метода:
   ```java
   Optional<Client> findByEmail(String email);
   ```
2. В классе `JdbcClientRepository.java` реализовать метод через `PreparedStatement` и `try-with-resources`.
3. В классе `Client.java` в методе `setPhone(String phone)` добавить проверку: номер должен содержать не менее 10 цифр.
4. Закоммитить и отправить Pull Request в `develop`:
   ```bash
   git add .
   git commit -m "feat(repo): add findByEmail and client phone validation"
   git push origin feature/domain-and-repositories
   ```

---

## 🎫 Задача №3: Dev 3 (Core / Business Logic & Reporting)
* **Ветка:** `feature/service-logic-and-excel`
* **Файлы:** `WasteRequestService.java`, `ExcelExporter.java`
* **Статус:** В работе

### Техническое задание:
1. В сервисе `WasteRequestService.java` реализовать фильтрацию по минимальному объему отходов на Stream API:
   ```java
   public List<WasteRequest> filterByMinVolume(double minVolume) {
       return getAllRequests().stream()
               .filter(r -> r.getVolumeM3() >= minVolume)
               .sorted(Comparator.comparingDouble(WasteRequest::getVolumeM3).reversed())
               .collect(Collectors.toList());
   }
   ```
2. В `ExcelExporter.java` добавить цветную подсветку ячеек статусов в таблице Excel (зеленый для `COMPLETED`, синий для `IN_PROGRESS`, серый для `CANCELLED`).
3. Закоммитить и отправить Pull Request в `develop`:
   ```bash
   git add .
   git commit -m "feat(service): add filterByMinVolume and status color styling in Excel"
   git push origin feature/service-logic-and-excel
   ```

---

## 🎫 Задача №4: Dev 4 (Presentation Layer / CLI & QA)
* **Ветка:** `feature/cli-ui-controller`
* **Файлы:** `ConsoleMenu.java`, `InputReader.java`
* **Статус:** В работе

### Техническое задание:
1. В `ConsoleMenu.java` добавить пункт меню `10. О разработчиках системы «Мусор Дроп»` с выводом состава команды и ролей.
2. В подменю поиска и фильтрации подключить вызов `service.filterByMinVolume()`.
3. Провести сквозное QA-тестирование (ввод некорректных данных, расчет по габаритам и таре).
4. Закоммитить и отправить Pull Request в `develop`:
   ```bash
   git add .
   git commit -m "feat(ui): add team info screen and min-volume filter to menu"
   git push origin feature/cli-ui-controller
   ```
