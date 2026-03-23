-- Planos fixos do sistema
INSERT INTO plano (id, nome, preco, duracao_dias, max_dispositivos, ativo)
VALUES
    (gen_random_uuid(), 'Básico',       29.90,  30,  2,  true),
    (gen_random_uuid(), 'Profissional', 59.90,  30,  10, true),
    (gen_random_uuid(), 'Enterprise',   159.90, 30,  50, true);

-- Tabela empresa
CREATE TABLE empresa (
                         id           UUID         NOT NULL DEFAULT gen_random_uuid(),
                         nome         VARCHAR(200) NOT NULL,
                         razao_social VARCHAR(200) NOT NULL,
                         cpf_cnpj     VARCHAR(18)  NOT NULL UNIQUE,
                         email        VARCHAR(255),
                         telefone     VARCHAR(20),
                         ativo        BOOLEAN      NOT NULL DEFAULT TRUE,
                         criado_em    TIMESTAMP    NOT NULL DEFAULT NOW(),

                         CONSTRAINT pk_empresa PRIMARY KEY (id)
);

CREATE INDEX idx_empresa_nome     ON empresa(nome);
CREATE INDEX idx_empresa_cpf_cnpj ON empresa(cpf_cnpj);