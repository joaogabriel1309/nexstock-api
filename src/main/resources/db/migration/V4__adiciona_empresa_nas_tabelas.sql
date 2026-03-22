-- usuario
ALTER TABLE usuario
    ADD COLUMN empresa_id UUID,
    ADD CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);

CREATE INDEX idx_usuario_empresa_id ON usuario(empresa_id);

-- dispositivo
ALTER TABLE dispositivo
    ADD COLUMN empresa_id UUID,
    ADD CONSTRAINT fk_dispositivo_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);

CREATE INDEX idx_dispositivo_empresa_id ON dispositivo(empresa_id);

-- produto
ALTER TABLE produto
    ADD COLUMN empresa_id UUID,
    ADD CONSTRAINT fk_produto_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);

CREATE INDEX idx_produto_empresa_id ON produto(empresa_id);

-- sync_log
ALTER TABLE sync_log
    ADD COLUMN empresa_id UUID,
    ADD CONSTRAINT fk_sync_log_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id);

CREATE INDEX idx_sync_log_empresa_id ON sync_log(empresa_id);