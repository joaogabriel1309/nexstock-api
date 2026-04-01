<div align="center">

# 📦 NexStock API

### Sistema de Gestão de Estoque Inteligente

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)](https://jwt.io/)
[![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow?style=for-the-badge)](https://github.com/joaogabriel1309/nexstock-api)

</div>

---

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Configuração](#-configuração)
- [Documentação da API](#-documentação-da-api)
  - [Autenticação](#autenticação)
  - [Setup](#setup)
  - [Empresas](#empresas)
  - [Usuários](#usuários)
  - [Planos](#planos)
  - [Contratos](#contratos)
  - [Dispositivos](#dispositivos)
  - [Produtos](#produtos)
  - [Sincronização](#sincronização)
- [Padrão de Respostas](#-padrão-de-respostas)
- [Estrutura de Pastas](#-estrutura-de-pastas)
- [Boas Práticas](#-boas-práticas)
- [Versionamento](#-versionamento)
- [Autor](#-autor)

---

## 🎯 Sobre o Projeto

**NexStock API** é uma solução robusta e escalável para gestão de estoque empresarial, desenvolvida com **Spring Boot** e seguindo os princípios de **Clean Architecture** e **RESTful**. 

A API oferece funcionalidades completas de gerenciamento de produtos, controle de movimentações, sincronização entre dispositivos e autenticação segura via JWT.

### Principais Funcionalidades

✅ Autenticação e autorização com JWT  
✅ Gerenciamento multi-empresa  
✅ Controle de usuários e permissões  
✅ Sistema de planos e contratos  
✅ Gestão completa de produtos  
✅ Sincronização de dados entre dispositivos  
✅ Rastreamento de movimentações de estoque  
✅ Setup inicial automatizado  

---

## 🚀 Tecnologias

### Core

- **Java 21** - Linguagem de programação
- **Spring Boot 3.5** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Segurança e autenticação
- **Spring Validation** - Validação de dados

### Banco de Dados

- **PostgreSQL 17** - Banco de dados relacional

### Ferramentas e Bibliotecas

- **Lombok** - Redução de boilerplate
- **JWT (JSON Web Token)** - Autenticação stateless
- **Bean Validation** - Validação de entradas
- **SLF4J + Logback** - Sistema de logs

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas bem definida:

```
nexstock-api/
├── controller/     → Camada de apresentação (REST endpoints)
├── service/        → Lógica de negócio
├── repository/     → Acesso aos dados (JPA)
├── dto/
│   ├── request/    → Objetos de entrada
│   └── response/   → Objetos de saída
├── model/          → Entidades do banco de dados
├── config/         → Configurações da aplicação
├── security/       → Configuração JWT e autenticação
└── exception/      → Tratamento de exceções
```

### Princípios Adotados

- **Separation of Concerns** - Separação clara de responsabilidades
- **Dependency Injection** - Inversão de controle via Spring
- **Single Responsibility** - Cada classe com responsabilidade única
- **RESTful Best Practices** - Padrão REST bem definido

---

## ⚙️ Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java JDK 21** ou superior
- **Maven 3.8+**
- **PostgreSQL 21**
- **Docker 29+**
- **Git**
- IDE de sua preferência (IntelliJ IDEA, Eclipse, VS Code)

---

## 🔧 Instalação e Execução

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/nexstock-api.git
cd nexstock-api
```

### 2. Subir Infraestrutura (Docker)

A API utiliza Docker Compose para gerenciar o banco de dados e a interface de gerenciamento:

```docker
docker-compose up -d
```

### 3. Configuração de Variáveis de Ambiente

A API já está preparada para ler as configurações sensíveis através de um arquivo .env. Certifique-se de que as chaves coincidam com o seu application.yml.
Crie um arquivo .env na raiz do projeto:

```.env
  # Configurações do Banco de Dados (Docker)
  POSTGRES_DB=nexstockdb
  POSTGRES_USER=postgres
  POSTGRES_PASSWORD=postgres
  
  # Configurações da API
  PORT=8080
  JWT_SECRET=7f9a2c8b1d4e6f9a2c8b1d4e6f9a2c8b1d4e6f9a2c8b1d4e6f9a2c8b1d4e6f9a
  JWT_EXPIRACAO_MS=86400000
```

### 4. Execute a aplicação

**Com Maven:**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

**Com Gradle:**

```bash
./gradlew build
./gradlew bootRun
```

### 5. Acesse a API

A aplicação estará disponível em:

```
http://localhost:8080
```

---

## 📚 Configuração

### Variáveis de Ambiente

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `SPRING_DATASOURCE_URL` | URL do banco PostgreSQL | `jdbc:postgresql://localhost:5432/nexstock` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `nexstock_user` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `senha123` |
| `JWT_SECRET` | Chave secreta para JWT | `sua_chave_secreta` |
| `JWT_EXPIRATION` | Tempo de expiração do token (ms) | `86400000` |
| `SERVER_PORT` | Porta da aplicação | `8080` |

---

## 📖 Documentação da API

### Base URL

```
http://localhost:8080/api
```

Todos os endpoints estão sob o prefixo `/api` e seguem o padrão RESTful.

---

## 🔐 Autenticação

A API utiliza **JWT (JSON Web Token)** para autenticação. Após o login ou registro, você receberá um token que deve ser enviado no header de todas as requisições protegidas.

### Endpoints Públicos

<details>
<summary><b>POST</b> <code>/api/auth/login</code> - Autenticar usuário</summary>

#### Descrição
Realiza o login de um usuário existente e retorna um token JWT.

#### Request Body
```json
{
  "email": "usuario@example.com",
  "senha": "senha123"
}
```

#### Response (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "usuario": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "nome": "João Silva",
    "email": "usuario@example.com",
    "empresaId": "123e4567-e89b-12d3-a456-426614174001"
  }
}
```

#### Exemplo com cURL
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "senha": "senha123"
  }'
```

</details>

<details>
<summary><b>POST</b> <code>/api/auth/registrar</code> - Registrar novo usuário</summary>

#### Descrição
Registra um novo usuário no sistema e retorna um token JWT.

#### Request Body
```json
{
  "nome": "João Silva",
  "email": "usuario@example.com",
  "senha": "senha123",
  "empresaId": "123e4567-e89b-12d3-a456-426614174001"
}
```

#### Response (201 Created)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "usuario": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "nome": "João Silva",
    "email": "usuario@example.com",
    "empresaId": "123e4567-e89b-12d3-a456-426614174001"
  }
}
```

#### Exemplo com cURL
```bash
curl -X POST http://localhost:8080/api/auth/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "usuario@example.com",
    "senha": "senha123",
    "empresaId": "123e4567-e89b-12d3-a456-426614174001"
  }'
```

</details>

### Header de Autenticação

Para rotas protegidas, inclua o token no header:

```
Authorization: Bearer {seu_token_jwt}
```

---

## ⚡ Setup

<details>
<summary><b>POST</b> <code>/api/setup</code> - Realizar setup inicial do sistema</summary>

#### Descrição
Configura o sistema pela primeira vez, criando estruturas iniciais necessárias.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "nomeEmpresa": "Minha Empresa LTDA",
  "cnpj": "12.345.678/0001-90",
  "planoId": "123e4567-e89b-12d3-a456-426614174000",
  "adminEmail": "admin@empresa.com",
  "adminSenha": "senha123"
}
```

#### Response (201 Created)
```
No content
```

#### Exemplo com cURL
```bash
curl -X POST http://localhost:8080/api/setup \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nomeEmpresa": "Minha Empresa LTDA",
    "cnpj": "12.345.678/0001-90",
    "planoId": "123e4567-e89b-12d3-a456-426614174000",
    "adminEmail": "admin@empresa.com",
    "adminSenha": "senha123"
  }'
```

</details>

---

## 🏢 Empresas

<details>
<summary><b>POST</b> <code>/api/empresas</code> - Criar nova empresa</summary>

#### Descrição
Registra uma nova empresa no sistema.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "nome": "Empresa XYZ Ltda",
  "cnpj": "12.345.678/0001-90",
  "telefone": "(11) 98765-4321",
  "email": "contato@empresa.com",
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "123",
    "complemento": "Sala 45",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "estado": "SP",
    "cep": "01234-567"
  }
}
```

#### Response (201 Created)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Empresa XYZ Ltda",
  "cnpj": "12.345.678/0001-90",
  "telefone": "(11) 98765-4321",
  "email": "contato@empresa.com",
  "ativa": true,
  "dataCriacao": "2024-01-15T10:30:00"
}
```

#### Headers de Resposta
```
Location: http://localhost:8080/api/empresas/123e4567-e89b-12d3-a456-426614174000
```

</details>

<details>
<summary><b>GET</b> <code>/api/empresas</code> - Listar todas as empresas</summary>

#### Descrição
Retorna a lista de todas as empresas cadastradas.

#### Headers
```
Authorization: Bearer {token}
```

#### Response (200 OK)
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "nome": "Empresa XYZ Ltda",
    "cnpj": "12.345.678/0001-90",
    "ativa": true,
    "dataCriacao": "2024-01-15T10:30:00"
  },
  {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "nome": "Empresa ABC S.A.",
    "cnpj": "98.765.432/0001-10",
    "ativa": true,
    "dataCriacao": "2024-02-20T14:45:00"
  }
]
```

</details>

<details>
<summary><b>GET</b> <code>/api/empresas/{id}</code> - Buscar empresa por ID</summary>

#### Descrição
Retorna os detalhes de uma empresa específica.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID da empresa |

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Empresa XYZ Ltda",
  "cnpj": "12.345.678/0001-90",
  "telefone": "(11) 98765-4321",
  "email": "contato@empresa.com",
  "ativa": true,
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "123",
    "complemento": "Sala 45",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "estado": "SP",
    "cep": "01234-567"
  },
  "dataCriacao": "2024-01-15T10:30:00"
}
```

#### Exemplo com cURL
```bash
curl -X GET http://localhost:8080/api/empresas/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer {token}"
```

</details>

<details>
<summary><b>PUT</b> <code>/api/empresas/{id}</code> - Atualizar empresa</summary>

#### Descrição
Atualiza os dados de uma empresa existente.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID da empresa |

#### Request Body
```json
{
  "nome": "Empresa XYZ Ltda - Atualizada",
  "telefone": "(11) 91234-5678",
  "email": "novo@empresa.com"
}
```

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Empresa XYZ Ltda - Atualizada",
  "cnpj": "12.345.678/0001-90",
  "telefone": "(11) 91234-5678",
  "email": "novo@empresa.com",
  "ativa": true,
  "dataAtualizacao": "2024-03-10T16:20:00"
}
```

</details>

<details>
<summary><b>PATCH</b> <code>/api/empresas/{id}/desativar</code> - Desativar empresa</summary>

#### Descrição
Desativa uma empresa no sistema (soft delete).

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID da empresa |

#### Response (204 No Content)
```
No content
```

#### Exemplo com cURL
```bash
curl -X PATCH http://localhost:8080/api/empresas/123e4567-e89b-12d3-a456-426614174000/desativar \
  -H "Authorization: Bearer {token}"
```

</details>

---

## 👥 Usuários

<details>
<summary><b>GET</b> <code>/api/usuarios</code> - Listar usuários por empresa</summary>

#### Descrição
Retorna a lista de todos os usuários de uma empresa específica.

#### Headers
```
Authorization: Bearer {token}
```

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Response (200 OK)
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "nome": "João Silva",
    "email": "joao@empresa.com",
    "ativo": true,
    "perfil": "ADMIN",
    "dataCriacao": "2024-01-15T10:30:00"
  },
  {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "nome": "Maria Santos",
    "email": "maria@empresa.com",
    "ativo": true,
    "perfil": "USUARIO",
    "dataCriacao": "2024-02-01T14:20:00"
  }
]
```

#### Exemplo com cURL
```bash
curl -X GET "http://localhost:8080/api/usuarios?empresaId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer {token}"
```

</details>

<details>
<summary><b>GET</b> <code>/api/usuarios/{id}</code> - Buscar usuário por ID</summary>

#### Descrição
Retorna os detalhes de um usuário específico.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do usuário |

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "João Silva",
  "email": "joao@empresa.com",
  "telefone": "(11) 98765-4321",
  "ativo": true,
  "perfil": "ADMIN",
  "empresaId": "123e4567-e89b-12d3-a456-426614174001",
  "dataCriacao": "2024-01-15T10:30:00"
}
```

</details>

<details>
<summary><b>DELETE</b> <code>/api/usuarios/{id}</code> - Deletar usuário</summary>

#### Descrição
Remove um usuário do sistema.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do usuário |

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Response (204 No Content)
```
No content
```

#### Exemplo com cURL
```bash
curl -X DELETE "http://localhost:8080/api/usuarios/123e4567-e89b-12d3-a456-426614174000?empresaId=123e4567-e89b-12d3-a456-426614174001" \
  -H "Authorization: Bearer {token}"
```

</details>

---

## 💼 Planos

<details>
<summary><b>GET</b> <code>/api/planos</code> - Listar planos ativos</summary>

#### Descrição
Retorna todos os planos de assinatura disponíveis para contratação.

#### Headers
```
Authorization: Bearer {token}
```

#### Response (200 OK)
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "nome": "Plano Básico",
    "descricao": "Ideal para pequenas empresas",
    "valor": 99.90,
    "limiteUsuarios": 5,
    "limiteProdutos": 500,
    "limiteDispositivos": 2,
    "ativo": true,
    "recursos": [
      "Gestão de estoque",
      "Sincronização básica",
      "Suporte por email"
    ]
  },
  {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "nome": "Plano Profissional",
    "descricao": "Para empresas em crescimento",
    "valor": 199.90,
    "limiteUsuarios": 20,
    "limiteProdutos": 5000,
    "limiteDispositivos": 10,
    "ativo": true,
    "recursos": [
      "Gestão de estoque avançada",
      "Sincronização em tempo real",
      "Relatórios personalizados",
      "Suporte prioritário"
    ]
  }
]
```

#### Exemplo com cURL
```bash
curl -X GET http://localhost:8080/api/planos \
  -H "Authorization: Bearer {token}"
```

</details>

---

## 📄 Contratos

<details>
<summary><b>POST</b> <code>/api/contratos</code> - Criar novo contrato</summary>

#### Descrição
Cria um novo contrato de assinatura para uma empresa.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "planoId": "123e4567-e89b-12d3-a456-426614174001",
  "dataInicio": "2024-03-01",
  "duracao": 12
}
```

#### Response (201 Created)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174002",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "planoId": "123e4567-e89b-12d3-a456-426614174001",
  "dataInicio": "2024-03-01",
  "dataFim": "2025-03-01",
  "status": "ATIVO",
  "valor": 199.90,
  "dataCriacao": "2024-03-01T10:00:00"
}
```

#### Headers de Resposta
```
Location: http://localhost:8080/api/contratos/123e4567-e89b-12d3-a456-426614174002
```

</details>

<details>
<summary><b>GET</b> <code>/api/contratos/{id}</code> - Buscar contrato por ID</summary>

#### Descrição
Retorna os detalhes de um contrato específico.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do contrato |

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174002",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "planoId": "123e4567-e89b-12d3-a456-426614174001",
  "planoNome": "Plano Profissional",
  "dataInicio": "2024-03-01",
  "dataFim": "2025-03-01",
  "status": "ATIVO",
  "valor": 199.90,
  "dataCriacao": "2024-03-01T10:00:00"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/contratos/{id}/renovar</code> - Renovar contrato</summary>

#### Descrição
Renova um contrato existente por mais um período.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do contrato |

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174002",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "planoId": "123e4567-e89b-12d3-a456-426614174001",
  "dataInicio": "2025-03-01",
  "dataFim": "2026-03-01",
  "status": "ATIVO",
  "valor": 199.90,
  "dataRenovacao": "2025-03-01T10:00:00"
}
```

#### Exemplo com cURL
```bash
curl -X POST http://localhost:8080/api/contratos/123e4567-e89b-12d3-a456-426614174002/renovar \
  -H "Authorization: Bearer {token}"
```

</details>

<details>
<summary><b>PATCH</b> <code>/api/contratos/{id}/cancelar</code> - Cancelar contrato</summary>

#### Descrição
Cancela um contrato ativo.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do contrato |

#### Response (204 No Content)
```
No content
```

#### Exemplo com cURL
```bash
curl -X PATCH http://localhost:8080/api/contratos/123e4567-e89b-12d3-a456-426614174002/cancelar \
  -H "Authorization: Bearer {token}"
```

</details>

---

## 📱 Dispositivos

<details>
<summary><b>POST</b> <code>/api/dispositivos</code> - Registrar novo dispositivo</summary>

#### Descrição
Registra um novo dispositivo móvel/terminal para sincronização de dados.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Tablet Almoxarifado",
  "tipo": "TABLET",
  "modelo": "Samsung Galaxy Tab S8",
  "numeroSerie": "ABC123XYZ789",
  "sistemaOperacional": "Android 13"
}
```

#### Response (201 Created)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174003",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Tablet Almoxarifado",
  "tipo": "TABLET",
  "modelo": "Samsung Galaxy Tab S8",
  "numeroSerie": "ABC123XYZ789",
  "ativo": true,
  "ultimaSincronizacao": null,
  "dataCriacao": "2024-03-15T14:30:00"
}
```

#### Headers de Resposta
```
Location: http://localhost:8080/api/dispositivos/123e4567-e89b-12d3-a456-426614174003
```

</details>

<details>
<summary><b>GET</b> <code>/api/dispositivos/empresa/{empresaId}</code> - Listar dispositivos por empresa</summary>

#### Descrição
Retorna todos os dispositivos cadastrados de uma empresa.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `empresaId` | UUID | ID da empresa |

#### Response (200 OK)
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174003",
    "nome": "Tablet Almoxarifado",
    "tipo": "TABLET",
    "modelo": "Samsung Galaxy Tab S8",
    "ativo": true,
    "ultimaSincronizacao": "2024-03-20T10:15:30",
    "dataCriacao": "2024-03-15T14:30:00"
  },
  {
    "id": "123e4567-e89b-12d3-a456-426614174004",
    "nome": "Smartphone Vendas",
    "tipo": "SMARTPHONE",
    "modelo": "iPhone 14 Pro",
    "ativo": true,
    "ultimaSincronizacao": "2024-03-20T11:45:00",
    "dataCriacao": "2024-03-16T09:20:00"
  }
]
```

#### Exemplo com cURL
```bash
curl -X GET http://localhost:8080/api/dispositivos/empresa/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer {token}"
```

</details>

<details>
<summary><b>GET</b> <code>/api/dispositivos/{id}</code> - Buscar dispositivo por ID</summary>

#### Descrição
Retorna os detalhes de um dispositivo específico.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do dispositivo |

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174003",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Tablet Almoxarifado",
  "tipo": "TABLET",
  "modelo": "Samsung Galaxy Tab S8",
  "numeroSerie": "ABC123XYZ789",
  "sistemaOperacional": "Android 13",
  "ativo": true,
  "ultimaSincronizacao": "2024-03-20T10:15:30",
  "dataCriacao": "2024-03-15T14:30:00"
}
```

</details>

<details>
<summary><b>DELETE</b> <code>/api/dispositivos/{id}</code> - Remover dispositivo</summary>

#### Descrição
Remove um dispositivo do sistema.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do dispositivo |

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Response (204 No Content)
```
No content
```

#### Exemplo com cURL
```bash
curl -X DELETE "http://localhost:8080/api/dispositivos/123e4567-e89b-12d3-a456-426614174003?empresaId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer {token}"
```

</details>

---

## 📦 Produtos

<details>
<summary><b>POST</b> <code>/api/produtos</code> - Criar novo produto</summary>

#### Descrição
Cadastra um novo produto no estoque.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Notebook Dell Inspiron 15",
  "codigo": "NB-DELL-001",
  "codigoBarras": "7891234567890",
  "descricao": "Notebook Dell Inspiron 15, Intel i7, 16GB RAM, 512GB SSD",
  "categoria": "INFORMATICA",
  "unidadeMedida": "UNIDADE",
  "precoVenda": 3499.90,
  "precoCusto": 2800.00,
  "quantidadeMinima": 5,
  "quantidadeMaxima": 50,
  "ativo": true
}
```

#### Response (201 Created)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174005",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Notebook Dell Inspiron 15",
  "codigo": "NB-DELL-001",
  "codigoBarras": "7891234567890",
  "categoria": "INFORMATICA",
  "precoVenda": 3499.90,
  "quantidadeAtual": 0,
  "ativo": true,
  "dataCriacao": "2024-03-20T15:00:00"
}
```

#### Headers de Resposta
```
Location: http://localhost:8080/api/produtos/123e4567-e89b-12d3-a456-426614174005
```

</details>

<details>
<summary><b>GET</b> <code>/api/produtos</code> - Listar produtos ativos</summary>

#### Descrição
Retorna todos os produtos ativos de uma empresa.

#### Headers
```
Authorization: Bearer {token}
```

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Response (200 OK)
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174005",
    "nome": "Notebook Dell Inspiron 15",
    "codigo": "NB-DELL-001",
    "codigoBarras": "7891234567890",
    "categoria": "INFORMATICA",
    "precoVenda": 3499.90,
    "quantidadeAtual": 25,
    "ativo": true,
    "dataCriacao": "2024-03-20T15:00:00"
  },
  {
    "id": "123e4567-e89b-12d3-a456-426614174006",
    "nome": "Mouse Logitech MX Master 3",
    "codigo": "MS-LOG-001",
    "codigoBarras": "7891234567891",
    "categoria": "PERIFERICOS",
    "precoVenda": 549.90,
    "quantidadeAtual": 150,
    "ativo": true,
    "dataCriacao": "2024-03-21T09:30:00"
  }
]
```

#### Exemplo com cURL
```bash
curl -X GET "http://localhost:8080/api/produtos?empresaId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer {token}"
```

</details>

<details>
<summary><b>GET</b> <code>/api/produtos/{id}</code> - Buscar produto por ID</summary>

#### Descrição
Retorna os detalhes completos de um produto específico.

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do produto |

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174005",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Notebook Dell Inspiron 15",
  "codigo": "NB-DELL-001",
  "codigoBarras": "7891234567890",
  "descricao": "Notebook Dell Inspiron 15, Intel i7, 16GB RAM, 512GB SSD",
  "categoria": "INFORMATICA",
  "unidadeMedida": "UNIDADE",
  "precoVenda": 3499.90,
  "precoCusto": 2800.00,
  "quantidadeAtual": 25,
  "quantidadeMinima": 5,
  "quantidadeMaxima": 50,
  "ativo": true,
  "dataCriacao": "2024-03-20T15:00:00",
  "dataAtualizacao": "2024-03-25T11:20:00"
}
```

</details>

<details>
<summary><b>PUT</b> <code>/api/produtos/{id}</code> - Atualizar produto</summary>

#### Descrição
Atualiza os dados de um produto existente.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do produto |

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |

#### Request Body
```json
{
  "nome": "Notebook Dell Inspiron 15 - Nova Geração",
  "precoVenda": 3799.90,
  "precoCusto": 3000.00,
  "quantidadeMinima": 10,
  "descricao": "Notebook Dell Inspiron 15, Intel i7 12ª Gen, 16GB RAM, 512GB SSD"
}
```

#### Response (200 OK)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174005",
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Notebook Dell Inspiron 15 - Nova Geração",
  "codigo": "NB-DELL-001",
  "precoVenda": 3799.90,
  "precoCusto": 3000.00,
  "quantidadeAtual": 25,
  "ativo": true,
  "dataAtualizacao": "2024-03-26T14:15:00"
}
```

