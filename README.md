# API de Reservas de Salas

API REST para cadastrar salas e usuários e reservar salas por período, sem conflito de horário.
Projeto de estudo construído com Spring Boot, JPA e PostgreSQL, com foco em regras de domínio, testes e execução em contêiner.

## Funcionalidades

- Cadastro e manutenção de **salas** (nome, capacidade, ativar/desativar).
- Cadastro e manutenção de **usuários** (e-mail único e normalizado).
- **Reservas** por sala e período, com checagem de conflito de horário.
- **Cancelamento** lógico: a reserva permanece no histórico com status `CANCELADA`.
- Consultas por período, paginadas e por sala.
- Documentação interativa com Swagger (springdoc).

## Stack

| Tecnologia | Uso |
|---|---|
| Java 25 + Spring Boot 4.1 | Aplicação e API REST |
| Spring Data JPA (Hibernate) | Persistência e consultas |
| PostgreSQL 17 | Banco de dados |
| Bean Validation | Validação de entrada |
| JUnit 5 + Mockito + AssertJ | Testes unitários |
| springdoc-openapi | Documentação Swagger |
| Docker e Docker Compose | Execução em contêiner |

## Regras de negócio

### Período da reserva: intervalo semiaberto `[início, fim)`

O início faz parte da reserva; o fim não. Por isso, uma reserva que termina às 11h libera o horário das 11h.

| Reserva existente | Nova reserva | Resultado |
|---|---|---|
| 10h–11h | 11h–12h | Permitida (fim igual ao início) |
| 10h–11h | 09h–10h | Permitida (fim igual ao início) |
| 10h–11h | 10h30–11h30 | **Conflito** (409) |
| 10h–12h | 10h–11h | **Conflito** (409) |
| 10h–11h | 10h–11h | **Conflito** (409) |

A regra está em `Reserva.periodosSeSobrepoem` e nas consultas do `ReservaRepository`, e é coberta por testes.

### Status da reserva

```
ATIVA ──cancelar──▶ CANCELADA
```

- Reservas `CANCELADA` **não entram** na checagem de conflito: o horário volta a ficar livre.
- Cancelar uma reserva já cancelada não gera erro (operação idempotente).
- Uma reserva cancelada **não pode mais ser alterada** (422).

### Outras regras

- Fim precisa ser depois do início. Fim igual ao início também é inválido.
- Capacidade precisa ser maior que zero.
- Nome de sala e de usuário não podem ser vazios.
- Sala **inativa** não recebe novas reservas (422). Reservas já existentes são mantidas.
- Sala ou usuário **com histórico de reservas não pode ser excluído** (409). Nesses casos, desative a sala.
- E-mail é normalizado (sem espaços, em minúsculas) e não pode se repetir (409).

## Endpoints

Base: `/api/v1`. Datas no formato ISO, por exemplo `2026-11-23T10:00:00`.

### Salas

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| GET | `/salas` | Lista as salas | 200 |
| GET | `/salas/{id}` | Busca uma sala | 200 |
| POST | `/salas` | Cria uma sala | 201 |
| PUT | `/salas/{id}` | Altera nome e capacidade | 200 |
| PATCH | `/salas/{id}/ativar` | Ativa a sala | 200 |
| PATCH | `/salas/{id}/desativar` | Desativa a sala | 200 |
| DELETE | `/salas/{id}` | Exclui (só sem reservas) | 204 |

### Usuários

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| GET | `/usuarios` | Lista os usuários | 200 |
| GET | `/usuarios/{id}` | Busca um usuário | 200 |
| POST | `/usuarios` | Cria um usuário | 201 |
| PUT | `/usuarios/{id}` | Altera nome e e-mail | 200 |
| DELETE | `/usuarios/{id}` | Exclui (só sem reservas) | 204 |

### Reservas

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| GET | `/reservas` | Lista todas as reservas | 200 |
| GET | `/reservas/{id}` | Busca uma reserva | 200 |
| POST | `/reservas` | Cria uma reserva | 201 |
| PUT | `/reservas/{id}` | Altera período, sala ou usuário | 200 |
| DELETE | `/reservas/{id}` | Cancela a reserva | 204 |
| GET | `/reservas/periodo?inicio=&fim=` | Reservas que se sobrepõem ao período | 200 |
| GET | `/reservas/paginadas?inicio=&fim=` | Igual ao anterior, paginado | 200 |
| GET | `/reservas/sala/{salaId}?inicio=&fim=` | Reservas de uma sala no período, paginado | 200 |

Exemplo de criação:

```http
POST /api/v1/reservas
Content-Type: application/json

{
  "usuarioId": 1,
  "salaId": 1,
  "inicio": "2026-12-01T10:00:00",
  "fim": "2026-12-01T11:00:00"
}
```

