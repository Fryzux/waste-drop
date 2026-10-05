# 👥 РАСПРЕДЕЛЕНИЕ СУЩНОСТЕЙ НА 4 ЧЕЛОВЕКА ДЛЯ ЗАЩИТЫ КР-1

> **Официальное требование кафедры:** Наличие в проекте не менее 1 сущности на одного участника команды.  
> **Каждый участник обязан продемонстрировать выполнение всех 8 пунктов по своей сущности:**  
> 1. Модель (конструктор, геттеры, сеттеры, инкапсуляция)  
> 2. Репозиторий (PostgreSQL через JDBC PreparedStatement)  
> 3. Сервис с CRUD операциями  
> 4. Перечисление (enum с полями и методами)  
> 5. Исключения (собственная иерархия exceptions)  
> 6.1. Прямая и обратная сортировка (Stream API)  
> 6.2. Фильтрация списка по основным критериям (Stream API)  
> 7. Экспорт данных в Excel (.xlsx через Apache POI)  
> 8. Консольный интерфейс для управления сущностью  

---

## 🏆 УЧАСТНИК 1 (АНДРЕЙ — Team Lead / Архитектор)
### Сущность: `WasteRequest` (Заявка на вывоз отходов)

* **1. Модель:** `WasteRequest.java`
  * Поля: `id` (Long), `clientId` (Long), `address` (String), `wasteType` (WasteType), `volumeM3` (double), `status` (RequestStatus), `createdAt` (LocalDateTime).
  * Конструкторы (полный и без ID), геттеры, сеттеры с валидацией, переопределенный `toString()` с табличным форматированием.
* **2. Репозиторий:** `WasteRequestRepository.java` + `JdbcWasteRequestRepository.java`
  * Таблица PostgreSQL: `waste_requests` (внешний ключ `fk_waste_requests_client`, индексы по статусу и дате).
  * Методы: `save(WasteRequest)`, `findById(Long)`, `findAll()`, `update(WasteRequest)`, `deleteById(Long)`, `findByClientId(Long)`.
* **3. Сервис:** `WasteRequestService.java`
  * CRUD: `createRequest()`, `getById()`, `getAllRequests()`, `updateStatus()`, `deleteRequest()`.
  * Валидация объема отходов (0.1 – 100.0 м³) и проверка существования клиента перед оформлением заявки.
* **4. Enum:** `RequestStatus.java` (`NEW`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)
  * Реализует конечный автомат (FSM): метод `canTransitionTo(RequestStatus)` запрещает недопустимые переходы (например, из `COMPLETED` обратно в `NEW`).
  * Дополнительный Enum: `WasteType.java` (`MUNICIPAL`, `CONSTRUCTION`, `BULKY`, `HAZARDOUS`).
* **5. Исключения:** `BusinessException.java`
  * Запрет перевода в некорректный статус.
  * Запрет физического удаления выполненных (`COMPLETED`) заявок из реестра.
  * `EntityNotFoundException.java` при отсутствии заявки по ID.
* **6.1. Сортировки (Stream API):**
  * `sortByDate(boolean ascending)` — прямая (сначала старые) и обратная (сначала новые).
  * `sortByVolume(boolean ascending)` — прямая (по возрастанию) и обратная (по убыванию).
* **6.2. Фильтрация (Stream API):**
  * `filterByStatus(RequestStatus)` — по статусу выполнения.
  * `filterByWasteType(WasteType)` — по типу отходов.
  * `filterByMinVolume(double)` — по минимальному объему.
  * `searchByAddress(String)` — полнотекстовый поиск по фрагменту адреса.
* **7. Экспорт в Excel:** `ExcelExporter.exportRequests()` $\to$ `reports/waste_requests.xlsx`
  * Apache POI: стили заголовков, автоподбор ширины колонок, цветные статусы.
* **8. Консольный интерфейс:**
  * Пункты главного меню: **2** (Создать), **3** (Реестр), **4** (Смена статуса), **5** (Удалить), **6** (Поиск/Фильтры), **7** (Сортировка), **8** (Статистика), **9** (Экспорт Excel).

---

## 🥈 УЧАСТНИК 2
### Сущность: `Client` (Контрагент / Клиент)