</details>

<details>
<summary><b>DELETE</b> <code>/api/produtos/{id}</code> - Deletar produto</summary>

#### Descrição
Remove um produto do sistema (soft delete).

#### Headers
```
Authorization: Bearer {token}
```

#### Path Parameters
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID do produto |

#### Query Parameters
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `empresaId` | UUID | Sim | ID da empresa |
| `dispositivoId` | UUID | Sim | ID do dispositivo que está deletando |

#### Response (204 No Content)
```
No content
```

#### Exemplo com cURL
```bash
curl -X DELETE "http://localhost:8080/api/produtos/123e4567-e89b-12d3-a456-426614174005?empresaId=123e4567-e89b-12d3-a456-426614174000&dispositivoId=123e4567-e89b-12d3-a456-426614174003" \
  -H "Authorization: Bearer {token}"
```

</details>

---

## 🔄 Sincronização

<details>
<summary><b>POST</b> <code>/api/sync</code> - Sincronizar dados</summary>

#### Descrição
Sincroniza produtos e movimentações entre o servidor e os dispositivos móveis. Este é o endpoint principal para manter os dados consistentes entre múltiplos dispositivos.

#### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "empresaId": "123e4567-e89b-12d3-a456-426614174000",
  "dispositivoId": "123e4567-e89b-12d3-a456-426614174003",
  "ultimaSincronizacao": "2024-03-25T10:00:00",
  "produtos": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174005",
      "nome": "Notebook Dell Inspiron 15",
      "codigo": "NB-DELL-001",
      "quantidadeAtual": 25,
      "precoVenda": 3499.90,
      "dataAtualizacao": "2024-03-26T14:00:00"
    }
  ],
  "movimentacoes": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174007",
      "produtoId": "123e4567-e89b-12d3-a456-426614174005",
      "tipo": "ENTRADA",
      "quantidade": 10,
      "observacao": "Entrada de estoque - Nota Fiscal 12345",
      "dataMovimentacao": "2024-03-26T15:30:00"
    }
  ]
}
```

#### Response (200 OK)
```json
{
  "sucesso": true,
  "timestamp": "2024-03-26T16:00:00",
  "produtosAtualizados": 1,
  "movimentacoesProcessadas": 1,
  "conflitos": [],
  "produtosServidor": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174006",
      "nome": "Mouse Logitech MX Master 3",
      "codigo": "MS-LOG-001",
      "quantidadeAtual": 150,
      "dataAtualizacao": "2024-03-26T12:00:00"
    }
  ],
  "movimentacoesServidor": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174008",
      "produtoId": "123e4567-e89b-12d3-a456-426614174006",
      "tipo": "SAIDA",
      "quantidade": 5,
      "dataMovimentacao": "2024-03-26T13:45:00"
    }
  ]
}
```

#### Exemplo com cURL
```bash
curl -X POST http://localhost:8080/api/sync \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "empresaId": "123e4567-e89b-12d3-a456-426614174000",
    "dispositivoId": "123e4567-e89b-12d3-a456-426614174003",
    "ultimaSincronizacao": "2024-03-25T10:00:00",
    "produtos": [...],
    "movimentacoes": [...]
  }'
