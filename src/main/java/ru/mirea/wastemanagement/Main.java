package ru.mirea.wastemanagement;

import ru.mirea.wastemanagement.repository.ClientRepository;
import ru.mirea.wastemanagement.repository.WasteRequestRepository;
import ru.mirea.wastemanagement.repository.impl.JdbcClientRepository;
import ru.mirea.wastemanagement.repository.impl.JdbcWasteRequestRepository;
import ru.mirea.wastemanagement.service.WasteRequestService;
import ru.mirea.wastemanagement.ui.ConsoleMenu;
import ru.mirea.wastemanagement.util.DatabaseManager;
import ru.mirea.wastemanagement.util.ExcelExporter;

/**
 * Главная точка входа в приложение (Main Class).
 * Реализует ручное связывание зависимостей (Manual Dependency Injection)
 * без использования тяжеловесных фреймворков.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println(">>> Инициализация системы учета заявок на вывоз отходов...");

        // 1. Инициализация инфраструктурного слоя БД
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.initDatabase();

        // 2. Инициализация слоя доступа к данным (Persistence Layer)
        ClientRepository clientRepository = new JdbcClientRepository(databaseManager);
        WasteRequestRepository wasteRequestRepository = new JdbcWasteRequestRepository(databaseManager);

        // 3. Инициализация сервисного слоя (Business Logic Layer)
        WasteRequestService requestService = new WasteRequestService(wasteRequestRepository, clientRepository);
        ExcelExporter excelExporter = new ExcelExporter();

        // 4. Инициализация слоя представления (Presentation Layer)
        ConsoleMenu menu = new ConsoleMenu(requestService, clientRepository, excelExporter);

        // 5. Запуск интерактивного консольного интерфейса
        menu.start();
    }
}