* **1. Модель:** `Client.java`
  * Поля: `id` (Long), `name` (String), `phone` (String), `email` (String), `clientType` (ClientType).
  * Инкапсулированная валидация: в методе `setPhone()` проверяется, что номер содержит не менее 10 цифр и не null.
* **2. Репозиторий:** `ClientRepository.java` + `JdbcClientRepository.java`
  * Таблица PostgreSQL: `clients` (`id`, `name`, `phone`, `email`, `client_type`).
  * Методы: `save(Client)`, `findById(Long)`, `findAll()`, `findByPhone(String)`, `findByEmail(String)`, `update(Client)`, `deleteById(Long)`.
* **3. Сервис:** `ClientService.java`
  * CRUD: `registerClient()`, `getById()`, `getAllClients()`, `updateClient()`, `deleteClient()`.
  * Бизнес-валидация: проверка длины наименования (не менее 2 символов), корректности формата email (регулярное выражение).
* **4. Enum:** `ClientType.java`
  * Значения: `INDIVIDUAL` («Физическое лицо»), `LEGAL_ENTITY` («Юридическое лицо»), `MUNICIPAL_ORG` («Муниципальная организация»).
  * Содержит конструктор с русскоязычным `title` и геттер `getTitle()`.
* **5. Исключения:**
  * `BusinessException.java` (невалидный email, дублирование телефона/почты).
  * `EntityNotFoundException.java` (клиент с данным ID не найден в БД).
* **6.1. Сортировки (Stream API):**
  * `sortByName(boolean ascending)` — прямая (А-Я) и обратная (Я-А) сортировка по наименованию/ФИО.
  * `sortById(boolean ascending)` — прямая и обратная сортировка по ID клиента.
* **6.2. Фильтрация (Stream API):**
  * `filterByType(ClientType)` — фильтрация по категории клиента (только юрлица, только физлица и т.д.).
  * `searchByName(String query)` — поиск по подстроке наименования.
* **7. Экспорт в Excel:** `ExcelExporter.exportClients()` $\to$ `reports/clients.xlsx`
  * Выгрузка всех контрагентов с автоматическим расчетом ширины ячеек.
* **8. Консольный интерфейс:**
  * Пункт главного меню **`1. База клиентов`** $\to$ внутреннее меню (1-7):
    1. Список всех клиентов
    2. Зарегистрировать нового клиента
    3. Изменить данные клиента (Update)
    4. Удалить клиента (Delete)
    5. Сортировка клиентов (прямая и обратная)
    6. Фильтрация и поиск клиентов
    7. Экспорт списка клиентов в Microsoft Excel

---

## 🥉 УЧАСТНИК 3
### Сущность: `Vehicle` (Спецтранспорт автопарка / Мусоровоз)

* **1. Модель:** `Vehicle.java`
  * Поля: `id` (Long), `licensePlate` (String), `modelName` (String), `capacityM3` (double), `status` (VehicleStatus).
  * Конструкторы, валидация госномера и объема (1.0 – 60.0 м³), переопределенный `toString()` с форматированием `Locale.US`.
* **2. Репозиторий:** `VehicleRepository.java` + `JdbcVehicleRepository.java`
  * Таблица PostgreSQL: `vehicles` (`id`, `license_plate` UNIQUE, `model_name`, `capacity_m3`, `status`).
  * Методы: `save(Vehicle)`, `findById(Long)`, `findAll()`, `findByLicensePlate(String)`, `findByStatus(VehicleStatus)`, `update(Vehicle)`, `deleteById(Long)`.
* **3. Сервис:** `VehicleService.java`
  * CRUD: `registerVehicle()`, `getById()`, `getAllVehicles()`, `updateVehicle()`, `updateStatus()`, `deleteVehicle()`.
  * Валидация уникальности госномера перед сохранением.
* **4. Enum:** `VehicleStatus.java`
  * Значения: `AVAILABLE` («Готов к рейсу / Свободен»), `ON_ROUTE` («На маршруте / В рейсе»), `MAINTENANCE` («На техническом обслуживании»).
  * Метод `getTitle()` для читаемого отображения в UI и Excel.
