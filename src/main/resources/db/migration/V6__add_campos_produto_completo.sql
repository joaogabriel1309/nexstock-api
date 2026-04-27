ALTER TABLE produto
    ADD COLUMN sku VARCHAR(100),
    ADD COLUMN descricao TEXT,
    ADD COLUMN unidade_medida VARCHAR(20),
    ADD COLUMN preco_custo NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN preco_venda NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN preco_venda_atacado NUMERIC(15,4),
    ADD COLUMN estoque_minimo NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN estoque_maximo NUMERIC(15,4),
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN permite_venda_sem_estoque BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE produto
SET sku = COALESCE(NULLIF(TRIM(codigo_barras), ''), id::text),
    unidade_medida = 'UN'
WHERE sku IS NULL
   OR unidade_medida IS NULL;

ALTER TABLE produto
    ALTER COLUMN sku SET NOT NULL,
    ALTER COLUMN unidade_medida SET NOT NULL;

ALTER TABLE produto
    ADD CONSTRAINT chk_produto_preco_custo
        CHECK (preco_custo >= 0),
    ADD CONSTRAINT chk_produto_preco_venda
        CHECK (preco_venda >= 0),
    ADD CONSTRAINT chk_produto_preco_venda_atacado
        CHECK (preco_venda_atacado IS NULL OR preco_venda_atacado >= 0),
    ADD CONSTRAINT chk_produto_estoque_minimo
        CHECK (estoque_minimo >= 0),
    ADD CONSTRAINT chk_produto_estoque_maximo
        CHECK (estoque_maximo IS NULL OR estoque_maximo >= estoque_minimo);

CREATE UNIQUE INDEX uq_produto_sku_empresa
    ON produto (empresa_id, sku);

CREATE INDEX idx_produto_sku
    ON produto (empresa_id, sku);
