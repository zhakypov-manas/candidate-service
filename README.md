# Candidate Service

Микросервис платформы CV-Scan для управления кандидатами.

## Архитектура

```
cv-parser → [Kafka: cv.parsed] → candidate-service → [Kafka: candidate.status.changed] → notification-service
```

## Стек

- Java 17, Spring Boot 3.2
- PostgreSQL + Flyway
- Apache Kafka
- Testcontainers (интеграционные тесты)
- Springdoc OpenAPI

## Быстрый старт

### 1. Поднять инфраструктуру

```bash
docker-compose up -d
```

### 2. Сборка и тесты

```bash
./gradlew build
```

### 3. Только тесты

```bash
./gradlew test
```

### 4. Запуск приложения

```bash
./gradlew bootRun
```

Swagger UI: http://localhost:8080/swagger-ui.html

### 5. Публикация тестовых событий Kafka

```bash
printf '%s\n' "$(tr -d '\n' < test-events/cv-parsed-sample.json)" \
  | kcat -P -b localhost:9092 -t cv.parsed -l

kcat -P -b localhost:9092 -t cv.parsed -l test-events/cv-parsed-bulk.ndjson

tr -d '\n' < test-events/cv-parsed-sample.json \
  | kafka-console-producer.sh --bootstrap-server localhost:9092 --topic cv.parsed
```

## Принятые решения

### Идентификатор кандидата

ID формируется из имени (транслитерация + slug). При коллизии добавляется суффикс UUID. Это делает ID читаемым в URL (`/api/v1/candidates/ivanov-ivan-ivanovich`). При создании через Kafka используется `candidateId` из события как-есть.

### Идемпотентность Kafka Consumer

Дубль определяется по паре `candidateId + parsedAt`. Это позволяет корректно обработать повторную доставку одного и того же события без создания дубля. Если кандидат уже существует с тем же id, событие также игнорируется.

### Машина состояний

Реализована в `StatusService` через статическую карту допустимых переходов (`EnumMap<CandidateStatus, Set<CandidateStatus>>`). Легко расширяется без изменения бизнес-логики.

### Хранение criteria / experience / questions

Хранятся как JSONB в PostgreSQL. Это избавляет от лишних таблиц для слабоструктурированных данных, при этом сохраняет возможность индексирования при необходимости.

### PATCH vs PUT

`PUT /candidates/{id}` не меняет статус — это явно проверяется в `CandidateService.update()`. Статус меняется только через `PATCH /candidates/{id}/status`.

### MDC / корреляция логов

`MdcFilter` добавляет `requestId` (из заголовка `X-Request-Id` или генерирует UUID) в MDC для каждого входящего запроса.

### Тестирование

- Модульные тесты (`StatusServiceTest`, `CvParsedConsumerTest`) — без Spring контекста, только Mockito.
- Интеграционные тесты (`CandidateApiIntegrationTest`, `KafkaIntegrationTest`) — реальные PostgreSQL и Kafka через Testcontainers.

## Что не успел / что можно улучшить

- DLT (Dead Letter Topic) для необработанных Kafka-сообщений
- Spring Data Specifications или Querydsl для более гибкой фильтрации (сейчас JPQL-запрос)
- `@EntityGraph` — в текущей реализации N+1 не является проблемой, так как JSONB-поля хранятся в той же таблице
- Более полное покрытие тестами (сейчас основные сценарии покрыты)
- CI/CD pipeline протестировать

## Структура проекта

```
src/
├── main/java/kg/tunduk/cvscan/candidate/
│   ├── controller/CandidateController.java
│   ├── service/
│   │   ├── CandidateService.java
│   │   ├── StatusService.java
│   │   └── CandidateMapper.java
│   ├── messaging/
│   │   ├── CvParsedConsumer.java
│   │   └── StatusChangedProducer.java
│   ├── repository/
│   │   ├── CandidateRepository.java
│   │   └── StatusHistoryRepository.java
│   ├── model/
│   │   ├── Candidate.java
│   │   ├── CandidateStatus.java
│   │   ├── Verdict.java
│   │   └── StatusHistory.java
│   ├── dto/
│   │   ├── CandidateWriteRequest.java
│   │   ├── CandidateResponse.java
│   │   ├── CandidatePage.java
│   │   ├── StatusChangeRequest.java
│   │   ├── StatusHistoryEntry.java
│   │   └── event/
│   │       ├── CvParsedEvent.java
│   │       └── StatusChangedEvent.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── CandidateNotFoundException.java
│   │   ├── InvalidStatusTransitionException.java
│   │   └── DuplicateEmailException.java
│   └── config/
│       ├── KafkaConfig.java
│       ├── JacksonConfig.java
│       └── MdcFilter.java
└── test/java/kg/tunduk/cvscan/candidate/
    ├── service/
    │   ├── StatusServiceTest.java
    │   └── CvParsedConsumerTest.java
    └── integration/
        ├── AbstractIntegrationTest.java
        ├── CandidateApiIntegrationTest.java
        └── KafkaIntegrationTest.java
```
