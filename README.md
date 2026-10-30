# Order Flow Microservice

Microsserviço de processamento de pedidos desenvolvido como solução para o desafio técnico do **BTG Pactual**.

O serviço **consome pedidos de uma fila RabbitMQ**, **calcula o valor total** de cada pedido, **persiste os dados no MongoDB** e expõe uma **API REST** para consultar os pedidos de um cliente, junto com o valor total gasto por ele.

---

## Visão geral

O fluxo resolvido pelo projeto é:

1. Um sistema externo publica um evento de **pedido criado** na fila `order-created`.
2. O microsserviço consome a mensagem, calcula o total do pedido (soma de `preço × quantidade` de cada item) e grava o pedido no MongoDB.
3. Um cliente da API consulta `GET /customers/{customerId}/orders` e recebe:
   - a **lista paginada** de pedidos do cliente;
   - o **valor total** somando todos os pedidos dele (`totalOnOrders`);
   - os **dados de paginação**.

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java | Linguagem principal |
| Spring Boot | Framework base da aplicação |
| Spring Web | API REST |
| Spring AMQP (RabbitMQ) | Consumo de mensagens da fila |
| Spring Data MongoDB | Persistência e consultas (repositório, `MongoTemplate` e aggregation) |
| MongoDB | Banco de dados de pedidos |
| RabbitMQ | Mensageria |
| Docker Compose | Subida local da infraestrutura (MongoDB + RabbitMQ) |
| Maven (`mvnw`) | Build e gerenciamento de dependências |

## Arquitetura e fluxo

```
┌──────────────┐   JSON    ┌────────────────────┐        ┌───────────────┐
│ Produtor     │ ────────▶ │ RabbitMQ           │        │ MongoDB       │
│ de pedidos   │           │ fila: order-created│        │ tb_orders     │
└──────────────┘           └─────────┬──────────┘        └───────▲───────┘
                                     │                           │
                                     ▼                           │ save
                          ┌─────────────────────┐                │
                          │ OrderCreatedListener│                │
                          └─────────┬───────────┘                │
                                    ▼                            │
                          ┌─────────────────────┐ ───────────────┘
                          │ OrderService        │
                          └─────────▲───────────┘
                                    │
                          ┌─────────┴───────────┐
   GET /customers/{id}/   │ OrderController     │ ◀──── Cliente HTTP
   orders                 └─────────────────────┘
```

**Fluxo de escrita (assíncrono):** `RabbitMQ → OrderCreatedListener → OrderService.save → OrderRepository → MongoDB`

**Fluxo de leitura (síncrono):** `HTTP → OrderController → OrderService → MongoDB (consulta paginada + aggregation do total)`


## Detalhamento dos componentes

### `RabbitMqConfig`
- Declara a fila durável **`order-created`**, criada automaticamente caso ainda não exista.
- Registra o `JacksonJsonMessageConverter`, responsável por converter o JSON das mensagens em objetos Java (`OrderCreatedEvent`).

### `OrderCreatedListener`
- Anotado com `@RabbitListener(queues = "order-created")`.
- A cada mensagem, registra um log e delega o salvamento ao `OrderService`.

### `OrderService`
- **`save(OrderCreatedEvent)`**: converte o evento em `OrderEntity`, mapeia os itens e calcula o total com `BigDecimal` (soma de `preço × quantidade`; `0` caso não haja itens). Depois persiste via repositório.
- **`findAllByCustomerId(customerId, pageRequest)`**: retorna a página de pedidos do cliente já convertida para `OrderResponse`.
- **`findTotalOnOrdersByCostumerId(customerId)`**: usa uma **aggregation do MongoDB** (`match` por `customerId` + `group().sum("total")`) para calcular, no próprio banco, o valor total de todos os pedidos do cliente.

### `OrderRepository`
- Extende `MongoRepository<OrderEntity, Long>`.
- Define `findAllByCustomerId(...)`, com suporte a paginação.

### `OrderController`
- Expõe `GET /customers/{customerId}/orders`.
- Monta a resposta com o total gasto (`summary`), a lista de pedidos (`data`) e a paginação (`pagination`).

