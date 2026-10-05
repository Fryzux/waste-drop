package ru.mirea.project;

import ru.mirea.project.repository.ClientRepository;
import ru.mirea.project.repository.VehicleRepository;
import ru.mirea.project.repository.WasteRequestRepository;
import ru.mirea.project.repository.impl.JdbcClientRepository;
import ru.mirea.project.repository.impl.JdbcVehicleRepository;
import ru.mirea.project.repository.impl.JdbcWasteRequestRepository;
import ru.mirea.project.service.ClientService;
import ru.mirea.project.service.VehicleService;
import ru.mirea.project.service.WasteRequestService;
import ru.mirea.project.ui.ConsoleMenu;
import ru.mirea.project.util.DatabaseManager;
import ru.mirea.project.util.ExcelExporter;

/**
 * Главная точка входа в приложение (Main Class).
 * Реализует ручное связывание зависимостей (Manual Dependency Injection)
 * без использования тяжеловесных фреймворков.
 * Связывает 3 доменные сущности (по 1 сущности на каждого участника команды).
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
        VehicleRepository vehicleRepository = new JdbcVehicleRepository(databaseManager);

        // 3. Инициализация сервисного слоя (Business Logic Layer)
        ClientService clientService = new ClientService(clientRepository);
        WasteRequestService requestService = new WasteRequestService(wasteRequestRepository, clientRepository);
        VehicleService vehicleService = new VehicleService(vehicleRepository);
        ExcelExporter excelExporter = new ExcelExporter();

        // 4. Инициализация слоя представления (Presentation Layer)
        ConsoleMenu menu = new ConsoleMenu(requestService, clientService, vehicleService, excelExporter);

        // 5. Запуск интерактивного консольного интерфейса
        menu.start();
    }
}
