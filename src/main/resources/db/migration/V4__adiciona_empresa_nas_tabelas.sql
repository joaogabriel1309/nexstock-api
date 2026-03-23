-- Adiciona empresa_id em contrato
ALTER TABLE contrato
    ADD COLUMN empresa_id UUID;
ALTER TABLE contrato
    ADD CONSTRAINT fk_contrato_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);
CREATE INDEX idx_contrato_empresa_id ON contrato(empresa_id);
ALTER TABLE contrato
DROP CONSTRAINT IF EXISTS fk_contrato_cliente,
    DROP COLUMN IF EXISTS cliente_id;

-- Atualiza usuario: troca contrato_id por empresa_id
ALTER TABLE usuario
    ADD COLUMN empresa_id UUID,
    ADD CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);
CREATE INDEX idx_usuario_empresa_id ON usuario(empresa_id);
ALTER TABLE usuario
DROP CONSTRAINT IF EXISTS uq_usuario_email_contrato;
ALTER TABLE usuario
    ADD CONSTRAINT uq_usuario_email UNIQUE (email);
ALTER TABLE usuario
DROP CONSTRAINT IF EXISTS fk_usuario_contrato,
    DROP COLUMN IF EXISTS contrato_id;

-- Atualiza dispositivo: troca contrato_id por usuario_id
ALTER TABLE dispositivo
    ADD COLUMN usuario_id UUID,
    ADD CONSTRAINT fk_dispositivo_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario(id);
CREATE INDEX idx_dispositivo_usuario_id ON dispositivo(usuario_id);
ALTER TABLE dispositivo
DROP CONSTRAINT IF EXISTS fk_dispositivo_contrato,
    DROP COLUMN IF EXISTS contrato_id;

-- Atualiza produto:
         -- troca contrato_id por empresa_id
ALTER TABLE produto
    ADD COLUMN empresa_id UUID,
    ADD CONSTRAINT fk_produto_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);
CREATE INDEX idx_produto_empresa_id ON produto(empresa_id);
ALTER TABLE produto
DROP CONSTRAINT IF EXISTS fk_produto_contrato,
    DROP COLUMN IF EXISTS contrato_id;
         -- adiciona a coluna de usuario_ultima_alteracao
ALTER TABLE produto
    ADD COLUMN usuario_ultima_alteracao UUID,
    ADD CONSTRAINT fk_produto_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario(id);


-- Atualiza sync_log: troca contrato_id por empresa_id
ALTER TABLE sync_log
    ADD COLUMN empresa_id UUID,
    ADD CONSTRAINT fk_sync_log_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);
CREATE INDEX idx_sync_log_empresa_id ON sync_log(empresa_id);
ALTER TABLE sync_log
DROP CONSTRAINT IF EXISTS fk_sync_log_contrato,
    DROP COLUMN IF EXISTS contrato_id;

-- Remove tabela cliente
DROP TABLE IF EXISTS cliente CASCADE;