### DTOs de resposta
- `ApiResponse<T>`: envelope genérico com `summary`, `data` e `pagination`.
- `OrderResponse`: `orderId`, `customerId` e `total`.
- `PaginationResponse`: `page`, `pageSize`, `totalElements` e `totalPages`, construído a partir de um `Page<?>` do Spring Data.


## Contrato da mensagem (RabbitMQ)

Fila: `order-created`

```json
{
  "codigoPedido": 1001,
  "codigoCliente": 1,
  "itens": [
    { "produto": "lápis", "quantidade": 100, "preco": 1.10 },
    { "produto": "caderno", "quantidade": 10, "preco": 1.00 }
  ]
}
```

## API REST

### Listar pedidos de um cliente

```
GET /customers/{customerId}/orders?page=0&PageSize=10
```

| Parâmetro | Tipo | Padrão | Descrição |
|---|---|---|---|
| `customerId` | path | — | Código do cliente |
| `page` | query | `0` | Número da página (começa em 0) |
| `PageSize` | query | `10` | Quantidade de itens por página |

**Exemplo de resposta (`200 OK`):**

```json
{
  "summary": {
    "totalOnOrders": 120.00
  },
  "data": [
    {
      "orderId": 1001,
      "customerId": 1,
      "total": 120.00
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## Como executar

### Pré-requisitos

- JDK compatível com a versão definida no `pom.xml`
- Docker e Docker Compose

### 1. Subir a infraestrutura

O `docker-compose.yml` sobe o MongoDB e o RabbitMQ:

```bash
docker compose up -d
```

| Serviço | Porta | Acesso |
|---|---|---|
| MongoDB | `27017` | usuário `admin` / senha `123` |
| RabbitMQ (AMQP) | `5672` | — |
| RabbitMQ (painel) | `15672` | http://localhost:15672 |

> No painel do RabbitMQ, o login padrão da imagem é `guest` / `guest`.

### 2. Configurar a aplicação

Em `src/main/resources/application.properties`, aponte para os serviços do Docker. Exemplo:

```properties
spring.data.mongodb.uri=mongodb://admin:123@localhost:27017/orderflow?authSource=admin
spring.data.mongodb.auto-index-creation=true

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

> `auto-index-creation=true` garante a criação do índice `customer_id_index` declarado na entidade.

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

(no Windows: `mvnw.cmd spring-boot:run`)

### 4. Testar o fluxo completo

1. Acesse o painel do RabbitMQ em http://localhost:15672.
2. Vá em **Queues and Streams → `order-created` → Publish message**.
3. Cole o JSON do [contrato da mensagem](#contrato-da-mensagem-rabbitmq) e publique.
4. Consulte a API:

```bash
curl "http://localhost:8080/customers/1/orders"
```

## Decisões de projeto

- **Mensageria desacoplada:** a criação do pedido acontece fora do serviço; ele apenas reage ao evento. Isso permite processar pedidos de forma assíncrona e escalar o consumo independentemente da API.
- **Fila durável:** a fila `order-created` é declarada como durável, para que as mensagens não se percam caso o broker reinicie.
- **MongoDB:** o pedido é um agregado natural (pedido + itens), então os itens ficam **embutidos** no mesmo documento, evitando joins e deixando a leitura simples e rápida.
- **`BigDecimal` / `Decimal128`:** valores monetários usam `BigDecimal` no Java e `Decimal128` no Mongo, evitando imprecisão de `double`.
- **Índice em `customerId`:** a principal consulta da API filtra por cliente, então o campo é indexado.
- **Aggregation no banco:** o total gasto pelo cliente é calculado pelo MongoDB (`match` + `group`), sem carregar todos os pedidos em memória.
- **Paginação:** a listagem usa `PageRequest`, e a resposta devolve metadados para o consumidor navegar pelas páginas.
- **Records para DTOs:** eventos e respostas são imutáveis e concisos.
- **Camadas separadas:** listener/controller → service → repository, mantendo a regra de negócio isolada da entrada de dados.
