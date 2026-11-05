CREATE TABLE hospital_settings (
    id            BIGINT PRIMARY KEY,
    phone         VARCHAR(30),
    email         VARCHAR(150),
    address       VARCHAR(500),
    opening_hours VARCHAR(500),
    map_url       VARCHAR(500),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_hospital_settings_single_row CHECK (id = 1)
);

INSERT INTO hospital_settings (id, phone) VALUES (1, '+91 79811 01666');