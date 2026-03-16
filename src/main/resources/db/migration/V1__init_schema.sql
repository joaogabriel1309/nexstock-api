CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE cliente (
                         id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         nome        VARCHAR(200) NOT NULL,
                         email       VARCHAR(255) NOT NULL UNIQUE,
                         documento   VARCHAR(20)  NULL,        -- CNPJ ou CPF
                         telefone    VARCHAR(20)  NULL,
                         criado_em   TIMESTAMP    NOT NULL DEFAULT now(),
                         ativo       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE plano (
                       id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                       nome             VARCHAR(100)  NOT NULL,
                       descricao        TEXT          NULL,
                       preco            NUMERIC(10,2) NOT NULL,
                       duracao_dias     INTEGER       NOT NULL,
                       max_dispositivos INTEGER       NOT NULL DEFAULT 1,
                       ativo            BOOLEAN       NOT NULL DEFAULT TRUE,
                       criado_em        TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE contrato (
                          id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                          cliente_id  UUID        NOT NULL REFERENCES cliente(id) ON DELETE RESTRICT,
                          plano_id    UUID        NOT NULL REFERENCES plano(id)   ON DELETE RESTRICT,
                          data_inicio DATE        NOT NULL,
                          data_fim    DATE        NOT NULL,
                          status      VARCHAR(15) NOT NULL DEFAULT 'ATIVO'
                              CHECK (status IN ('ATIVO','EXPIRADO','CANCELADO','SUSPENSO')),
                          renovado_de UUID        NULL REFERENCES contrato(id),
                          criado_em   TIMESTAMP   NOT NULL DEFAULT now(),

                          CONSTRAINT chk_datas CHECK (data_fim > data_inicio)
);

CREATE INDEX idx_contrato_cliente_id ON contrato(cliente_id);
CREATE INDEX idx_contrato_status     ON contrato(status);
CREATE INDEX idx_contrato_data_fim   ON contrato(data_fim);

CREATE TABLE dispositivo (
                             id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                             contrato_id UUID         NOT NULL REFERENCES contrato(id) ON DELETE RESTRICT,
                             nome        VARCHAR(150) NOT NULL,
                             sistema     VARCHAR(100) NOT NULL,
                             ultimo_sync TIMESTAMP    NULL
);

CREATE INDEX idx_dispositivo_contrato_id ON dispositivo(contrato_id);

CREATE TABLE produto (
                         id                           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                         contrato_id                  UUID           NOT NULL REFERENCES contrato(id) ON DELETE RESTRICT,
                         nome                         VARCHAR(255)   NOT NULL,
                         codigo_barras                VARCHAR(100)   NULL,
                         estoque                      NUMERIC(15, 4) NOT NULL DEFAULT 0,
                         atualizado_em                TIMESTAMP      NOT NULL,
                         versao                       BIGINT         NOT NULL DEFAULT 1,
                         dispositivo_ultima_alteracao UUID           NULL
                                 REFERENCES dispositivo(id) ON DELETE SET NULL,
                         deletado                     BOOLEAN        NOT NULL DEFAULT FALSE,

                         CONSTRAINT uq_produto_codbarra_contrato UNIQUE (contrato_id, codigo_barras)
);

CREATE INDEX idx_produto_contrato_id    ON produto(contrato_id);
CREATE INDEX idx_produto_atualizado_em  ON produto(contrato_id, atualizado_em);
CREATE INDEX idx_produto_deletado       ON produto(deletado);

CREATE TABLE movimentacao_estoque (
                                      id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                      contrato_id UUID           NOT NULL REFERENCES contrato(id) ON DELETE RESTRICT,
                                      produto_id  UUID           NOT NULL REFERENCES produto(id)  ON DELETE RESTRICT,
                                      tipo        VARCHAR(10)    NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA')),
                                      quantidade  NUMERIC(15, 4) NOT NULL CHECK (quantidade > 0),
                                      criado_em   TIMESTAMP      NOT NULL,
                                      dispositivo_id UUID        NULL REFERENCES dispositivo(id) ON DELETE SET NULL
);

CREATE INDEX idx_mov_contrato_id    ON movimentacao_estoque(contrato_id);
CREATE INDEX idx_mov_produto_id     ON movimentacao_estoque(produto_id);
CREATE INDEX idx_mov_criado_em      ON movimentacao_estoque(criado_em);
CREATE INDEX idx_mov_dispositivo_id ON movimentacao_estoque(dispositivo_id);

CREATE TABLE sync_log (
                          id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                          contrato_id         UUID        NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
                          dispositivo_id      UUID        NOT NULL REFERENCES dispositivo(id) ON DELETE CASCADE,
                          data_sync           TIMESTAMP   NOT NULL,
                          registros_enviados  INTEGER     NOT NULL DEFAULT 0,
                          registros_recebidos INTEGER     NOT NULL DEFAULT 0,
                          status              VARCHAR(10) NOT NULL CHECK (status IN ('SUCESSO','ERRO')),
                          mensagem_erro       TEXT        NULL
);

CREATE INDEX idx_sync_log_contrato_id   ON sync_log(contrato_id);
CREATE INDEX idx_sync_log_dispositivo_id ON sync_log(dispositivo_id);
CREATE INDEX idx_sync_log_data_sync     ON sync_log(data_sync);
