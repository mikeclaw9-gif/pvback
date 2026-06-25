CREATE TABLE IF NOT EXISTS producto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio_compra NUMERIC(19,2) NOT NULL,
    precio_venta NUMERIC(19,2) NOT NULL,
    existencia INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT producto_codigo_key UNIQUE (codigo),
    CONSTRAINT producto_existencia_check CHECK (existencia >= 0),
    CONSTRAINT producto_precio_compra_check CHECK (precio_compra >= 0),
    CONSTRAINT producto_precio_venta_check CHECK (precio_venta >= 0)
);
CREATE INDEX IF NOT EXISTS idx_producto_codigo ON producto(codigo);
CREATE INDEX IF NOT EXISTS idx_producto_activo ON producto(activo) WHERE activo = TRUE;
CREATE INDEX IF NOT EXISTS idx_producto_nombre_btree ON producto(nombre);
