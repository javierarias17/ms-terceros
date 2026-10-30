CREATE TABLE IF NOT EXISTS tercero_registrations (
    id VARCHAR(36) PRIMARY KEY,
    calidad VARCHAR(50),
    nacionalidad VARCHAR(50),
    tipo_persona VARCHAR(50),
    correo VARCHAR(255),
    nombre VARCHAR(255),
    cedula VARCHAR(50),
    fecha_expedicion_cedula VARCHAR(20),
    lugar_nacimiento VARCHAR(100),
    pais_nacimiento VARCHAR(100),
    nit VARCHAR(50),
    celular VARCHAR(30),
    direccion VARCHAR(255),
    estado VARCHAR(20) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tercero_documents (
    id VARCHAR(36) PRIMARY KEY,
    tercero_registration_id VARCHAR(36) NOT NULL REFERENCES tercero_registrations(id),
    document_key VARCHAR(100) NOT NULL,
    url TEXT NOT NULL,
    file_name VARCHAR(255),
    content_type VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tercero_documents_registration_id
    ON tercero_documents (tercero_registration_id);
