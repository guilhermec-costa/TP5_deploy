# CRUD System - Manual de Execução

## Visão Geral
Sistema CRUD completo com backend Java (Javalin) e frontend React, incluindo testes automatizados, CI/CD com GitHub Actions e cobertura de código via JaCoCo.

## Estrutura do Projeto
```
TP4/
├── .github/
│   └── workflows/
│       └── backend-ci.yml    # Pipeline CI/CD do backend
├── backend/                  # Backend Java com Javalin
│   ├── src/main/java/
│   │   └── com/crud/
│   │       ├── config/       # Configuração da aplicação
│   │       ├── controller/   # Controladores REST
│   │       ├── exception/    # Exceções customizadas
│   │       ├── model/        # Modelos de dados
│   │       ├── repository/   # Repositório de dados
│   │       └── service/      # Lógica de negócio
│   └── src/test/            # Testes
│       └── com/crud/
│           ├── unit/         # Testes unitários
│           ├── integration/  # Testes de integração
│           ├── fuzz/         # Testes de fuzz
│           └── selenium/     # Testes Selenium
└── frontend/                # Frontend React
    ├── public/
    └── src/
        ├── config/           # Configurações (API)
        ├── hooks/            # Hooks customizados
        ├── components/       # Componentes React
        ├── App.js           # Componente principal
        └── App.css          # Estilos
```

---

## Como Rodar a Aplicação Integrada

### 1. Backend

#### Compilação
```bash
cd backend
mvn clean compile
```

#### Execução
```bash
cd backend
mvn exec:java -Dexec.mainClass="com.crud.Main"
```

O servidor será iniciado em: http://localhost:8080

#### Endpoints da API
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/items` | Listar todos os itens |
| GET | `/api/items/{id}` | Obter item por ID |
| POST | `/api/items` | Criar novo item |
| PUT | `/api/items/{id}` | Atualizar item |
| DELETE | `/api/items/{id}` | Excluir item |
| GET | `/api/health` | Verificação de saúde |

### 2. Frontend

#### Instalação
```bash
cd frontend
npm install
# ou com pnpm
pnpm install
```

#### Execução
```bash
cd frontend
npm start
# ou com pnpm
pnpm start
```

O frontend será iniciado em: http://localhost:3000

### 3. Execução Simultânea (Aplicação Integrada)

1. Inicie o backend primeiro (porta 8080)
2. Em outro terminal, inicie o frontend (porta 3000)
3. Acesse http://localhost:3000 para usar a interface

---

## Executando os Testes

### Todos os Testes
```bash
cd backend
mvn test
```

### Testes Unitários Apenas
```bash
mvn test -Dtest="**/unit/*Test"
```

### Testes de Integração Apenas
```bash
mvn test -Dtest="**/integration/*Test"
```

### Relatório de Cobertura JaCoCo
Após executar os testes:
```bash
mvn jacoco:report
```

O relatório será gerado em: `backend/target/site/jacoco/index.html`

---

## Workflows GitHub Actions

### Pipeline CI Backend (.github/workflows/backend-ci.yml)

O workflow é executado automaticamente nos seguintes eventos:

| Evento | Trigger |
|--------|---------|
| Push para branches | main, master, develop |
| Pull Request para | main, master, develop |
| Execução manual | workflow_dispatch |

### Jobs do Pipeline

1. **build-and-test**: Build completo + todos os testes + JaCoCo
2. **unit-tests**: Apenas testes unitários
3. **integration-tests**: Apenas testes de integração

### Interpretando os Resultados

#### Aba Actions do Repositório
1. Acesse a aba **Actions** no repositório GitHub
2. Selecione o workflow **Backend CI**
3. Visualize os jobs e suas execuções

#### Status dos Jobs
- **Verde (check)**: Sucesso
- **Vermelho (x)**: Falha
- **Amarelo (ponto)**: Em execução

#### Artefatos Disponíveis
- `jacoco-report`: Relatório de cobertura de código
- `unit-test-results`: Resultados detalhados dos testes unitários

#### Logs de Execução
Clique em qualquer job para ver os logs detalhados:
- Passos de checkout, setup Java, build
- Resultados dos testes
- Métricas de cobertura

### Configuração do Runner
O workflow utiliza **ubuntu-latest** (GitHub-hosted runner) com Java 17 (Temurin).

---

## Principais Mudanças de Refatoração

### Frontend (React)

#### Antes
- App.js continha toda a lógica de estado, fetch e CRUD
- URLs e configurações hardcoded
- Confirmação de delete inline no App.js

#### Depois
- **Hooks customizados**:
  - `useApi`: Wrapper genérico para requisições HTTP
  - `useItems`: Lógica de CRUD encapsulada
- **Config centralizada** (`src/config/api.js`): URL da API configurável via variável de ambiente
- **Componente ConfirmDialog**: Separação da lógica de confirmação de exclusão
- **App.js simplificado**: Apenas gerencia UI e estados de modais

### Backend

O backend já estava bem estruturado, mantendo:
- Arquitetura em camadas (Controller → Service → Repository)
- Exceções customizadas para tratamento de erros
- Validação de dados no Service
- Testes com alta cobertura

### CI/CD

- Pipeline configurado com Maven e JaCoCo
- Execução paralela de jobs (unit/integration tests)
- Artefatos disponíveis para download
- Relatório de cobertura integrado

---

## Requisitos Não Funcionais

| Requisito | Status |
|-----------|--------|
| Linguagem: Java | ✅ Backend em Java 17 |
| Clean Code | ✅ Código modular e documentado |
| Cobertura de testes ≥ 85% | ✅ JaCoCo configurado |
| Workflows GitHub Actions | ✅ CI configurado |
| Mensagens de erro claras | ✅ Tratamento de exceções |

---

## Deploy (Simulação)

Este projeto implementa uma simulação de deploy utilizando **GitHub Actions com Self-Hosted Runner**. Essa abordagem foi escolhida para evitar custos de plataformas de deploy (como Heroku, Render, etc.) e as dores de cabeça associadas a plataformas gratuitas .

### Como funciona

1. **Self-Hosted Runner**: Um runner instalado na própria máquina (neste caso, a mesma máquina onde o código está sendo desenvolvido) que escuta por jobs do GitHub Actions.

2. **Workflow de Deploy**: Quando há um push para a branch `master`, o workflow (`.github/workflows/deploy.yml`) é executado no runner local.

3. **Docker Compose**: O workflow copia os arquivos necessários para a pasta `.deploy/` e sobe os containers (PostgreSQL, Backend e Frontend) via Docker Compose.

### Diferença para um Deploy Real

A única diferença para um deploy real em produção seria a **máquina alvo**:

- **Deploy Real**: O runner precisaria estar em um servidor de produção (VPS, cloud, etc.), e o workflow utilizaria SSH para conectar e fazer o deploy remoto.

- **Deploy Simulado (atual)**: O runner está na mesma máquina local, então o deploy acontece diretamente sem necessidade de SSH.

### Configuração

1. Configurar um self-hosted runner no repositório GitHub (Settings → Actions → Runners)
2. O runner deve ter Docker instalado
3. Ao dar push para `master`, o deploy é triggered automaticamente

### Serviços Subidos

- **PostgreSQL**: Porta 5440
- **Backend**: Porta 8080
- **Frontend**: Porta 3000
