# RP: Remover Dispositivos da API e Simplificar Autenticacao

## Objetivo

Simplificar a API removendo o conceito de `Dispositivo` como entidade de negocio e de controle operacional, deixando o sistema baseado apenas em:

- `Usuario`
- `Senha`
- `JWT`

Ou seja: um usuario autenticado acessa a API diretamente, sem precisar registrar, validar ou rastrear um dispositivo.

## Motivacao

Hoje a validacao por dispositivo nao agrega valor suficiente para o fluxo atual e aumenta a complexidade da API:

- exige cadastro de dispositivo para operar certas partes do sistema
- adiciona validacoes e limites que nao ajudam o caso de uso principal
- complica o fluxo de sincronizacao
- espalha referencias de dispositivo por varias entidades e DTOs

O objetivo desta mudanca e deixar a autenticacao e as operacoes mais diretas:

- um usuario faz login
- recebe token
- opera a API

## Estado Atual

Hoje `Dispositivo` participa de:

- endpoint dedicado `/api/dispositivos`
- `DispositivoService`
- `DispositivoRepository`
- `DispositivoRequest` e `DispositivoResponse`
- `SetupService`, que cria dispositivo inicial
- `SyncService`, que exige `dispositivoId`
- `SyncLog`, que referencia dispositivo
- `Produto`, com `dispositivoUltimaAlteracao`
- `MovimentacaoEstoque`, com referencia a dispositivo
- limites do plano (`maxDispositivos`)
- documentacao da API no `README`

## Objetivo Funcional

Ao final da mudanca:

- nao deve existir endpoint de dispositivo
- nenhuma operacao da API deve exigir `dispositivoId`
- o login continua baseado apenas em email e senha
- alteracoes de produto e sincronizacao devem ser associadas somente ao usuario autenticado ou ao usuario informado no contexto da operacao
- o setup inicial nao deve mais criar dispositivo padrao

## Escopo da Mudanca

### 1. Remover API de dispositivos

Remover:

- `DispositivoController`
- `DispositivoService`
- `DispositivoRepository`
- `DispositivoRequest`
- `DispositivoResponse`
- excecao `LimiteDispositivosException`, se deixar de fazer sentido

Tambem remover documentacao e referencias a `/api/dispositivos`.

### 2. Simplificar autenticacao

Manter:

- login por email e senha
- emissao de JWT por usuario

Nao sera mais necessario:

- registrar dispositivo para uso da API
- consultar dispositivos por empresa ou usuario
- limitar a quantidade de dispositivos por plano

### 3. Remover `dispositivoId` dos contratos da API

Revisar DTOs que ainda recebem ou retornam `dispositivoId`, por exemplo:

- `ProdutoRequest`
- `SyncRequest`
- `ProdutoSyncRequest`
- `ProdutoResponse`
- `SyncResponse`

Objetivo:

- remover `dispositivoId` dos requests
- remover `dispositivoUltimaAlteracaoId` dos responses, se nao houver mais utilidade

### 4. Ajustar sincronizacao

Hoje a sincronizacao depende de `dispositivoId`.

Com a remocao de dispositivos, o sync deve ser refeito para operar somente com:

- `empresaId`
- usuario autenticado
- `ultimoSyncCliente`
- listas de produtos e movimentacoes

Decisao recomendada:

- associar `usuarioUltimaAlteracao` nas entidades
- remover dependencia de `dispositivoUltimaAlteracao`
- adaptar `SyncLog` para registrar o usuario em vez do dispositivo

## Proposta Tecnica

### Banco de dados

Criar migration nova, sem editar migrations antigas.

### Tabelas e colunas a revisar

#### Tabela `dispositivo`

Opcoes:

1. Remover a tabela e todas as FKs
2. Manter temporariamente, mas tornar obsoleta

Recomendacao:

- remover do modelo ativo agora
- se houver risco de migracao grande, fazer em duas etapas

#### Tabela `produto`

Remover:

- coluna `dispositivo_ultima_alteracao`

Manter:

- `usuario_ultima_alteracao`

#### Tabela `movimentacao_estoque`

Remover:

- coluna `dispositivo_id`

#### Tabela `sync_log`

Trocar:

- `dispositivo_id`

Por:

- `usuario_id`

Ou, se o sync deixar de ser relevante no modelo atual:

- simplificar o log para conter apenas `empresa_id`, `usuario_id`, timestamp e status

#### Tabela `plano`

Revisar se `max_dispositivos` ainda faz sentido.

Se nao fizer:

- remover da entidade
- remover dos DTOs
- remover da documentacao

## Regras de Negocio

### Autenticacao

- usuario ativo pode fazer login com email e senha
- empresa do usuario precisa continuar ativa
- nenhuma regra de validacao por dispositivo deve existir

### Produtos

- alteracoes devem ser atribuiveis ao usuario
- nao deve haver mais referencia a ultimo dispositivo que alterou

### Sincronizacao

- a sincronizacao deve funcionar sem identificador de dispositivo
- o controle de concorrencia continua por `atualizadoEm` e `versao`

## Impacto por Camada

### Controllers

Remover:

- `DispositivoController`

Atualizar:

- `SyncController`
- `ProdutoController`, se ainda houver restos de contrato antigo

### Services

Remover:

- `DispositivoService`

Atualizar:

- `SyncService`
- `SetupService`
- `ProdutoService`

### Entidades

Atualizar:

- `Produto`
- `MovimentacaoEstoque`
- `SyncLog`
- `Plano`

Remover:

- `Dispositivo`

### DTOs

Atualizar:

- `ProdutoRequest`
- `ProdutoResponse`
- `SyncRequest`
- `ProdutoSyncRequest`
- `MovimentacaoSyncRequest`, se necessario
- `SyncResponse`
- `PlanoResponse`

### Documentacao

Atualizar:

- `README.md`
- exemplos de payload
- secoes de autenticacao e sincronizacao

## Estrategia de Implementacao

### Fase 1

- remover uso de `dispositivoId` dos contratos
- adaptar services para operar sem dispositivo
- trocar rastreio por usuario

### Fase 2

- remover endpoints e classes de dispositivo
- remover limite de dispositivos do plano

### Fase 3

- criar migration para exclusao de FKs/colunas/tabela
- atualizar README
- ajustar testes

## Criticos Antes de Implementar

Algumas decisoes precisam ser tomadas antes do codigo:

1. O endpoint `/api/sync` continua existindo?
2. O `SyncLog` ainda sera mantido?
3. O plano ainda precisa ter algum limite operacional substituindo `maxDispositivos`?
4. O setup inicial deve criar apenas usuario/empresa/contrato, sem nenhum artefato extra?

## Recomendacao

Minha recomendacao para manter a mudanca limpa e sem meia-remocao e:

1. remover completamente a entidade `Dispositivo`
2. manter `Sync`, mas baseado em usuario
3. preservar `SyncLog`, trocando referencia de dispositivo por usuario
4. remover `maxDispositivos` do dominio e dos responses de plano

## Criterios de Aceite

- nao existe mais `/api/dispositivos`
- nao existe mais `dispositivoId` em requests obrigatorios
- login continua funcionando apenas com email e senha
- setup nao cria dispositivo inicial
- sync funciona sem dispositivo
- produto e movimentacao nao dependem de dispositivo
- testes automatizados cobrem o fluxo sem dispositivo
- README nao menciona mais o cadastro e uso de dispositivos

