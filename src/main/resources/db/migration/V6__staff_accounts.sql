ALTER TABLE admin_user ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE admin_user ADD CONSTRAINT ck_admin_user_role
    CHECK (role IN ('ROLE_ADMIN', 'ROLE_STAFF'));