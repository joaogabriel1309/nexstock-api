CREATE TABLE empresa (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    contrato_id  UUID         NOT NULL,
    nome         VARCHAR(200) NOT NULL,
    razao_social VARCHAR(200) NOT NULL,
    cpf_cnpj     VARCHAR(18)  NOT NULL,
    email        VARCHAR(255),
    telefone     VARCHAR(20),
    ativo        BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_empresa PRIMARY KEY (id),
    CONSTRAINT fk_empresa_contrato FOREIGN KEY (contrato_id)
        REFERENCES contrato(id)
);

CREATE INDEX idx_empresa_contrato_id ON empresa(contrato_id);
CREATE INDEX idx_empresa_nome        ON empresa(nome);
CREATE INDEX idx_empresa_cpf_cnpj    ON empresa(cpf_cnpj);