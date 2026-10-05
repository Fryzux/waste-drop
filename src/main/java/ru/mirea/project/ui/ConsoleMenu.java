package ru.mirea.project.ui;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.*;
import ru.mirea.project.service.ClientService;
import ru.mirea.project.service.VehicleService;
import ru.mirea.project.service.WasteRequestService;
import ru.mirea.project.service.WasteRequestStats;
import ru.mirea.project.util.ExcelExporter;

import java.util.List;

/**
 * Консольное интерактивное меню пользователя.
 * Реализует паттерн Presentation Controller с глобальным перехватом
 * бизнес-исключений для предотвращения сбоев JVM во время демонстрации.
 * Обеспечивает полноценное управление всеми 3 сущностями проекта (по 1 на каждого участника).
 */
public class ConsoleMenu {
    private final WasteRequestService service;
    private final ClientService clientService;
    private final VehicleService vehicleService;
    private final ExcelExporter excelExporter;
    private final InputReader reader;

    public ConsoleMenu(WasteRequestService service, ClientService clientService, VehicleService vehicleService, ExcelExporter excelExporter) {
        this.service = service;
        this.clientService = clientService;
        this.vehicleService = vehicleService;
        this.excelExporter = excelExporter;
        this.reader = new InputReader();
    }

    public ConsoleMenu(WasteRequestService service, ClientService clientService, ExcelExporter excelExporter) {
        this(service, clientService, null, excelExporter);
    }

    public void start() {
        printBanner();
        while (true) {
            printMainMenu();
            int choice = reader.readIntInRange("Выберите пункт меню [0-10]: ", 0, 10);
            System.out.println();

            if (choice == 0) {
                System.out.println("Завершение работы программы. До свидания!");
                break;
            }

            try {
                handleMenuChoice(choice);
            } catch (BusinessException e) {
                System.out.println("\n[!] БИЗНЕС-ОШИБКА: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[!] НЕПРЕДВИДЕННАЯ ОШИБКА: " + e.getMessage());
            }

            System.out.println("\nНажмите Enter для возврата в главное меню...");
            reader.waitForEnter();
        }
    }

    private void printBanner() {
        System.out.println("================================================================================");
        System.out.println("                  СЕРВИС ЗАЯВОК НА ВЫВОЗ ОТХОДОВ «МУСОР ДРОП»                   ");
        System.out.println("================================================================================");
    }

    private void printMainMenu() {
        System.out.println("\n----------------------------- ГЛАВНОЕ МЕНЮ -----------------------------");
        System.out.println("  1. Сущность «Клиенты» (CRUD, сортировка, фильтрация, экспорт) [Участник 2]");
        System.out.println("  2. Оформить новую заявку на вывоз отходов (с мастером объема)");
        System.out.println("  3. Показать полный реестр заявок [Участник 1 - Андрей]");
        System.out.println("  4. Сменить статус заявки (контроль конечного автомата FSM)");
        System.out.println("  5. Удалить заявку из реестра");
        System.out.println("  6. Поиск и фильтрация заявок (по адресу, клиенту, типу, статусу)");
        System.out.println("  7. Сортировка заявок (прямая и обратная по дате или объему)");
        System.out.println("  8. Аналитика и статистика реестра (5 ключевых метрик)");
        System.out.println("  9. Экспорт реестра заявок в Microsoft Excel (.xlsx)");
        System.out.println(" 10. Сущность «Спецтранспорт» (CRUD, сортировка, фильтрация, экспорт) [Участник 3]");
        System.out.println("  0. Выход из системы");
        System.out.println("------------------------------------------------------------------------");
    }

    private void handleMenuChoice(int choice) {
        switch (choice) {
            case 1 -> handleClientsSubmenu();
            case 2 -> handleCreateRequest();
            case 3 -> handleViewAllRequests();
            case 4 -> handleUpdateStatus();
            case 5 -> handleDeleteRequest();
            case 6 -> handleSearchAndFilter();
            case 7 -> handleSorting();
            case 8 -> handleStatistics();
            case 9 -> handleExportExcel();
            case 10 -> handleVehiclesSubmenu();
            default -> System.out.println("Неизвестный пункт.");
        }
    }