Listagens paginadas aceitam `page`, `size` e `sort`, por exemplo `?page=0&size=20&sort=inicio,asc`.

Swagger com todos os endpoints: `http://localhost:8080/swagger-ui.html`.

## Erros

Todos os erros têm o mesmo formato:

```json
{
  "status": 409,
  "mensagem": "O horário da reserva precisa estar disponível.",
  "campos": {}
}
```

Em erros de validação, `campos` indica o campo problemático:

```json
{
  "status": 400,
  "mensagem": "Dados inválidos.",
  "campos": { "capacidade": "A capacidade deve ser maior que zero." }
}
```

| Código | Quando |
|---|---|
| 400 | Dados inválidos: validação, JSON malformado, data ou parâmetro com formato errado, fim antes do início |
| 404 | Sala, usuário ou reserva não encontrados |
| 409 | Conflito: horário ocupado, e-mail repetido, exclusão de algo com histórico |
| 422 | Operação não permitida pelo estado atual: sala inativa, reserva cancelada |

## Como executar

### Com Docker (recomendado)

Pré-requisito: Docker com Compose.

```bash
cp .env.example .env          # Linux/macOS
copy .env.example .env        # Windows (PowerShell ou cmd)
```

Edite o `.env` e defina `DB_PASSWORD`. Depois:

```bash
docker compose up --build
```

A API fica em `http://localhost:8080` e o PostgreSQL em `localhost:5432`.

Para parar e apagar também os dados do banco:

```bash
docker compose down -v
```

### Localmente (sem Docker)

Pré-requisitos: JDK 25 e um PostgreSQL acessível.

1. Crie o banco `reservas`.
2. Defina as variáveis de ambiente:
   - `DB_PASSWORD` (obrigatória)
   - `DB_URL` (opcional, padrão `jdbc:postgresql://localhost:5432/reservas`)
   - `DB_USER` (opcional, padrão `postgres`)
3. Rode a aplicação:

```bash
./mvnw spring-boot:run
```

No IntelliJ, defina `DB_PASSWORD` em *Run → Edit Configurations → Environment variables*.

## Testes

```bash
./mvnw test
```

São 65 testes:

- **Domínio** (`ReservaTest`, `SalaTest`, `UsuarioTest`): regras de período, fronteiras do intervalo, status, validações e transições.
- **Serviços** (`ReservaServiceTest`, `SalaServiceTest`, `UsuarioServiceTest`): fluxos com repositórios simulados. Também verificam que nada é gravado quando uma regra falha.
- **Contrato HTTP** (`ReservaControllerTest`): códigos de status e formato dos erros.

Os testes não precisam de banco.

## Decisões de projeto

- **Regras no domínio.** As entidades validam a si mesmas. Os setters públicos foram removidos para que nenhuma regra seja contornada.
- **Operações que dependem do banco ficam no serviço.** A checagem de conflito precisa consultar o banco, então fica no `ReservaService`.
- **Concorrência.** Criar ou alterar uma reserva trava a linha da sala (`SELECT ... FOR UPDATE`) até o fim da transação. Assim, dois pedidos simultâneos para a mesma sala passam um de cada vez e o segundo vê a reserva do primeiro. Ao disparar oito pedidos simultâneos para o mesmo horário, apenas um recebeu 201.
- **Lazy loading.** As associações `Reserva → Sala` e `Reserva → Usuario` são `LAZY`. As respostas usam DTOs com ids, o que evita carregar objetos que não serão usados. `open-in-view` está desligado para que o acesso ao banco fique na camada de serviço.
- **Nome de sala não é único.** Pode haver "Sala 101" em andares diferentes. Quem precisa de unicidade deve tratar isso com o identificador.
- **Exclusão lógica para reservas.** Cancelar mantém o histórico. Excluir sala ou usuário com histórico é bloqueado, em vez de apagar as reservas junto.
- **Credenciais fora do código.** A senha do banco vem de variável de ambiente. O `.env` não vai para o Git.
- **Schema com `ddl-auto=update`.** Adequado ao estudo. Em produção, eu usaria Flyway ou Liquibase.

## Limitações conhecidas

- Não há autenticação nem autorização: qualquer cliente pode alterar qualquer recurso.
- `ddl-auto=update` não versiona o schema (sem migrations).
- O teste de concorrência é manual (ver acima) e não faz parte da suíte automática. Automatizá-lo exigiria Testcontainers.

## Estrutura

```
src/main/java/com/br/reservas
├── controller/   endpoints HTTP (finos, só traduzem HTTP para o serviço)
├── service/      regras de orquestração: conflito, bloqueio, existência
├── model/        entidades com regras de domínio
├── repository/   acesso ao banco e consultas por período
├── dto/          contratos de entrada e saída da API
└── exception/    exceções de negócio e tratamento global de erros
```
