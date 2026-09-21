package ru.mirea.wastemanagement.ui;

import ru.mirea.wastemanagement.model.StandardContainer;
import ru.mirea.wastemanagement.util.VolumeCalculator;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

/**
 * Безопасный ридер пользовательского ввода из консоли.
 * Архитектурные гарантии:
 * 1. Исключение зависаний буфера Scanner (чтение исключительно через nextLine()).
 * 2. Кроссплатформенность локалей (нормализация запятой в точку перед парсингом double).
 * 3. Неубиваемость: циклы повторного ввода при некорректных данных без падения JVM.
 * 4. Интерактивный ассистент выбора объема отходов.
 */
public class InputReader {
    private final Scanner scanner;

    public InputReader() {
        // Принудительная фиксация кодировки и локали ввода
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    /**
     * Чтение непустой строки.
     */
    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  [!] Значение не может быть пустым. Повторите ввод.");
        }
    }

    /**
     * Чтение целого числа в заданном диапазоне.
     */
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf(Locale.US, "  [!] Число должно быть в диапазоне от %d до %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Ошибка: Введите корректное целое число.");
            }
        }
    }

    /**
     * Чтение вещественного числа в заданном диапазоне с поддержкой запятой и точки.
     */
    public double readDoubleInRange(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            // Нормализация разделителя дробной части (Windows Russian locale vs US)
            line = line.replace(',', '.');
            try {
                double val = Double.parseDouble(line);
                if (val >= min && val <= max) {
                    return Math.round(val * 100.0) / 100.0;
                }
                System.out.printf(Locale.US, "  [!] Значение должно быть в диапазоне от %.2f до %.2f.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Ошибка: Введите корректное число (например: 12.5 или 12,5).");
            }
        }
    }

    /**
     * Чтение ID сущности (положительный Long).
     */
    public Long readId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                long val = Long.parseLong(line);
                if (val > 0) {
                    return val;
                }
                System.out.println("  [!] ID должен быть положительным числом.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Ошибка: Введите корректный числовой ID.");
            }
        }
    }

    /**
     * Интерактивный мастер определения объема отходов:
     * 1. Прямой ввод в кубометрах
     * 2. Расчет по габаритам (Длина × Ширина × Высота)
     * 3. Расчет по типовой таре ЖКХ/ГОСТ
     */
    public double promptVolume() {
        System.out.println("\n  --- Определение объема отходов ---");
        System.out.println("  1. Ввести объем напрямую в кубических метрах (м³)");
        System.out.println("  2. Рассчитать по габаритам (Длина × Ширина × Высота)");
        System.out.println("  3. Рассчитать по типовой таре (мешки, евробаки, бункеры)");

        int mode = readIntInRange("  Выберите способ [1-3]: ", 1, 3);

        switch (mode) {
            case 1 -> {
                return readDoubleInRange("  Введите объем отходов (м³): ", 0.1, 100.0);
            }
            case 2 -> {
                System.out.println("  [ Мастер расчета по габаритам ]");
                double length = readDoubleInRange("    Длина (м): ", 0.1, 20.0);
                double width = readDoubleInRange("    Ширина (м): ", 0.1, 20.0);
                double height = readDoubleInRange("    Высота (м): ", 0.1, 10.0);
                double volume = VolumeCalculator.calculateByDimensions(length, width, height);
                System.out.printf(Locale.US, "  [✓] Рассчитанный объем: %.2f м³\n", volume);
                return volume;
            }
            case 3 -> {
                System.out.println("  [ Мастер расчета по типовой таре ]");
                StandardContainer[] containers = StandardContainer.values();
                for (int i = 0; i < containers.length; i++) {
                    System.out.printf(Locale.US, "    %d. %s\n", i + 1, containers[i].toString());
                }
                int cIdx = readIntInRange("    Выберите тару [1-" + containers.length + "]: ", 1, containers.length) - 1;
                int count = readIntInRange("    Количество единиц тары (шт): ", 1, 100);
                double volume = VolumeCalculator.calculateByContainers(containers[cIdx], count);
                System.out.printf(Locale.US, "  [✓] Рассчитанный объем (%d шт.): %.2f м³\n", count, volume);
                return volume;
            }
            default -> throw new IllegalStateException();
        }
    }
}
