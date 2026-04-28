ALTER TABLE produto
    DROP CONSTRAINT IF EXISTS produto_dispositivo_ultima_alteracao_fkey;

ALTER TABLE movimentacao_estoque
    DROP CONSTRAINT IF EXISTS movimentacao_estoque_dispositivo_id_fkey;

ALTER TABLE sync_log
    DROP CONSTRAINT IF EXISTS sync_log_dispositivo_id_fkey;

ALTER TABLE sync_log
    ADD COLUMN usuario_id UUID;

UPDATE produto p
SET usuario_ultima_alteracao = d.usuario_id
FROM dispositivo d
WHERE p.dispositivo_ultima_alteracao = d.id
  AND p.usuario_ultima_alteracao IS NULL
  AND d.usuario_id IS NOT NULL;

UPDATE sync_log s
SET usuario_id = d.usuario_id
FROM dispositivo d
WHERE s.dispositivo_id = d.id
  AND d.usuario_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_sync_log_usuario_id
    ON sync_log (usuario_id);

ALTER TABLE sync_log
    ADD CONSTRAINT sync_log_usuario_id_fkey
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL;

DROP INDEX IF EXISTS idx_mov_dispositivo_id;
DROP INDEX IF EXISTS idx_sync_log_dispositivo_id;

ALTER TABLE movimentacao_estoque
    DROP COLUMN IF EXISTS dispositivo_id;

ALTER TABLE produto
    DROP COLUMN IF EXISTS dispositivo_ultima_alteracao;

ALTER TABLE sync_log
    DROP COLUMN IF EXISTS dispositivo_id;

DROP TABLE IF EXISTS dispositivo;
