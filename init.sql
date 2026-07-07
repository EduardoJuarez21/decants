-- Script SQL opcional para crear la tabla manualmente
-- Spring Boot con ddl-auto: update la crea automáticamente al iniciar.
-- Usa este script si prefieres control total sobre el esquema.

CREATE TABLE IF NOT EXISTS pedidos (
    id               BIGSERIAL PRIMARY KEY,
    nombre_cliente   VARCHAR(255)  NOT NULL,
    telefono         VARCHAR(50)   NOT NULL,
    email            VARCHAR(255),
    tipo_producto    VARCHAR(255)  NOT NULL,
    cantidad         INTEGER       NOT NULL CHECK (cantidad > 0),
    texto_personalizado VARCHAR(100),
    color_preferido  VARCHAR(100),
    fecha_evento     DATE,
    comentarios      VARCHAR(500),
    estado_pedido    VARCHAR(50)   NOT NULL DEFAULT 'NUEVO',
    fecha_creacion   TIMESTAMP     NOT NULL,
    fecha_actualizacion TIMESTAMP  NOT NULL
);

-- Índice para consultas frecuentes por estado
CREATE INDEX IF NOT EXISTS idx_pedidos_estado ON pedidos (estado_pedido);
