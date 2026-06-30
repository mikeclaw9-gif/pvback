ALTER TABLE ventas ADD COLUMN IF NOT EXISTS metodo_pago VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_ventas_metodo_pago ON ventas(metodo_pago);
