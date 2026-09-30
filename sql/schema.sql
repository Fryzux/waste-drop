-- ==========================================================
-- СХЕМА БАЗЫ ДАННЫХ: СИСТЕМА УЧЕТА ЗАЯВОК НА ВЫВОЗ ОТХОДОВ
-- ==========================================================

-- 1. Таблица клиентов (физические и юридические лица)
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- 2. Таблица заявок на вывоз отходов
CREATE TABLE IF NOT EXISTS waste_requests (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    address VARCHAR(500) NOT NULL,
    waste_type VARCHAR(50) NOT NULL,
    volume_m3 NUMERIC(6, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ограничение целостности: запрет удаления клиента при наличии заявок
    CONSTRAINT fk_waste_requests_client 
        FOREIGN KEY (client_id) 
        REFERENCES clients(id) 
        ON DELETE RESTRICT,
        
    -- Защитная валидация допустимого объема отходов на уровне ядра СУБД
    CONSTRAINT chk_volume_range 
        CHECK (volume_m3 >= 0.1 AND volume_m3 <= 100.0)
);

-- Индексы для оптимизации выборок по внешнему ключу и статусу
CREATE INDEX IF NOT EXISTS idx_requests_client_id ON waste_requests(client_id);
CREATE INDEX IF NOT EXISTS idx_requests_status ON waste_requests(status);
CREATE INDEX IF NOT EXISTS idx_requests_created_at ON waste_requests(created_at);