    // ==========================================================
    // 1. СУЩНОСТЬ 2: КЛИЕНТЫ (ПОЛНЫЙ КОМПЛЕКТ)
    // ==========================================================
    private void handleClientsSubmenu() {
        System.out.println("--- БАЗА КЛИЕНТОВ (СУЩНОСТЬ 2) ---");
        System.out.println("1. Список всех клиентов");
        System.out.println("2. Зарегистрировать нового клиента");
        System.out.println("3. Изменить данные клиента (Update)");
        System.out.println("4. Удалить клиента (Delete)");
        System.out.println("5. Сортировка клиентов (прямая и обратная)");
        System.out.println("6. Фильтрация и поиск клиентов");
        System.out.println("7. Экспорт списка клиентов в Microsoft Excel");
        int sub = reader.readIntInRange("Выберите действие [1-7]: ", 1, 7);

        switch (sub) {
            case 1 -> {
                List<Client> clients = clientService.getAllClients();
                if (clients.isEmpty()) System.out.println("Клиенты не найдены.");
                else {
                    System.out.println("\nСПИСОК КЛИЕНТОВ:");
                    clients.forEach(System.out::println);
                }
            }
            case 2 -> {
                String name = reader.readNonEmptyString("Введите наименование организации / ФИО: ");
                String phone = reader.readNonEmptyString("Введите контактный телефон (от 10 цифр): ");
                String email = reader.readNonEmptyString("Введите электронную почту: ");
                System.out.println("Выберите тип клиента:");
                System.out.println("  1. Физическое лицо");
                System.out.println("  2. Юридическое лицо");
                System.out.println("  3. Муниципальная организация");
                int typeChoice = reader.readIntInRange("Тип [1-3]: ", 1, 3);
                ClientType type = switch (typeChoice) {
                    case 2 -> ClientType.LEGAL_ENTITY;
                    case 3 -> ClientType.MUNICIPAL_ORG;
                    default -> ClientType.INDIVIDUAL;
                };
                Client created = clientService.registerClient(name, phone, email, type);
                System.out.printf("[✓] Клиент успешно зарегистрирован: %s\n", created);
            }
            case 3 -> {
                Long id = reader.readId("Введите ID клиента для редактирования: ");
                String name = reader.readNonEmptyString("Новое наименование / ФИО: ");
                String phone = reader.readNonEmptyString("Новый контактный телефон: ");
                String email = reader.readNonEmptyString("Новый email: ");
                System.out.println("Выберите новый тип клиента (1-Физ, 2-Юр, 3-Муниц): ");
                int t = reader.readIntInRange("Тип [1-3]: ", 1, 3);
                ClientType type = switch (t) {
                    case 2 -> ClientType.LEGAL_ENTITY;
                    case 3 -> ClientType.MUNICIPAL_ORG;
                    default -> ClientType.INDIVIDUAL;
                };
                Client updated = clientService.updateClient(id, name, phone, email, type);
                System.out.printf("[✓] Данные клиента успешно обновлены: %s\n", updated);
            }
            case 4 -> {
                Long id = reader.readId("Введите ID клиента для удаления: ");
                clientService.deleteClient(id);
                System.out.printf("[✓] Клиент #%d успешно удален.\n", id);
            }
            case 5 -> {
                System.out.println("1. По имени (А-Я, прямая)");
                System.out.println("2. По имени (Я-А, обратная)");
                System.out.println("3. По ID (возрастание)");
                System.out.println("4. По ID (убывание)");
                int s = reader.readIntInRange("Выбор [1-4]: ", 1, 4);
                List<Client> res = switch (s) {
                    case 1 -> clientService.sortByName(true);
                    case 2 -> clientService.sortByName(false);
                    case 3 -> clientService.sortById(true);
                    case 4 -> clientService.sortById(false);
                    default -> clientService.getAllClients();
                };
                res.forEach(System.out::println);
            }
            case 6 -> {
                System.out.println("1. Поиск по фрагменту имени/названия");
                System.out.println("2. Фильтр по типу контрагента");
                int f = reader.readIntInRange("Выбор [1-2]: ", 1, 2);
                if (f == 1) {
                    String q = reader.readNonEmptyString("Введите поисковый запрос: ");
                    clientService.searchByName(q).forEach(System.out::println);
                } else {
                    System.out.println("1-Физлицо, 2-Юрлицо, 3-Муниципалитет");
                    int t = reader.readIntInRange("Тип [1-3]: ", 1, 3);
                    ClientType ct = switch (t) {
                        case 2 -> ClientType.LEGAL_ENTITY;
                        case 3 -> ClientType.MUNICIPAL_ORG;
                        default -> ClientType.INDIVIDUAL;
                    };
                    clientService.filterByType(ct).forEach(System.out::println);
                }
            }
            case 7 -> {
                String path = "./reports/clients.xlsx";
                String saved = excelExporter.exportClients(clientService.getAllClients(), path);
                System.out.println("[✓] База клиентов выгружена в Excel: " + saved);
            }
        }
    }

