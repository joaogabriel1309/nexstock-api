DROP TABLE IF EXISTS sync_log CASCADE;
DROP TABLE IF EXISTS movimentacao_estoque CASCADE;
DROP TABLE IF EXISTS produto CASCADE;
DROP TABLE IF EXISTS dispositivo CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS empresa CASCADE;
DROP TABLE IF EXISTS contrato CASCADE;
DROP TABLE IF EXISTS plano CASCADE;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE plano (
                       id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                       nome             VARCHAR(100)  NOT NULL,
                       descricao        TEXT          NULL,
                       preco            NUMERIC(10,2) NOT NULL,
                       duracao_dias     INTEGER       NOT NULL,
                       max_dispositivos INTEGER       NOT NULL DEFAULT 1,
                       ativo            BOOLEAN       NOT NULL DEFAULT TRUE,
                       criado_em        TIMESTAMP     NOT NULL DEFAULT now(),
                       atualizado_em    TIMESTAMP     NOT NULL DEFAULT now(),
                       deletado_em      TIMESTAMP     NULL
);

CREATE TABLE contrato (
                          id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                          plano_id      UUID        NOT NULL REFERENCES plano(id),
                          data_inicio   DATE        NOT NULL,
                          data_fim      DATE        NOT NULL,
                          status        VARCHAR(15) NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','EXPIRADO','CANCELADO','SUSPENSO')),
                          renovado_de   UUID        NULL,
                          criado_em     TIMESTAMP   NOT NULL DEFAULT now(),
                          atualizado_em TIMESTAMP NOT NULL DEFAULT now(),
                          deletado_em   TIMESTAMP NULL,
                          CONSTRAINT chk_datas    CHECK (data_fim > data_inicio)
);

CREATE TABLE empresa (
                         id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         contrato_id   UUID         NOT NULL REFERENCES contrato(id),
                         nome          VARCHAR(200) NOT NULL,
                         razao_social  VARCHAR(200) NOT NULL,
                         cpf_cnpj      VARCHAR(18)  NOT NULL UNIQUE,
                         email         VARCHAR(255),
                         telefone      VARCHAR(20),
                         ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
                         criado_em     TIMESTAMP    NOT NULL DEFAULT now(),
                         atualizado_em TIMESTAMP   NOT NULL DEFAULT now(),
                         deletado_em   TIMESTAMP   NULL
);

CREATE TABLE usuario (
                         id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         empresa_id    UUID         NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
                         nome          VARCHAR(150) NOT NULL,
                         email         VARCHAR(255) NOT NULL UNIQUE,
                         senha         VARCHAR(255) NOT NULL,
                         role          VARCHAR(20)  NOT NULL DEFAULT 'OPERADOR' CHECK (role IN ('ADMIN','OPERADOR')),
                         ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
                         criado_em     TIMESTAMP    NOT NULL DEFAULT now(),
                         atualizado_em TIMESTAMP   NOT NULL DEFAULT now(),
                         deletado_em   TIMESTAMP   NULL
);

CREATE TABLE dispositivo (
                             id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                             empresa_id    UUID         NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
                             usuario_id    UUID         REFERENCES usuario(id) ON DELETE SET NULL,
                             nome          VARCHAR(150) NOT NULL,
                             sistema       VARCHAR(100) NOT NULL,
                             ultimo_sync   TIMESTAMP    NULL,
                             criado_em     TIMESTAMP    NOT NULL DEFAULT now(),
                             atualizado_em TIMESTAMP   NOT NULL DEFAULT now(),
                             deletado_em   TIMESTAMP   NULL
);

CREATE TABLE produto (
                         id                           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                         empresa_id                   UUID           NOT NULL REFERENCES empresa(id),
                         contrato_id                  UUID           NULL REFERENCES contrato(id), -- Compatibilidade JPA
                         nome                         VARCHAR(255)   NOT NULL,
                         codigo_barras                VARCHAR(100)   NULL,
                         estoque                      NUMERIC(15, 4) NOT NULL DEFAULT 0,
                         versao                       BIGINT         NOT NULL DEFAULT 1,
                         usuario_ultima_alteracao     UUID           REFERENCES usuario(id) ON DELETE SET NULL,
                         dispositivo_ultima_alteracao UUID           REFERENCES dispositivo(id) ON DELETE SET NULL,
                         criado_em                    TIMESTAMP      NOT NULL DEFAULT now(),
                         atualizado_em                TIMESTAMP      NOT NULL DEFAULT now(),
                         deletado_em                  TIMESTAMP      NULL,
                         CONSTRAINT uq_produto_codbarra_empresa UNIQUE (empresa_id, codigo_barras)
);

CREATE TABLE movimentacao_estoque (
                                      id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                      empresa_id     UUID           NOT NULL REFERENCES empresa(id),
                                      produto_id     UUID           NOT NULL REFERENCES produto(id),
                                      tipo           VARCHAR(10)    NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA')),
                                      quantidade     NUMERIC(15, 4) NOT NULL CHECK (quantidade > 0),
                                      dispositivo_id UUID           REFERENCES dispositivo(id) ON DELETE SET NULL,
                                      criado_em      TIMESTAMP      NOT NULL DEFAULT now(),
                                      atualizado_em  TIMESTAMP      NOT NULL DEFAULT now(),
                                      deletado_em    TIMESTAMP      NULL
);

CREATE TABLE sync_log (
                          id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                          empresa_id          UUID        NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
                          dispositivo_id      UUID        NOT NULL REFERENCES dispositivo(id) ON DELETE CASCADE,
                          data_sync           TIMESTAMP   NOT NULL DEFAULT now(),
                          registros_enviados  INTEGER     NOT NULL DEFAULT 0,
                          registros_recebidos INTEGER     NOT NULL DEFAULT 0,
                          status              VARCHAR(10) NOT NULL CHECK (status IN ('SUCESSO','ERRO')),
                          mensagem_erro       TEXT        NULL
);

CREATE INDEX idx_produto_atualizado    ON produto(empresa_id, atualizado_em);
CREATE INDEX idx_produto_deletado      ON produto(deletado_em) WHERE deletado_em IS NOT NULL;
CREATE INDEX idx_usuario_email         ON usuario(email);
CREATE INDEX idx_empresa_cpf_cnpj      ON empresa(cpf_cnpj);

INSERT INTO plano (nome, preco, duracao_dias, max_dispositivos) VALUES
                                                                    ('Básico', 29.90, 30, 2),
                                                                    ('Profissional', 59.90, 30, 10),
                                                                    ('Enterprise', 159.90, 30, 50);