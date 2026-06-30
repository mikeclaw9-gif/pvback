CREATE TABLE IF NOT EXISTS cortes_caja (
    id BIGSERIAL PRIMARY KEY,
    fecha_apertura TIMESTAMP NOT NULL,
    fecha_cierre TIMESTAMP,
    monto_inicial NUMERIC(19,2) NOT NULL,
    monto_final NUMERIC(19,2),
    total_ventas NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_gastos NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_efectivo NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_tarjeta NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_transferencia NUMERIC(19,2) NOT NULL DEFAULT 0,
    diferencia NUMERIC(19,2) NOT NULL DEFAULT 0,
    observacion VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    usuario_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_cortes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_cortes_caja_estado ON cortes_caja(estado);
CREATE INDEX IF NOT EXISTS idx_cortes_caja_fecha ON cortes_caja(fecha_apertura);
CREATE INDEX IF NOT EXISTS idx_cortes_caja_activo ON cortes_caja(activo) WHERE activo = TRUE;
