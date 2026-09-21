package ru.mirea.wastemanagement.ui;

import ru.mirea.wastemanagement.exception.BusinessException;
import ru.mirea.wastemanagement.exception.EntityNotFoundException;
import ru.mirea.wastemanagement.model.Client;
import ru.mirea.wastemanagement.model.RequestStatus;
import ru.mirea.wastemanagement.model.WasteRequest;
import ru.mirea.wastemanagement.model.WasteType;
import ru.mirea.wastemanagement.repository.ClientRepository;
import ru.mirea.wastemanagement.service.WasteRequestService;
import ru.mirea.wastemanagement.service.WasteRequestStats;
import ru.mirea.wastemanagement.util.ExcelExporter;

import java.util.List;

/**
 * Консольное интерактивное меню пользователя.
 * Реализует паттерн Presentation Controller с глобальным перехватом
 * бизнес-исключений для предотвращения сбоев JVM во время демонстрации.
 */
public class ConsoleMenu {
    private final WasteRequestService service;
    private final ClientRepository clientRepository;
    private final ExcelExporter excelExporter;
    private final InputReader reader;

    public ConsoleMenu(WasteRequestService service, ClientRepository clientRepository, ExcelExporter excelExporter) {
        this.service = service;
        this.clientRepository = clientRepository;
        this.excelExporter = excelExporter;
        this.reader = new InputReader();
    }

    public void start() {
        printBanner();
        while (true) {
            printMainMenu();
            int choice = reader.readIntInRange("Выберите пункт меню [0-9]: ", 0, 9);
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
        System.out.println("  1. База клиентов (просмотр и регистрация нового контрагента)");
        System.out.println("  2. Оформить новую заявку на вывоз отходов (с мастером объема)");
        System.out.println("  3. Показать полный реестр заявок");
        System.out.println("  4. Сменить статус заявки (контроль конечного автомата FSM)");
        System.out.println("  5. Удалить заявку из реестра");
        System.out.println("  6. Поиск и фильтрация (по адресу, клиенту, типу, статусу)");
        System.out.println("  7. Сортировка заявок (по дате оформления или объему)");
        System.out.println("  8. Аналитика и статистика реестра (5 ключевых метрик)");
        System.out.println("  9. Экспорт реестра в Microsoft Excel (.xlsx)");
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
            default -> System.out.println("Неизвестный пункт.");
        }
    }

    // 1. Клиенты
    private void handleClientsSubmenu() {
        System.out.println("--- БАЗА КЛИЕНТОВ ---");
        System.out.println("1. Список всех клиентов");
        System.out.println("2. Зарегистрировать нового клиента");
        int sub = reader.readIntInRange("Выберите действие [1-2]: ", 1, 2);

        if (sub == 1) {
            List<Client> clients = clientRepository.findAll();
            if (clients.isEmpty()) {
                System.out.println("Клиенты не найдены.");
            } else {
                System.out.println("\nСПИСОК КЛИЕНТОВ:");
                clients.forEach(System.out::println);
            }
        } else {
            String name = reader.readNonEmptyString("Введите наименование организации / ФИО: ");
            String phone = reader.readNonEmptyString("Введите контактный телефон: ");
            String email = reader.readNonEmptyString("Введите электронную почту: ");
            Client created = clientRepository.save(new Client(name, phone, email));
            System.out.printf("[✓] Клиент успешно зарегистрирован с ID=%d!\n", created.getId());
        }
    }

    // 2. Создание заявки
    private void handleCreateRequest() {
        System.out.println("--- ОФОРМЛЕНИЕ НОВОЙ ЗАЯВКИ ---");
        Long clientId = reader.readId("Введите ID клиента: ");

        // Проверка клиента до дальнейшего ввода
        clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID=" + clientId + " не существует! Сначала зарегистрируйте клиента в меню 1."));

        String address = reader.readNonEmptyString("Введите точный адрес вывоза (от 5 символов): ");

        System.out.println("\nВыберите тип отходов:");
        WasteType[] types = WasteType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("  %d. %s (%s)\n", i + 1, types[i].name(), types[i].getTitle());
        }
        int typeIdx = reader.readIntInRange("Выберите тип [1-" + types.length + "]: ", 1, types.length) - 1;
        WasteType selectedType = types[typeIdx];

        // Использование интерактивного мастера объема отходов
        double volumeM3 = reader.promptVolume();