* **5. Исключения:**
  * `BusinessException.java` — строгий бизнес-запрет: **нельзя списать или удалить машину, которая прямо сейчас находится в рейсе (`ON_ROUTE`)**!
  * `EntityNotFoundException.java` (спецтранспорт с указанным ID отсутствует).
* **6.1. Сортировки (Stream API):**
  * `sortByCapacity(boolean ascending)` — прямая (по возрастанию) и обратная (по убыванию) по вместимости кузова в м³.
  * `sortByLicensePlate(boolean ascending)` — алфавитная сортировка по госномерам машин.
* **6.2. Фильтрация (Stream API):**
  * `filterByStatus(VehicleStatus)` — выборка только доступных машин либо машин на ремонте.
  * `filterByMinCapacity(double minCapacity)` — выборка большегрузных мусоровозов с объемом не менее X м³.
* **7. Экспорт в Excel:** `ExcelExporter.exportVehicles()` $\to$ `reports/vehicles.xlsx`
  * Таблица автопарка с параметрами кузова и текущим эксплуатационным статусом.
* **8. Консольный интерфейс:**
  * Пункт главного меню **`10. Автопарк спецтехники`** $\to$ внутреннее меню (1-8):
    1. Список спецтранспорта
    2. Зарегистрировать спецтранспорт
    3. Изменить данные спецтранспорта (Update)
    4. Сменить статус машины (AVAILABLE / ON_ROUTE / MAINTENANCE)
    5. Удалить транспорт из автопарка (Delete)
    6. Сортировка транспорта (прямая и обратная по объему кузова)
    7. Фильтрация транспорта (по статусу или мин. вместимости)
    8. Экспорт автопарка в Microsoft Excel

---

## 🎖️ УЧАСТНИК 4
### Сущность: `Worker` (Персонал и экипажи: водители, грузчики, диспетчеры)

* **1. Модель:** `Worker.java`
  * Поля: `id` (Long), `fullName` (String), `phone` (String), `role` (WorkerRole), `salary` (double).
  * Инкапсуляция: валидация телефона в `setPhone()` (не менее 10 цифр), защита оклада, переопределенный `toString()` (`Locale.US`).
* **2. Репозиторий:** `WorkerRepository.java` + `JdbcWorkerRepository.java`
  * Таблица PostgreSQL: `workers` (`id`, `full_name`, `phone`, `role`, `salary`).
  * Методы: `save(Worker)`, `findById(Long)`, `findAll()`, `findByRole(WorkerRole)`, `update(Worker)`, `deleteById(Long)`.
* **3. Сервис:** `WorkerService.java`
  * CRUD: `registerWorker()`, `getById()`, `getAllWorkers()`, `updateWorker()`, `deleteWorker()`.
  * Валидация: длина ФИО не менее 3 символов, допустимый диапазон оклада от 20 000 до 500 000 руб.
* **4. Enum:** `WorkerRole.java`
  * Значения: `DRIVER` («Водитель мусоровоза»), `LOADER` («Оператор-грузчик»), `DISPATCHER` («Логист-диспетчер»).
  * Метод `getTitle()` и `toString()`.
* **5. Исключения:**
  * `BusinessException.java` (недопустимый оклад, короткое имя, ошибки валидации).
  * `EntityNotFoundException.java` (сотрудник с данным ID не найден в штатном расписании).
* **6.1. Сортировки (Stream API):**
  * `sortByName(boolean ascending)` — прямая (А-Я) и обратная (Я-А) сортировка по ФИО сотрудника.
  * `sortBySalary(boolean ascending)` — прямая (по возрастанию оклада) и обратная (по убыванию оклада).
* **6.2. Фильтрация (Stream API):**
  * `filterByRole(WorkerRole)` — выборка по занимаемой должности (водители, грузчики, диспетчеры).
  * `searchByName(String query)` — поиск сотрудника по фрагменту фамилии или имени.
* **7. Экспорт в Excel:** `ExcelExporter.exportWorkers()` $\to$ `reports/workers.xlsx`
  * Полное штатное расписание оператора с окладами и контактными номерами.
