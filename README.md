# Media Platform

Media Platform - учебный микросервисный проект для работы с фото- и видеоконтентом. Цель проекта - постепенно собрать
основу для загрузки, хранения, обработки и выдачи медиафайлов через независимые сервисы.

Сейчас в репозитории есть корневой Maven parent-проект и первый модуль `media-service`.

## Текущий статус

`media-service` умеет:

- хранить метаданные фото в PostgreSQL;
- загружать оригиналы фото в MinIO;
- получать фото по id и списком;
- частично обновлять данные фото;
- сохранять запросы на обработку фото в transactional outbox;
- отправлять `PhotoProcessingRequestedEvent` в Kafka-топик
  `photo.processing.requested.v1`;
- повторять неуспешные отправки и отмечать опубликованные события статусом
  `PUBLISHED`;
- управлять схемой БД через Liquibase;
- запускаться локально вместе с PostgreSQL, MinIO и Kafka через Docker Compose.

Consumer и отдельный `processing-service` пока не реализованы: сжатие изображений
и создание thumbnail еще не выполняются.

## Поток загрузки фото

1. `media-service` проверяет файл и сохраняет метаданные фото в PostgreSQL.
2. Оригинал загружается в MinIO.
3. В одной транзакции с обновлением метаданных создается запись в таблице
   `retryable_tasks`.
4. Планировщик отправляет событие в Kafka с ключом `photoId`.
5. После подтверждения от Kafka запись получает статус `PUBLISHED`. При ошибке
   отправка будет повторена позднее.

События доставляются как минимум один раз, поэтому будущий Consumer должен быть
идемпотентным и учитывать `eventId`.

## Технологии

- Java 25
- Maven
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Liquibase
- MinIO
- Apache Kafka 4.3.1 в KRaft-режиме для локальной разработки
- MapStruct
- Lombok
- Docker Compose

## Структура проекта

```text
.
├── docker
├── docs
├── media-service
│   └── src/main/java
│       ├── feature/photo
│       └── infrastructure
│           ├── messaging/kafka
│           └── outbox
├── docker-compose.yml
├── pom.xml
├── README.md
├── PLANS.md
└── code_review.md
```

## Документация

- [Локальный запуск](docs/local-development.md)
- [API media-service](docs/api.md)

## Быстрый запуск

```bash
cp .env.example .env
docker compose up -d --build
```

Основные адреса:

```text
media-service: http://localhost:8080
MinIO Console: http://localhost:9001
Kafka: localhost:9092
```

При запуске вне Docker адрес Kafka задается через
`KAFKA_BOOTSTRAP_SERVERS` (по умолчанию `localhost:9092`). В Docker Compose
`media-service` использует внутренний адрес `kafka:19092`.

## Проверки

```bash
mvn test
```

Для сборки только `media-service`:

```bash
mvn -pl media-service -am clean package -DskipTests
```

## Планируемое развитие

В дальнейшем проект может быть расширен отдельными сервисами:

- `processing-service` - чтение `photo.processing.requested.v1`, загрузка
  оригинала из MinIO, сжатие изображения и создание thumbnail;
- `gateway-service` - единая точка входа;
- `auth-service` - аутентификация и авторизация;
- отдельное файловое хранилище для оригиналов и производных медиафайлов.

Финальный набор сервисов может меняться по мере развития проекта.