```

#### Comportamento da Sincronização

1. **Upload (Dispositivo → Servidor)**
   - Produtos criados/editados no dispositivo são enviados ao servidor
   - Movimentações registradas offline são processadas
   - Conflitos são detectados e resolvidos

2. **Download (Servidor → Dispositivo)**
   - Produtos atualizados por outros dispositivos são retornados
   - Novas movimentações são enviadas
   - Apenas alterações desde `ultimaSincronizacao` são retornadas

3. **Resolução de Conflitos**
   - Last-Write-Wins (última escrita vence)
   - Conflitos são registrados para análise
   - Integridade do estoque é mantida

</details>

---

## ✅ Padrão de Respostas

### Respostas de Sucesso

#### 200 OK
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Recurso",
  "status": "ATIVO"
}
```

#### 201 Created
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "Novo Recurso",
  "dataCriacao": "2024-03-26T10:00:00"
}
```
**Header:** `Location: /api/recurso/{id}`

#### 204 No Content
```
(corpo vazio)
```

### Respostas de Erro

#### 400 Bad Request
```json
{
  "timestamp": "2024-03-26T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "errors": [
    {
      "field": "nome",
      "message": "Nome não pode ser vazio"
    },
    {
      "field": "email",
      "message": "Email inválido"
    }
  ],
  "path": "/api/empresas"
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2024-03-26T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido ou expirado",
  "path": "/api/produtos"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2024-03-26T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Você não tem permissão para acessar este recurso",
  "path": "/api/empresas/123"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2024-03-26T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Produto não encontrado com id: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/produtos/123e4567-e89b-12d3-a456-426614174000"
}
```

#### 409 Conflict
```json
{
  "timestamp": "2024-03-26T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Já existe um produto com este código: NB-DELL-001",
  "path": "/api/produtos"
}
```

#### 500 Internal Server Error
```json
{
  "timestamp": "2024-03-26T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Erro interno no servidor. Por favor, tente novamente mais tarde.",
  "path": "/api/produtos"
}
```

---

## 📁 Estrutura de Pastas

```
nexstock-api/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/nexstock/nexstock_api/
│   │   │       ├── config/              # Configurações
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── JwtConfig.java
│   │   │       │   └── CorsConfig.java
│   │   │       │
│   │   │       ├── controller/          # Controllers REST
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── EmpresaController.java
│   │   │       │   ├── UsuarioController.java
│   │   │       │   ├── ProdutoController.java
│   │   │       │   ├── DispositivoController.java
│   │   │       │   ├── ContratoController.java
│   │   │       │   ├── PlanoController.java
│   │   │       │   ├── SyncController.java
│   │   │       │   └── SetupController.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── request/         # DTOs de entrada
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── RegistroUsuarioRequest.java
│   │   │       │   │   ├── EmpresaRequest.java
│   │   │       │   │   ├── ProdutoRequest.java
│   │   │       │   │   ├── SyncRequest.java
│   │   │       │   │   └── ...
│   │   │       │   │
│   │   │       │   └── response/        # DTOs de saída
│   │   │       │       ├── LoginResponse.java
│   │   │       │       ├── UsuarioResponse.java
│   │   │       │       ├── EmpresaResponse.java
│   │   │       │       ├── ProdutoResponse.java
│   │   │       │       ├── SyncResponse.java
│   │   │       │       └── ...
│   │   │       │
│   │   │       ├── model/               # Entidades JPA
│   │   │       │   ├── Empresa.java
│   │   │       │   ├── Usuario.java
│   │   │       │   ├── Produto.java
│   │   │       │   ├── Dispositivo.java
│   │   │       │   ├── Contrato.java
│   │   │       │   ├── Plano.java
│   │   │       │   ├── Movimentacao.java
│   │   │       │   └── ...
│   │   │       │
│   │   │       ├── repository/          # Repositories JPA
│   │   │       │   ├── EmpresaRepository.java
│   │   │       │   ├── UsuarioRepository.java
│   │   │       │   ├── ProdutoRepository.java
│   │   │       │   ├── DispositivoRepository.java
│   │   │       │   └── ...
│   │   │       │
│   │   │       ├── service/             # Lógica de negócio
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── EmpresaService.java
│   │   │       │   ├── UsuarioService.java
│   │   │       │   ├── ProdutoService.java
│   │   │       │   ├── SyncService.java
│   │   │       │   └── ...
│   │   │       │
│   │   │       ├── security/            # Segurança JWT
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── UserDetailsServiceImpl.java
│   │   │       │
│   │   │       ├── exception/           # Exceções customizadas
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   ├── BusinessException.java
│   │   │       │   └── ...
│   │   │       │
│   │   │       └── NexstockApiApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml          # Configurações
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/           # Flyway migrations (opcional)
│   │
│   └── test/
│       └── java/
│           └── br/com/nexstock/nexstock_api/
│               ├── controller/
│               ├── service/
│               └── integration/
│
├── .gitignore
├── pom.xml                              # Maven dependencies
└── README.md
```

---

## 🎯 Boas Práticas

### Arquitetura

- ✅ **Clean Architecture** - Separação clara entre camadas
- ✅ **SOLID Principles** - Código limpo e manutenível
- ✅ **Dependency Injection** - Baixo acoplamento via Spring
- ✅ **DTOs** - Separação entre entidades e objetos de transferência

### Segurança

- ✅ **JWT Authentication** - Autenticação stateless
- ✅ **Password Encryption** - BCrypt para senhas
- ✅ **CORS Configuration** - Controle de origens permitidas
- ✅ **Input Validation** - Bean Validation em todos os endpoints

### Código

- ✅ **Lombok** - Redução de boilerplate code
- ✅ **Exception Handling** - Tratamento global de exceções
- ✅ **Logging** - SLF4J para rastreamento
- ✅ **RESTful Design** - Seguindo convenções HTTP

### Banco de Dados

- ✅ **JPA/Hibernate** - ORM para persistência
- ✅ **UUID** - IDs universalmente únicos
- ✅ **Soft Delete** - Desativação lógica de registros
- ✅ **Indexes** - Otimização de consultas

### API Design

- ✅ **Versioning** - URL versionada (/api)
- ✅ **HTTP Status Codes** - Uso correto de status
- ✅ **HATEOAS** - Links de recursos relacionados
- ✅ **Pagination** - Listagens paginadas (onde aplicável)

---

## 🔄 Versionamento

A API atualmente está na **versão 1.0** (implícita).

Todas as rotas estão sob o prefixo `/api`, preparadas para versionamento futuro:

```
/api/v1/...  (planejado)
/api/v2/...  (futuro)
```

### Changelog

#### v1.0.0 (atual)
- ✅ Sistema de autenticação JWT
- ✅ Gerenciamento de empresas
- ✅ Controle de usuários
- ✅ CRUD completo de produtos
- ✅ Sistema de planos e contratos
- ✅ Gestão de dispositivos
- ✅ Sincronização de dados
- ✅ Setup inicial automatizado

---

## 📊 Status do Projeto

```
🟢 Em Desenvolvimento Ativo
```

### Roadmap

## 🚀 Funcionalidades de Negócio

- [ ] Implementar paginação nos endpoints de listagem: Utilizar Pageable do Spring Data JPA para evitar sobrecarga de memória e melhorar a performance no carregamento de grandes volumes de produtos e empresas.
- [ ] Adicionar filtros avançados de busca: Implementar buscas dinâmicas utilizando Spring Data Specifications ou QueryDSL, permitindo filtrar produtos por nome, categoria, faixa de preço e status simultaneamente.
- [ ] Implementar relatórios de estoque: Geração de documentos em PDF ou CSV (utilizando JasperReports ou Apache POI) para fechamento de inventário e análise de movimentações mensais.
- [ ] Adicionar dashboard de métricas: Exposição de métricas de negócio via endpoints customizados para alimentar gráficos de vendas, produtos mais movimentados e saúde dos PDVs.
- [ ] Exportação de Dados (CSV/Excel): Facilitar a migração de dados e relatórios para os donos de empresas.

---

## 📦 Gestão de Produtos e Mídia

- [ ] Adicionar suporte a upload de imagens de produtos: Integração com serviços de Storage (como AWS S3, Google Cloud Storage ou local) para vincular fotos reais aos itens do catálogo via multipart/form-data.
- [ ] Soft Delete Global: Garantir que nada seja deletado fisicamente do banco sem rastro (usando filtros automáticos do Hibernate).

---

## 🔔 Comunicação e Notificações

- [ ] Implementar notificações push: Integração com Firebase Cloud Messaging (FCM) para alertar dispositivos mobile (Flutter) sobre estoque baixo, vencimento de contratos ou novos produtos sincronizados.
- [ ] Webhooks para Integrações: Notificar sistemas externos quando uma venda/sincronização for concluída.

---

## 🔐 Segurança e Autenticação

- [ ] Implementar Refresh Token: Evitar que o usuário precise logar todo dia, mantendo a segurança do JWT.
- [ ] Rate Limiting por API Key/Empresa: Implementar Bucket4j para evitar abusos nos endpoints de Sync e Login.
- [ ] CORS Policy Restrito: Configurar permissões apenas para os domínios oficiais do seu Angular e Flutter.

---

## ⚡ Performance e Escalabilidade

- [ ] Cache de Segundo Nível com Redis: Cachear planos, perfis de usuário e produtos frequentes para reduzir latência.
- [ ] Processamento Assíncrono com @Async: Mover tarefas pesadas (como envio de e-mails ou geração de logs complexos) para threads secundárias.
- [ ] Compressão GZIP/Brotli: Ativar compressão nas respostas JSON para economizar banda no mobile (Flutter).
- [ ] Database Connection Pooling: Otimizar o HikariCP para suportar picos de conexões simultâneas de múltiplos Dispositivos.

---

## 🛡️ Resiliência e Confiabilidade

- [ ] Circuit Breaker com Resilience4j: Proteger a API de falhas em cascata, especialmente em chamadas externas ou processos pesados de banco.
- [ ] Health Check Avançado: Verificar se o Redis e o Sistema de Arquivos (Imagens) estão operacionais, além do Postgres.

---

## 📊 Observabilidade e Monitoramento

- [ ] Centralização de Logs com ELK ou Loki: Parar de ler arquivos .log e usar uma interface de busca de erros.
- [ ] Distributed Tracing (Micrometer Tracing): Rastrear o caminho completo de uma requisição de Sync do início ao fim.
- [ ] Métricas Customizadas no Actuator: Expor quantos produtos estão sendo sincronizados por minuto para o Prometheus/Grafana.

---

## 🧪 Qualidade e Padronização

- [ ] Padronização RFC 9457 (Problem Details): Retornar erros em um formato JSON padrão de mercado (type, title, detail, instance).
- [ ] Documentação Interativa com Swagger/OpenAPI: Refinar as descrições dos modelos e exemplos de Request/Response no Swagger UI.
- [ ] Testes de Integração com Testcontainers: Garantir que o código funciona com um banco Postgres real durante o build.
- [ ] Checkstyle / SonarLint: Automatizar a verificação de padrões de código Java Sênior no projeto.

---

## 🌍 Internacionalização

- [ ] Suporte a múltiplos idiomas (i18n): Configurar MessageSource e LocaleResolver para internacionalizar mensagens de erro e respostas da API, preparando o sistema para o mercado global.

---

## 📜 Auditoria e Compliance

- [ ] Implementar auditoria de ações (Audit Log): Utilizar Hibernate Envers para registrar o histórico completo de alterações ("Quem alterou", "Quando" e "O que") em tabelas críticas como Produtos e Contratos.
- [ ] Auditoria de Entidades com Hibernate Envers: Manter histórico de "quem alterou o quê" em cada produto ou contrato.

---

## 🤖 Inteligência Artificial (IA)

- [ ] Implementar previsão inteligente de estoque: Utilizar análise de histórico de movimentações para prever demanda e evitar ruptura de estoque.
- [ ] Adicionar recomendação de compra inteligente: Sugerir automaticamente quais produtos comprar e em qual quantidade com base no histórico e previsões.
- [ ] Criar sistema de alertas inteligentes: Detectar padrões anormais como picos de venda, produtos parados ou comportamento inesperado.
- [ ] Implementar assistente virtual (AI Chat): Permitir consultas em linguagem natural como "quais produtos mais vendem?" ou "qual meu faturamento?".
- [ ] Gerar insights automáticos de negócio: Analisar dados e retornar recomendações estratégicas para o usuário.
- [ ] Implementar sugestão automática de cadastro de produtos: Preencher nome, categoria e descrição com base em entrada simples.
- [ ] Criar categorização automática de produtos: Classificar produtos automaticamente utilizando IA.
- [ ] Implementar detecção de anomalias: Identificar movimentações suspeitas ou inconsistentes.
- [ ] Criar score de saúde do estoque: Gerar uma pontuação baseada em giro, excesso e ruptura de produtos.
- [ ] Implementar personalização por empresa (multi-tenant AI): Adaptar recomendações com base no comportamento de cada empresa.
- [ ] Criar histórico de aprendizado da IA: Melhorar previsões com base em decisões passadas.
- [ ] Implementar API de IA desacoplada: Criar módulo ou microserviço separado para evolução independente da inteligência artificial.

---

<div align="center">

### ⭐ Se este projeto te ajudou, deixe uma estrela!

**NexStock API** - Gestão de Estoque Inteligente

</div>
