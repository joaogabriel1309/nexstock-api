ALTER TABLE produto
    ADD COLUMN imagem_url TEXT,
    ADD COLUMN imagem_key VARCHAR(500);

CREATE INDEX idx_produto_imagem_key
    ON produto (imagem_key)
    WHERE imagem_key IS NOT NULL;
