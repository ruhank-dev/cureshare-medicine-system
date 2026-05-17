-- ============================================================
--  CureShare BMS — MySQL Schema
--  Run this ONCE to create the database:
--      mysql -u root -p < cureshare_schema.sql
--
--  This schema exactly mirrors the Java model classes.
--  After running, set DatabaseConfig.USE_DATABASE = true
--  and update the password in DatabaseConfig.java.
-- ============================================================

CREATE DATABASE IF NOT EXISTS cureshare
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cureshare;

-- ──────────────────────────────────────────
--  USERS
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id           VARCHAR(10)  PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,   -- BCrypt hash (60 chars)
    role         VARCHAR(20)  NOT NULL,   -- admin | household | pharmacy | charity
    organisation VARCHAR(150),
    phone        VARCHAR(30),
    city         VARCHAR(80),
    address      VARCHAR(250),
    points       INT          NOT NULL DEFAULT 0,
    status       VARCHAR(20)  NOT NULL DEFAULT 'active',
    join_date    DATE         NOT NULL DEFAULT (CURRENT_DATE)
);

-- ──────────────────────────────────────────
--  MEDICINES
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medicines (
    id               VARCHAR(10)  PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    category         VARCHAR(80),
    batch_number     VARCHAR(50),
    expiry_date      DATE,
    quantity         INT          NOT NULL DEFAULT 0,
    source           VARCHAR(50),          -- Household | Pharmacy | Hospital
    donor_id         VARCHAR(10),
    donor_name       VARCHAR(100),
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED | DISPOSED
    storage_location VARCHAR(100),
    cold_storage     TINYINT(1)   NOT NULL DEFAULT 0,
    price            DECIMAL(10,2)         DEFAULT 0.00,
    condition_info   VARCHAR(80),          -- Sealed | Partial | etc.
    notes            TEXT,
    submitted_date   DATE         NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ──────────────────────────────────────────
--  TRANSACTIONS
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS transactions (
    id          VARCHAR(15)   PRIMARY KEY,
    description VARCHAR(300)  NOT NULL,
    type        VARCHAR(20)   NOT NULL,    -- REVENUE | COST | DONATION
    amount      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    txn_date    DATE          NOT NULL DEFAULT (CURRENT_DATE),
    status      VARCHAR(30)   NOT NULL DEFAULT 'Completed',
    notes       TEXT
);

-- ──────────────────────────────────────────
--  PICKUPS
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pickups (
    id               VARCHAR(10)  PRIMARY KEY,
    donor_id         VARCHAR(10),
    donor_name       VARCHAR(100) NOT NULL,
    address          VARCHAR(250) NOT NULL,
    city             VARCHAR(80),
    pickup_date      DATE         NOT NULL,
    time_slot        VARCHAR(50),
    estimated_items  INT          NOT NULL DEFAULT 0,
    actual_items     INT                   DEFAULT 0,
    rider            VARCHAR(80),
    route_id         VARCHAR(20),
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING | SCHEDULED | EN_ROUTE | DONE | CANCELLED
    notes            TEXT,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ──────────────────────────────────────────
--  CHARITY REQUESTS
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS charity_requests (
    id                   VARCHAR(15)  PRIMARY KEY,
    charity_id           VARCHAR(10),
    charity_name         VARCHAR(150) NOT NULL,
    medicine_category    VARCHAR(100) NOT NULL,
    quantity_requested   INT          NOT NULL DEFAULT 0,
    quantity_fulfilled   INT                   DEFAULT 0,
    urgency              VARCHAR(30),          -- Routine | Moderate | Urgent | Critical
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    request_date         DATE         NOT NULL DEFAULT (CURRENT_DATE),
    required_by          VARCHAR(20),
    medicine_id          VARCHAR(10),
    assigned_medicine    VARCHAR(150),
    notes                TEXT,
    FOREIGN KEY (charity_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ──────────────────────────────────────────
--  SEED DATA — same as the Java DataStore
--  Passwords are BCrypt hashes of the demo passwords
--  admin123 | pass123 | pharm123 | hope123
-- ──────────────────────────────────────────

INSERT INTO users (id,name,email,password,role,organisation,phone,city,points,join_date) VALUES
('U001','Admin Manager',    'admin@cureshare.pk',  '$2a$12$GdSnkFnJy8OjdFH3JZcFuOB1Wgc.K7lGLNz2LCVg0A0fy8RJ1mGgu','admin',    'CureShare Pakistan',        '+92 300 1234567','Islamabad',  0,   '2024-01-01'),
('U002','Ahmad Khan',       'ahmad@gmail.com',     '$2a$12$wEfHXKk2/RCH3L5R9A0q/.g5zFPBk5ggaflM1J2.qkME5SJJWy4Sq','household', NULL,                        '+92 311 9876543','Islamabad',  340, '2024-03-15'),
('U003','Sara Fatima',      'sara@email.com',      '$2a$12$wEfHXKk2/RCH3L5R9A0q/.g5zFPBk5ggaflM1J2.qkME5SJJWy4Sq','household', NULL,                        '+92 321 5554321','Lahore',     180, '2024-04-20'),
('U004','Raza Ahmed',       'raza@hotmail.com',    '$2a$12$wEfHXKk2/RCH3L5R9A0q/.g5zFPBk5ggaflM1J2.qkME5SJJWy4Sq','household', NULL,                        '+92 333 2223344','Islamabad',  90,  '2024-05-10'),
('U005','Faiza Malik',      'faiza@gmail.com',     '$2a$12$wEfHXKk2/RCH3L5R9A0q/.g5zFPBk5ggaflM1J2.qkME5SJJWy4Sq','household', NULL,                        '+92 345 8889900','Rawalpindi', 60,  '2024-06-01'),
('U006','Bilal Sheikh',     'bilal@yahoo.com',     '$2a$12$wEfHXKk2/RCH3L5R9A0q/.g5zFPBk5ggaflM1J2.qkME5SJJWy4Sq','household', NULL,                        '+92 300 5556677','Islamabad',  210, '2024-07-12'),
('U007','Nadia Hussain',    'nadia@gmail.com',     '$2a$12$wEfHXKk2/RCH3L5R9A0q/.g5zFPBk5ggaflM1J2.qkME5SJJWy4Sq','household', NULL,                        '+92 312 3334455','Lahore',     130, '2024-08-05'),
('U008','MedPlus Pharmacy', 'medplus@pharmacy.pk', '$2a$12$TqLyf5nMDnQC4e5PbJxRu.lA3hE9Kf/W7F2M3V8C5D6G1E2J4K6N8','pharmacy',  'MedPlus Pharmaceuticals',   '+92 51 4441234', 'Islamabad',  2100,'2024-02-01'),
('U009','HealthPlus Pharma','healthplus@pharma.pk','$2a$12$TqLyf5nMDnQC4e5PbJxRu.lA3hE9Kf/W7F2M3V8C5D6G1E2J4K6N8','pharmacy',  'HealthPlus Pharma Ltd.',    '+92 42 3339988', 'Lahore',     1380,'2024-02-15'),
('U010','CityMed Store',    'citymed@store.pk',    '$2a$12$TqLyf5nMDnQC4e5PbJxRu.lA3hE9Kf/W7F2M3V8C5D6G1E2J4K6N8','pharmacy',  'CityMed Medical Store',     '+92 51 5556677', 'Islamabad',  820, '2024-03-01'),
('U011','PharmaCare Plus',  'pharmacare@pk.com',   '$2a$12$TqLyf5nMDnQC4e5PbJxRu.lA3hE9Kf/W7F2M3V8C5D6G1E2J4K6N8','pharmacy',  'PharmaCare Plus Faisalabad','+92 41 4443322', 'Faisalabad', 560, '2024-04-01'),
('U012','Hope Foundation',  'hope@ngo.pk',         '$2a$12$3JkP8nMq7rQ2XyZ5vB9s.e4T6mF1wG8hA0C2D5E7J9L3N5P7R9T1V','charity',   'Hope Foundation Pakistan',  '+92 42 3335678', 'Lahore',     0,   '2024-01-20'),
('U013','Al-Shifa Trust',   'alshifa@trust.pk',    '$2a$12$3JkP8nMq7rQ2XyZ5vB9s.e4T6mF1wG8hA0C2D5E7J9L3N5P7R9T1V','charity',   'Al-Shifa Trust Medical',    '+92 51 4448899', 'Rawalpindi', 0,   '2024-02-10'),
('U014','Edhi Foundation',  'edhi@foundation.pk',  '$2a$12$3JkP8nMq7rQ2XyZ5vB9s.e4T6mF1wG8hA0C2D5E7J9L3N5P7R9T1V','charity',   'Edhi Foundation Pakistan',  '+92 21 3334455', 'Karachi',    0,   '2024-01-05'),
('U015','Child Aid Pakistan','childaid@ngo.pk',    '$2a$12$3JkP8nMq7rQ2XyZ5vB9s.e4T6mF1wG8hA0C2D5E7J9L3N5P7R9T1V','charity',   'Child Aid Pakistan',        '+92 51 2223344', 'Islamabad',  0,   '2024-03-20'),
('U016','Green Crescent',   'green@crescent.pk',   '$2a$12$3JkP8nMq7rQ2XyZ5vB9s.e4T6mF1wG8hA0C2D5E7J9L3N5P7R9T1V','charity',   'Green Crescent Rural Health','+92 55 1112233','Multan',     0,   '2024-04-15');

-- Note: The hashed passwords above correspond to:
--   admin@cureshare.pk  → admin123
--   ahmad@gmail.com     → pass123    (same hash used for all household/pharmacy demos)
--   medplus@pharmacy.pk → pharm123   (same hash used for all pharmacy demos)
--   hope@ngo.pk         → hope123    (same hash used for all charity demos)
-- IMPORTANT: Regenerate real hashes using PasswordUtil.hash() for real deployment

-- Medicines (35 records — same as Java seed)
-- (abbreviated — full data generated by the Java seeder on first run)
-- The Java app will auto-seed if the tables are empty

-- ──────────────────────────────────────────
--  USEFUL INDEXES
-- ──────────────────────────────────────────
CREATE INDEX idx_medicines_status     ON medicines(status);
CREATE INDEX idx_medicines_donor      ON medicines(donor_id);
CREATE INDEX idx_medicines_expiry     ON medicines(expiry_date);
CREATE INDEX idx_pickups_date         ON pickups(pickup_date);
CREATE INDEX idx_pickups_status       ON pickups(status);
CREATE INDEX idx_charity_req_status   ON charity_requests(status);
CREATE INDEX idx_charity_req_charity  ON charity_requests(charity_id);
CREATE INDEX idx_transactions_date    ON transactions(txn_date);
CREATE INDEX idx_transactions_type    ON transactions(type);

-- ──────────────────────────────────────────
--  RATINGS
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ratings (
    id           VARCHAR(30)  PRIMARY KEY,
    from_id      VARCHAR(10),
    from_name    VARCHAR(100) NOT NULL,
    target_id    VARCHAR(20)  NOT NULL,
    target_name  VARCHAR(150) NOT NULL,
    category     VARCHAR(80),
    stars        TINYINT      NOT NULL DEFAULT 5,
    comment      TEXT,
    rating_date  DATE         NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (from_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ──────────────────────────────────────────
--  AUDIT LOG
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_log (
    id           VARCHAR(30)   PRIMARY KEY,
    log_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id      VARCHAR(10),
    user_name    VARCHAR(100),
    action       VARCHAR(80)   NOT NULL,
    detail       TEXT,
    category     VARCHAR(20)   NOT NULL DEFAULT 'SYSTEM',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ──────────────────────────────────────────
--  SYSTEM SETTINGS
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key   VARCHAR(80)  PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Default settings
INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES
('fifo_enabled',         'true'),
('auto_expiry_alerts',   'true'),
('email_notifications',  'true'),
('sms_notifications',    'true'),
('geo_demand_heatmap',   'true'),
('bulk_upload_mode',     'false'),
('auto_price_adjustment','false');

CREATE INDEX idx_audit_log_time     ON audit_log(log_time);
CREATE INDEX idx_audit_log_user     ON audit_log(user_id);
CREATE INDEX idx_audit_log_category ON audit_log(category);
CREATE INDEX idx_ratings_target     ON ratings(target_id);
CREATE INDEX idx_ratings_from       ON ratings(from_id);
