-- ==========================================================
-- ТЕСТОВЫЕ ДАННЫЕ ДЛЯ ДЕМОНСТРАЦИИ И СДАЧИ ПРОЕКТА
-- ==========================================================

-- Клиенты (5 разнородных контрагентов)
INSERT INTO clients (id, name, phone, email) VALUES
(1, 'ООО "ЭкоСтрой-Сервис"', '+7 (495) 101-20-30', 'info@ecostroy.ru'),
(2, 'ИП Иванов Алексей Сергеевич', '+7 (916) 222-33-44', 'ivanov.waste@mail.ru'),
(3, 'ТСЖ "Вернадский квартал"', '+7 (495) 333-44-55', 'tsj_vernad@yandex.ru'),
(4, 'ПАО "ТехноПарк Юго-Запад"', '+7 (499) 777-88-99', 'service@technopark.ru'),
(5, 'Кузнецова Елена Павловна', '+7 (903) 555-11-22', 'kuznetsova.e@gmail.com')
ON CONFLICT (id) DO NOTHING;

-- Заявки с различными типами отходов, статусами и датами создания
INSERT INTO waste_requests (id, client_id, address, waste_type, volume_m3, status, created_at) VALUES
(1, 1, 'г. Москва, пр-т Вернадского, д. 78, стр. 4', 'CONSTRUCTION', 25.50, 'COMPLETED', '2026-03-01 10:30:00'),
(2, 1, 'г. Москва, ул. Удальцова, д. 15', 'CONSTRUCTION', 42.00, 'IN_PROGRESS', '2026-03-15 14:00:00'),
(3, 2, 'г. Москва, Ленинский пр-т, д. 120', 'MUNICIPAL', 5.00, 'COMPLETED', '2026-03-05 09:15:00'),
(4, 2, 'г. Москва, ул. Миклухо-Маклая, д. 6', 'BULKY', 12.00, 'NEW', '2026-03-18 16:45:00'),
(5, 3, 'г. Москва, ул. Коштоянца, д. 47 к. 1', 'MUNICIPAL', 8.50, 'IN_PROGRESS', '2026-03-12 11:20:00'),
(6, 3, 'г. Москва, пр-т Вернадского, д. 86', 'BULKY', 15.00, 'CANCELLED', '2026-03-02 17:00:00'),
(7, 4, 'г. Москва, 2-й Южнопортовый проезд, д. 10', 'HAZARDOUS', 2.50, 'NEW', '2026-03-19 10:00:00'),
(8, 4, 'г. Москва, ш. Энтузиастов, д. 56', 'CONSTRUCTION', 30.00, 'COMPLETED', '2026-03-08 12:30:00'),
(9, 5, 'г. Москва, ул. Вавилова, д. 28', 'BULKY', 3.20, 'NEW', '2026-03-20 08:30:00'),
(10, 5, 'г. Москва, ул. Профсоюзная, д. 43', 'MUNICIPAL', 1.80, 'COMPLETED', '2026-03-10 15:10:00')
ON CONFLICT (id) DO NOTHING;

-- Обновляем типы клиентов
UPDATE clients SET client_type = 'LEGAL_ENTITY' WHERE id IN (1, 4) AND client_type = 'INDIVIDUAL';
UPDATE clients SET client_type = 'MUNICIPAL_ORG' WHERE id = 3 AND client_type = 'INDIVIDUAL';

-- Спецтранспорт автопарка (5 единиц)
INSERT INTO vehicles (id, license_plate, model_name, capacity_m3, status) VALUES
(1, 'А101МР77', 'КАМАЗ КО-440-5 (Мусоровоз)', 22.00, 'AVAILABLE'),
(2, 'В202МР77', 'МАЗ КО-449 (Пресс-мусоровоз)', 16.50, 'AVAILABLE'),
(3, 'С303МР77', 'Scania G410 (Тяжелый бункеровоз)', 32.00, 'ON_ROUTE'),
(4, 'Е404МР77', 'ГАЗон NEXT (Малый бункеровоз)', 8.00, 'MAINTENANCE'),
(5, 'К505МР77', 'MAN TGS 26.360 (Крюковой погрузчик)', 27.00, 'AVAILABLE')
ON CONFLICT (id) DO NOTHING;

-- Персонал и экипажи (5 сотрудников)
INSERT INTO workers (id, full_name, phone, role, salary) VALUES
(1, 'Петров Василий Иванович', '+7 (916) 111-22-33', 'DRIVER', 85000.00),
(2, 'Сидоров Алексей Михайлович', '+7 (925) 222-33-44', 'DRIVER', 82000.00),
(3, 'Ковалев Дмитрий Андреевич', '+7 (903) 333-44-55', 'LOADER', 60000.00),
(4, 'Морозов Сергей Павлович', '+7 (999) 444-55-66', 'LOADER', 58000.00),
(5, 'Соколова Анна Викторовна', '+7 (495) 555-66-77', 'DISPATCHER', 75000.00)
ON CONFLICT (id) DO NOTHING;

-- Корректировка автоинкрементных последовательностей
SELECT setval('clients_id_seq', COALESCE((SELECT MAX(id) FROM clients), 1));
SELECT setval('waste_requests_id_seq', COALESCE((SELECT MAX(id) FROM waste_requests), 1));
SELECT setval('vehicles_id_seq', COALESCE((SELECT MAX(id) FROM vehicles), 1));
SELECT setval('workers_id_seq', COALESCE((SELECT MAX(id) FROM workers), 1));