    // ==========================================================
    // 10. СУЩНОСТЬ 3: СПЕЦТРАНСПОРТ (ПОЛНЫЙ КОМПЛЕКТ)
    // ==========================================================
    private void handleVehiclesSubmenu() {
        if (vehicleService == null) {
            System.out.println("[!] Сервис спецтранспорта не инициализирован.");
            return;
        }
        System.out.println("--- АВТОПАРК СПЕЦТЕХНИКИ (СУЩНОСТЬ 3) ---");
        System.out.println("1. Список спецтранспорта");
        System.out.println("2. Зарегистрировать спецтранспорт");
        System.out.println("3. Изменить данные спецтранспорта (Update)");
        System.out.println("4. Сменить статус машины (AVAILABLE / ON_ROUTE / MAINTENANCE)");
        System.out.println("5. Удалить транспорт из автопарка (Delete)");
        System.out.println("6. Сортировка транспорта (прямая и обратная по объему кузова)");
        System.out.println("7. Фильтрация транспорта (по статусу или мин. вместимости)");
        System.out.println("8. Экспорт автопарка в Microsoft Excel");
        int sub = reader.readIntInRange("Выберите действие [1-8]: ", 1, 8);

        switch (sub) {
            case 1 -> {
                List<Vehicle> list = vehicleService.getAllVehicles();
                if (list.isEmpty()) System.out.println("Автопарк пуст.");
                else list.forEach(System.out::println);
            }
            case 2 -> {
                String plate = reader.readNonEmptyString("Введите госномер (напр. А101МР77): ");
                String model = reader.readNonEmptyString("Введите марку и модель: ");
                double cap = reader.readDouble("Введите вместимость кузова в м³ (1-60): ");
                Vehicle created = vehicleService.registerVehicle(plate, model, cap, VehicleStatus.AVAILABLE);
                System.out.printf("[✓] Машина успешно добавлена в автопарк: %s\n", created);
            }
            case 3 -> {
                Long id = reader.readId("Введите ID спецтранспорта: ");
                String plate = reader.readNonEmptyString("Новый госномер: ");
                String model = reader.readNonEmptyString("Новая модель: ");
                double cap = reader.readDouble("Новая вместимость: ");
                Vehicle updated = vehicleService.updateVehicle(id, plate, model, cap, null);
                System.out.printf("[✓] Данные обновлены: %s\n", updated);
            }
            case 4 -> {
                Long id = reader.readId("Введите ID спецтранспорта: ");
                System.out.println("1. AVAILABLE (Готов к рейсу / Свободен)");
                System.out.println("2. ON_ROUTE (На маршруте / В рейсе)");
                System.out.println("3. MAINTENANCE (На техническом обслуживании)");
                int st = reader.readIntInRange("Выберите статус [1-3]: ", 1, 3);
                VehicleStatus status = switch (st) {
                    case 2 -> VehicleStatus.ON_ROUTE;
                    case 3 -> VehicleStatus.MAINTENANCE;
                    default -> VehicleStatus.AVAILABLE;
                };
                Vehicle updated = vehicleService.updateStatus(id, status);
                System.out.printf("[✓] Статус обновлен: %s\n", updated);
            }
            case 5 -> {
                Long id = reader.readId("Введите ID спецтранспорта для списания: ");
                vehicleService.deleteVehicle(id);
                System.out.printf("[✓] Машина #%d списана из автопарка.\n", id);
            }
            case 6 -> {
                System.out.println("1. По объему кузова (возрастание, прямая)");
                System.out.println("2. По объему кузова (убывание, обратная)");
                System.out.println("3. По госномеру (А-Я)");
                int s = reader.readIntInRange("Выбор [1-3]: ", 1, 3);
                List<Vehicle> res = switch (s) {
                    case 1 -> vehicleService.sortByCapacity(true);
                    case 2 -> vehicleService.sortByCapacity(false);
                    case 3 -> vehicleService.sortByLicensePlate(true);
                    default -> vehicleService.getAllVehicles();
                };
                res.forEach(System.out::println);
            }
            case 7 -> {
                System.out.println("1. Показать только свободные машины (AVAILABLE)");
                System.out.println("2. Показать машины на ТО (MAINTENANCE)");
                System.out.println("3. Показать машины с объемом не менее X м³");
                int f = reader.readIntInRange("Выбор [1-3]: ", 1, 3);
                if (f == 1) vehicleService.filterByStatus(VehicleStatus.AVAILABLE).forEach(System.out::println);
                else if (f == 2) vehicleService.filterByStatus(VehicleStatus.MAINTENANCE).forEach(System.out::println);
                else {
                    double min = reader.readDouble("Введите минимальный объем (м³): ");
                    vehicleService.filterByMinCapacity(min).forEach(System.out::println);
                }
            }
            case 8 -> {
                String path = "./reports/vehicles.xlsx";
                String saved = excelExporter.exportVehicles(vehicleService.getAllVehicles(), path);
                System.out.println("[✓] Данные автопарка выгружены в Excel: " + saved);
            }
        }
    }

