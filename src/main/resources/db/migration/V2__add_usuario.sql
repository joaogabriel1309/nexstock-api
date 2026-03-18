CREATE TABLE usuario (
                         id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         contrato_id UUID         NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
                         nome        VARCHAR(150) NOT NULL,
                         email       VARCHAR(255) NOT NULL,
                         senha       VARCHAR(255) NOT NULL,   -- bcrypt hash
                         role        VARCHAR(20)  NOT NULL DEFAULT 'OPERADOR'
                             CHECK (role IN ('ADMIN','OPERADOR')),
                         ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
                         criado_em   TIMESTAMP    NOT NULL DEFAULT now(),

                         CONSTRAINT uq_usuario_email_contrato UNIQUE (contrato_id, email)
);

CREATE INDEX idx_usuario_contrato_id ON usuario(contrato_id);
CREATE INDEX idx_usuario_email       ON usuario(email);