* **8. Консольный интерфейс:**
  * Пункт главного меню **`11. Персонал и экипажи`** $\to$ внутреннее меню (1-7):
    1. Список всех сотрудников
    2. Нанять нового сотрудника (Регистрация)
    3. Изменить данные сотрудника (Update)
    4. Уволить сотрудника из штата (Delete)
    5. Сортировка персонала (прямая и обратная)
    6. Фильтрация и поиск сотрудников
    7. Экспорт базы персонала в Microsoft Excel

---

## ⚡ БЫСТРАЯ СВОДНАЯ ТАБЛИЦА ДЛЯ КОМИССИИ

| № | Участник | Сущность | Модель | Репозиторий | Сервис | Enum | Исключение (бизнес-правило) | Сортировки | Фильтрация | Меню в консоли |
|---|---|---|---|---|---|---|---|---|---|---|
| **1** | **Андрей (Lead)** | `WasteRequest` | `WasteRequest.java` | `JdbcWasteRequestRepository` | `WasteRequestService` | `RequestStatus`, `WasteType` | Нельзя удалять выполненные (`COMPLETED`) | По дате и объему (asc/desc) | По статусу, типу, объему, адресу | Пункты 2–9 |
| **2** | **Участник 2** | `Client` | `Client.java` | `JdbcClientRepository` | `ClientService` | `ClientType` | Валидация email и телефона ($\ge 10$ цифр) | По имени (А-Я / Я-А), по ID | По типу клиента (юр/физ/мун) | Пункт 1 (подпункты 1–7) |
| **3** | **Участник 3** | `Vehicle` | `Vehicle.java` | `JdbcVehicleRepository` | `VehicleService` | `VehicleStatus` | Нельзя удалять машину в рейсе (`ON_ROUTE`) | По объему кузова, по госномерам | По статусу (свободен/ТО), по объему | Пункт 10 (подпункты 1–8) |
| **4** | **Участник 4** | `Worker` | `Worker.java` | `JdbcWorkerRepository` | `WorkerService` | `WorkerRole` | Диапазон оклада 20k–500k, валидация ФИО | По ФИО (А-Я / Я-А), по окладу | По должности (водитель/грузчик/диспетчер) | Пункт 11 (подпункты 1–7) |

---

## 💻 ПРИМЕРЫ СОРТИРОВОК И ФИЛЬТРАЦИИ НА STREAM API (ЕСЛИ СПРОСЯТ КОД)

### 1. Прямая и обратная сортировка:
```java
// Прямая (по возрастанию оклада):
workers.stream()
       .sorted(Comparator.comparingDouble(Worker::getSalary))
       .collect(Collectors.toList());

// Обратная (по убыванию оклада):
workers.stream()
       .sorted(Comparator.comparingDouble(Worker::getSalary).reversed())
       .collect(Collectors.toList());
```

### 2. Фильтрация:
```java
// Фильтрация по перечислению (Enum):
workers.stream()
       .filter(w -> w.getRole() == WorkerRole.DRIVER)
       .collect(Collectors.toList());
```

### 3. Экспорт в Excel (Apache POI):
```java
try (Workbook workbook = new XSSFWorkbook()) {
    Sheet sheet = workbook.createSheet("Штатное расписание");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("ID");
    header.createCell(1).setCellValue("ФИО");
    header.createCell(2).setCellValue("Должность");
    // Заполнение данными через for-each...
    try (FileOutputStream fos = new FileOutputStream("reports/workers.xlsx")) {
        workbook.write(fos);
    }
}
```

---

## 🎯 СОВЕТЫ ПО СДАЧЕ КР-1:
1. Запускайте программу командой:  
   `java -Dfile.encoding=UTF-8 -jar target/waste-management-system-1.0.0-jar-with-dependencies.jar`  
   или прямо из VS Code нажатием **F5** (`Run Main`).
2. В консольном меню нет надписей `[Участник 1/2/3/4]`, всё выглядит как реальная производственная система:
   - База клиентов
   - Реестр заявок на вывоз отходов
   - Автопарк спецтехники
   - Персонал и экипажи
3. При вопросе преподавателя: каждый участник уверенно открывает свой класс модели, сервиса и репозитория, показывает пункт меню и файл `.xlsx`, который сгенерировался в папке `reports/`.