    // ==========================================================
    // СУЩНОСТЬ 1: ЗАЯВКИ НА ВЫВОЗ (АНДРЕЙ)
    // ==========================================================

    private void handleCreateRequest() {
        System.out.println("--- ОФОРМЛЕНИЕ НОВОЙ ЗАЯВКИ ---");
        Long clientId = reader.readId("Введите ID клиента: ");
        clientService.getById(clientId);

        String address = reader.readNonEmptyString("Введите точный адрес вывоза (от 5 символов): ");

        System.out.println("\nВыберите тип отходов:");
        WasteType[] types = WasteType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("  %d. %s (%s)\n", i + 1, types[i].name(), types[i].getTitle());
        }
        int typeIdx = reader.readIntInRange("Выберите тип [1-" + types.length + "]: ", 1, types.length) - 1;
        WasteType selectedType = types[typeIdx];

        double volumeM3 = reader.promptVolume();

        WasteRequest created = service.createRequest(clientId, address, selectedType, volumeM3);
        System.out.println("\n[✓] Заявка успешно зарегистрирована!");
        System.out.printf("    Номер заявки: #%d\n", created.getId());
        System.out.printf("    Статус: %s (%s)\n", created.getStatus().name(), created.getStatus().getTitle());
        System.out.printf("    Расчетный объем: %.2f м³\n", created.getVolumeM3());
    }

    private void handleViewAllRequests() {
        System.out.println("--- ПОЛНЫЙ РЕЕСТР ЗАЯВОК ---");
        List<WasteRequest> all = service.getAllRequests();
        printTable(all);
    }

    private void handleUpdateStatus() {
        System.out.println("--- СМЕНА СТАТУСА ЗАЯВКИ (FSM) ---");
        Long requestId = reader.readId("Введите номер заявки (ID): ");

        WasteRequest current = service.getById(requestId);
        System.out.println("Текущее состояние: " + current);

        System.out.println("\nДоступные целевые статусы:");
        RequestStatus[] statuses = RequestStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            boolean allowed = current.getStatus().canTransitionTo(statuses[i]);
            System.out.printf("  %d. %-12s - %s %s\n",
                    i + 1, statuses[i].name(), statuses[i].getTitle(), allowed ? "[РАЗРЕШЕНО]" : "[ЗАПРЕЩЕНО]");
        }

        int statusIdx = reader.readIntInRange("Выберите новый статус [1-" + statuses.length + "]: ", 1, statuses.length) - 1;
        RequestStatus nextStatus = statuses[statusIdx];

        WasteRequest updated = service.updateStatus(requestId, nextStatus);
        System.out.println("\n[✓] Статус заявки #" + requestId + " успешно изменен на: " + updated.getStatus().getTitle());
    }

    private void handleDeleteRequest() {
        System.out.println("--- УДАЛЕНИЕ ЗАЯВКИ ИЗ РЕЕСТРА ---");
        Long requestId = reader.readId("Введите номер заявки (ID) для удаления: ");

        WasteRequest current = service.getById(requestId);
        System.out.println("Найдена заявка: " + current);

        String confirm = reader.readNonEmptyString("Вы уверены, что хотите безвозвратно удалить заявку? (y/n): ");
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm) || "да".equalsIgnoreCase(confirm)) {
            service.deleteRequest(requestId);
            System.out.println("[✓] Заявка #" + requestId + " успешно удалена из системы.");
        } else {
            System.out.println("[-] Удаление отменено пользователем.");
        }
    }

    private void handleSearchAndFilter() {
        System.out.println("--- ПОИСК И ФИЛЬТРАЦИЯ ЗАЯВОК (STREAM API) ---");
        System.out.println("  1. Поиск по фрагменту адреса (без учета регистра)");
        System.out.println("  2. Фильтрация по статусу заявки");
        System.out.println("  3. Фильтрация по типу отходов");
        System.out.println("  4. Отбор крупных партий (объем >= minVolume)");
        System.out.println("  5. Показать все заявки конкретного клиента");
        int opt = reader.readIntInRange("Выберите режим поиска [1-5]: ", 1, 5);

        List<WasteRequest> result;
        switch (opt) {
            case 1 -> {
                String q = reader.readNonEmptyString("Введите поисковый запрос по адресу: ");
                result = service.searchByAddress(q);
            }
            case 2 -> {
                System.out.println("Выберите статус:");
                RequestStatus[] statuses = RequestStatus.values();
                for (int i = 0; i < statuses.length; i++) {
                    System.out.printf("  %d. %s (%s)\n", i + 1, statuses[i].name(), statuses[i].getTitle());
                }
                int idx = reader.readIntInRange("Статус [1-" + statuses.length + "]: ", 1, statuses.length) - 1;
                result = service.filterByStatus(statuses[idx]);
            }
            case 3 -> {
                System.out.println("Выберите тип отходов:");
                WasteType[] types = WasteType.values();
                for (int i = 0; i < types.length; i++) {
                    System.out.printf("  %d. %s (%s)\n", i + 1, types[i].name(), types[i].getTitle());
                }
                int idx = reader.readIntInRange("Тип [1-" + types.length + "]: ", 1, types.length) - 1;
                result = service.filterByWasteType(types[idx]);
            }
            case 4 -> {
                double minVol = reader.readDouble("Введите минимальный объем (м³): ");
                result = service.filterByMinVolume(minVol);
            }
            case 5 -> {
                Long clientId = reader.readId("Введите ID клиента: ");
                result = service.searchByClientId(clientId);
            }
            default -> result = service.getAllRequests();
        }

        printTable(result);
    }

    private void handleSorting() {
        System.out.println("--- СОРТИРОВКА ЗАЯВОК (STREAM API) ---");
        System.out.println("  1. По дате создания (сначала новые, обратная)");
        System.out.println("  2. По дате создания (сначала старые, прямая)");
        System.out.println("  3. По объему отходов (по возрастанию, прямая)");
        System.out.println("  4. По объему отходов (по убыванию, обратная)");
        int opt = reader.readIntInRange("Выберите порядок сортировки [1-4]: ", 1, 4);

        List<WasteRequest> sorted = switch (opt) {
            case 1 -> service.sortByDate(false);
            case 2 -> service.sortByDate(true);
            case 3 -> service.sortByVolume(true);
            case 4 -> service.sortByVolume(false);
            default -> service.getAllRequests();
        };

        printTable(sorted);
    }

    private void handleStatistics() {
        WasteRequestStats stats = service.getStatistics();
        System.out.println(stats);
    }

    private void handleExportExcel() {
        System.out.println("--- ЭКСПОРТ РЕЕСТРА В EXCEL (APACHE POI) ---");
        List<WasteRequest> all = service.getAllRequests();
        if (all.isEmpty()) {
            System.out.println("[!] Реестр пуст. Экспорт отменен.");
            return;
        }

        String targetPath = "./reports/waste_requests.xlsx";
        System.out.println("Формирование документа...");
        String savedPath = excelExporter.exportRequests(all, targetPath);

        System.out.println("[✓] Файл отчета успешно сформирован!");
        System.out.println("    Расположение: " + savedPath);
        System.out.println("    Всего выгружено строк: " + all.size());
    }

    private void printTable(List<WasteRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            System.out.println("  [ Заявок по вашему запросу не найдено ]");
            return;
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-11s | %-9s | %-16s | %-16s | %s\n",
                "ID", "СТАТУС", "ОБЪЕМ", "ТИП ОТХОДОВ", "ДАТА СОЗДАНИЯ", "АДРЕС И КЛИЕНТ");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        requests.forEach(System.out::println);
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.printf("Всего отображено записей: %d\n", requests.size());
    }
}
