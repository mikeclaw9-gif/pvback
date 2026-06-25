ALTER TABLE clientes ADD COLUMN IF NOT EXISTS documento VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_clientes_documento ON clientes(documento);
