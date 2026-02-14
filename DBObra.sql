-- =============================================
-- 1. CREACIÓN DE LA BASE DE DATOS
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'Dbobra')
BEGIN
    CREATE DATABASE Dbobra;
END
GO

USE Dbobra;
GO

-- =============================================
-- 2. TABLA DE CARGOS (Evolucionada)
-- =============================================
CREATE TABLE cargos (
    id_cargo INT IDENTITY(1,1) PRIMARY KEY,
    nombre_cargo VARCHAR(50) NOT NULL,
    sueldo_diario DECIMAL(10, 2) NOT NULL,
    area VARCHAR(50) DEFAULT 'General',
    tope_anticipo DECIMAL(10, 2) DEFAULT 0.00
);

-- =============================================
-- 3. TABLA DE TRABAJADORES (Consolidada)
-- =============================================
CREATE TABLE trabajadores (
    id_trabajador INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL, -- Ya renombrado para tu Java
    cedula VARCHAR(15) UNIQUE,
    telefono VARCHAR(15),
    id_cargo INT,
    activo BIT DEFAULT 1,
    FOREIGN KEY (id_cargo) REFERENCES cargos(id_cargo)
);

-- =============================================
-- 4. TABLA DE ASISTENCIA (Con observaciones)
-- =============================================
CREATE TABLE asistencia (
    id_asistencia INT IDENTITY(1,1) PRIMARY KEY,
    id_trabajador INT,
    fecha DATE NOT NULL,
    estado VARCHAR(20) DEFAULT 'ASISTIO' CHECK (estado IN ('ASISTIO', 'FALTA', 'ATRASO', 'MEDIO_DIA')),
    horas_extras INT DEFAULT 0,
    observacion VARCHAR(255), -- Columna necesaria para tu DAO
    FOREIGN KEY (id_trabajador) REFERENCES trabajadores(id_trabajador)
);

-- =============================================
-- 5. TABLA DE ANTICIPOS
-- =============================================
CREATE TABLE anticipos (
    id_anticipo INT IDENTITY(1,1) PRIMARY KEY,
    id_trabajador INT,
    fecha DATE NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    motivo VARCHAR(100),
    FOREIGN KEY (id_trabajador) REFERENCES trabajadores(id_trabajador)
);

-- =============================================
-- 6. DATOS DE PRUEBA INICIALES
-- =============================================
INSERT INTO cargos (nombre_cargo, sueldo_diario, area, tope_anticipo) VALUES 
('Maestro Mayor', 30.00, 'Obra Civil', 50.00),
('Albañil', 22.00, 'Obra Civil', 30.00),
('Peon', 18.00, 'Limpieza', 20.00);

INSERT INTO trabajadores (nombre, cedula, telefono, id_cargo) VALUES 
('Juan Perez', '1712345678', '099111222', 1),
('Carlos Toapanta', '1787654321', '098888777', 2);

INSERT INTO anticipos (id_trabajador, fecha, monto, motivo) VALUES 
(1, CAST(GETDATE() AS DATE), 10.00, 'Pasajes');