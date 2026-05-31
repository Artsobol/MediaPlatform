# Media Platform

Media Platform - учебный микросервисный проект для работы с фото- и видеоконтентом. Цель проекта - постепенно собрать основу для загрузки, хранения, обработки и выдачи медиафайлов через независимые сервисы.

Сейчас в репозитории есть корневой Maven parent-проект и первый модуль `media-service`.

## Текущий статус

`media-service` умеет:

- хранить метаданные фото в PostgreSQL;
- загружать оригиналы фото в MinIO;
- получать фото по id и списком;
- частично обновлять данные фото;
- управлять схемой БД через Liquibase;
- запускаться локально через Docker Compose.

Обработка изображений, авторизация, API Gateway и отдельные сервисы обработки пока не реализованы.

## Технологии

- Java 25
- Maven
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Liquibase
- MinIO
- MapStruct
- Lombok
- Docker Compose

## Структура проекта

```text
.
├── docker
├── docs
├── media-service
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
```

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

- `processing-service` - обработка фото и видео;
- `gateway-service` - единая точка входа;
- `auth-service` - аутентификация и авторизация;
- отдельное файловое хранилище для оригиналов и производных медиафайлов.

Финальный набор сервисов может меняться по мере развития проекта.