        WasteRequest created = service.createRequest(clientId, address, selectedType, volumeM3);
        System.out.printf("\n[✓] Заявка #%d успешно создана со статусом 'НОВАЯ'!\n", created.getId());
        System.out.println(created);
    }

    // 3. Просмотр всех
    private void handleViewAllRequests() {
        System.out.println("--- РЕЕСТР ВСЕХ ЗАЯВОК НА ВЫВОЗ ОТХОДОВ ---");
        List<WasteRequest> list = service.getAllRequests();
        printTable(list);
    }

    // 4. Смена статуса
    private void handleUpdateStatus() {
        System.out.println("--- ИЗМЕНЕНИЕ СТАТУСА ЗАЯВКИ ---");
        Long id = reader.readId("Введите ID заявки: ");
        WasteRequest req = service.getById(id);

        System.out.println("Текущая заявка:");
        System.out.println(req);
        System.out.println("Текущий статус: " + req.getStatus().getTitle());

        System.out.println("\nВыберите новый целевой статус:");
        RequestStatus[] statuses = RequestStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            boolean allowed = req.getStatus().canTransitionTo(statuses[i]);
            System.out.printf("  %d. %-12s %s\n", 
                    i + 1, statuses[i].getTitle(), allowed ? "[РАЗРЕШЕНО]" : "[ЗАПРЕЩЕНО АВТОМАТОМ]");
        }

        int sIdx = reader.readIntInRange("Выберите статус [1-" + statuses.length + "]: ", 1, statuses.length) - 1;
        WasteRequest updated = service.updateStatus(id, statuses[sIdx]);
        System.out.printf("[✓] Статус заявки #%d успешно обновлен на '%s'!\n", id, updated.getStatus().getTitle());
    }

    // 5. Удаление заявки
    private void handleDeleteRequest() {
        System.out.println("--- УДАЛЕНИЕ ЗАЯВКИ ---");
        Long id = reader.readId("Введите ID удаляемой заявки: ");
        service.deleteRequest(id);
        System.out.printf("[✓] Заявка #%d успешно удалена из реестра.\n", id);
    }

    // 6. Поиск и фильтрация
    private void handleSearchAndFilter() {
        System.out.println("--- ПОИСК И ФИЛЬТРАЦИЯ (STREAM API) ---");
        System.out.println("1. Поиск по фрагменту адреса (регистронезависимый)");
        System.out.println("2. Поиск по ID клиента");
        System.out.println("3. Фильтрация по статусу заявки");
        System.out.println("4. Фильтрация по типу отходов");

        int opt = reader.readIntInRange("Выберите тип фильтра [1-4]: ", 1, 4);
        List<WasteRequest> result;

        switch (opt) {
            case 1 -> {
                String query = reader.readNonEmptyString("Введите поисковый запрос адреса: ");
                result = service.searchByAddress(query);
            }
            case 2 -> {
                Long cId = reader.readId("Введите ID клиента: ");
                result = service.searchByClientId(cId);
            }
            case 3 -> {
                RequestStatus[] st = RequestStatus.values();
                for (int i = 0; i < st.length; i++) {
                    System.out.printf("  %d. %s\n", i + 1, st[i].getTitle());
                }
                int idx = reader.readIntInRange("Выберите статус: ", 1, st.length) - 1;
                result = service.filterByStatus(st[idx]);
            }
            case 4 -> {
                WasteType[] wt = WasteType.values();
                for (int i = 0; i < wt.length; i++) {
                    System.out.printf("  %d. %s\n", i + 1, wt[i].getTitle());
                }
                int idx = reader.readIntInRange("Выберите тип: ", 1, wt.length) - 1;
                result = service.filterByWasteType(wt[idx]);
            }
            default -> result = List.of();
        }

        printTable(result);
    }

    // 7. Сортировка
    private void handleSorting() {
        System.out.println("--- СОРТИРОВКА ЗАЯВОК (STREAM API) ---");
        System.out.println("1. По дате оформления (сначала новые)");
        System.out.println("2. По дате оформления (сначала старые)");
        System.out.println("3. По объему (по возрастанию)");
        System.out.println("4. По объему (по убыванию)");

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

    // 8. Статистика
    private void handleStatistics() {
        WasteRequestStats stats = service.getStatistics();
        System.out.println(stats);
    }

    // 9. Экспорт в Excel
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
