package ru.mirea.wastemanagement.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Менеджер управления подключениями к СУБД PostgreSQL (Паттерн Singleton).
 * Реализует поддержку Twelve-Factor App через чтение переменных окружения
 * с безопасным fallback на файл 'db.properties' или локальные значения по умолчанию.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private final String url;
    private final String user;
    private final String password;

    private DatabaseManager() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("[!] Предупреждение: Не удалось прочитать db.properties, используются значения по умолчанию.");
        }

        // Переменные окружения имеют наивысший приоритет (для CI/CD, Docker и изоляции паролей разработчиков)
        this.url = System.getenv().getOrDefault("DB_URL",
                props.getProperty("db.url", "jdbc:postgresql://localhost:5432/waste_db"));
        this.user = System.getenv().getOrDefault("DB_USER",
                props.getProperty("db.user", "postgres"));
        this.password = System.getenv().getOrDefault("DB_PASSWORD",
                props.getProperty("db.password", "postgres"));

        String driver = props.getProperty("db.driver", "org.postgresql.Driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver не найден в classpath: " + driver, e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Получение физического соединения с БД.
     * Вызывающая сторона ОБЯЗАНА использовать try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Автоматическая инициализация структуры БД (schema.sql) и тестовых данных (data.sql)
     * при первом запуске приложения («работа из коробки»).
     */
    public void initDatabase() {
        try (Connection conn = getConnection()) {
            executeSqlScript(conn, "schema.sql");
            executeSqlScript(conn, "data.sql");
            System.out.println("[✓] СУБД успешно подключена и инициализирована (schema.sql, data.sql)");
        } catch (Exception e) {
            System.err.println("[!] Инициализация БД пропущена или завершилась с ошибкой: " + e.getMessage());
            System.err.println("    Убедитесь, что PostgreSQL запущен и база данных создана.");
        }
    }

    private void executeSqlScript(Connection conn, String scriptPath) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(scriptPath)) {
            if (in == null) {
                System.err.println("[!] SQL-скрипт не найден в ресурсах: " + scriptPath);
                return;
            }
            String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            
            // Выполняем скрипт целиком
            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            }
        }
    }

    public String getUrl() {
        return url;
    }
}
