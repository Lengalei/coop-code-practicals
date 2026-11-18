# Payments API

Spring Boot 3 REST API for account management, customer transactions, transfers,
and balance enquiries. Uses Java 17, Spring Data JPA, SQL Server, Spring Security,
JWT, and RabbitMQ.

## API Endpoints

### Authentication

| Method | Endpoint | Authentication | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | None | Register a user |
| `POST` | `/api/v1/auth/login` | None | Authenticate and return a JWT |

`POST /api/v1/auth/register` and `POST /api/v1/auth/login` accept a username and password. Set `APP_USERNAME` and `APP_PASSWORD` in the ignored local `.env` file, then load those variables before making requests:

```bash
set -a
. ./.env
set +a

curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d "$(jq -n --arg username "$APP_USERNAME" --arg password "$APP_PASSWORD" \
    '{username: $username, password: $password}')"
```

Protected endpoints require `Authorization: Bearer <token>`.

### Accounts

| Method | Endpoint | Authentication | Description |
|---|---|---|---|
| `POST` | `/api/v1/accounts` | Required | Create an account |
| `GET` | `/api/v1/accounts/{accountNumber}/balance` | Required | Get account balance |

Create-account request:

```json
{
  "ownerName": "Alice Wanjiru",
  "openingBalance": 50000.00
}
```

Account numbers are 10 digits.

### Transactions

| Method | Endpoint | Authentication | Description |
|---|---|---|---|
| `POST` | `/api/v1/transactions` | Required | Record a deposit or withdrawal |

```json
{
  "reference": "T-001",
  "accountNumber": "1000000001",
  "type": "DEPOSIT",
  "channel": "MPESA",
  "amount": 500.00,
  "msisdn": "254700000000",
  "description": "M-Pesa deposit"
}
```

Supported transaction types: `DEPOSIT`, `WITHDRAWAL`.

### Transfers

| Method | Endpoint | Authentication | Description |
|---|---|---|---|
| `POST` | `/api/v1/transfers` | Required | Transfer funds between accounts |

```json
{
  "reference": "X-001",
  "fromAccount": "1000000001",
  "toAccount": "1000000002",
  "amount": 500.00,
  "description": "Account transfer"
}
```

## Error Responses

```json
{
  "timestamp": "...",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Insufficient funds on account 100****001",
  "path": "/api/v1/transfers"
}
```

| Status | Meaning |
|---|---|
| `400` | Invalid request or same-account transfer |
| `401` | Missing or invalid JWT |
| `403` | Insufficient permissions |
| `404` | Account not found |
| `409` | Duplicate reference or concurrent update |
| `422` | Insufficient funds or unsupported transfer |
| `500` | Unexpected server error |

## Code Structure

```text
src/main/java/com/coop/payments
├── config/       Application configuration and startup services
├── controller/   HTTP endpoints
├── dto/          Request and response records
├── entity/       JPA entities
├── exception/    Domain exceptions and global error handling
├── messaging/    RabbitMQ events, publisher, and consumer
├── repository/   Spring Data JPA repositories
├── security/     JWT and user-details implementation
├── service/      Business logic and transactions
└── util/         Shared utilities
```

## Implementation

- Money is represented with `BigDecimal` and stored with precision 19 and scale 2.
- Transaction and transfer operations use `@Transactional`.
- Account updates use pessimistic database locks.
- Opposite transfers lock accounts in the same order to prevent deadlocks.
- Transaction and transfer references are unique and provide idempotency.
- Transfer events are published after database commit.
- RabbitMQ failures are retried before delivery to the dead-letter queue.
- Account numbers are masked in logs.
- Requests include a correlation ID through `X-Correlation-Id` or the generated ID.
- Passwords are hashed with BCrypt.
- Authentication uses stateless JWT-based Spring Security.
