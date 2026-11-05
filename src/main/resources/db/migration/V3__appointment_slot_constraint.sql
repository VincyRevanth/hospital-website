ALTER TABLE appointment ADD COLUMN confirmed_slot_key VARCHAR(60);

ALTER TABLE appointment
    ADD CONSTRAINT uq_appointment_confirmed_slot UNIQUE (confirmed_slot_key);

ALTER TABLE appointment
    ADD CONSTRAINT ck_appointment_slot_key_status
    CHECK (confirmed_slot_key IS NULL OR status = 'CONFIRMED');