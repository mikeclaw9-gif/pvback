CREATE TABLE IF NOT EXISTS gastos (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    monto NUMERIC(19,2) NOT NULL,
    fecha_gasto TIMESTAMP NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    metodo_pago VARCHAR(50),
    observacion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT gastos_monto_check CHECK (monto > 0)
);

CREATE INDEX IF NOT EXISTS idx_gastos_categoria ON gastos(categoria);
CREATE INDEX IF NOT EXISTS idx_gastos_fecha ON gastos(fecha_gasto);
CREATE INDEX IF NOT EXISTS idx_gastos_activo ON gastos(activo) WHERE activo = TRUE;
