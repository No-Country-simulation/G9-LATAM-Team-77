ALTER TABLE usuarios
    ADD COLUMN google_subject VARCHAR(255) NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_google_subject UNIQUE (google_subject);
