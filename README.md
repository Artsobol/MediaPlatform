# Media Platform

Media Platform - учебный микросервисный проект для работы с фото- и видеоконтентом. Цель проекта - постепенно собрать основу для загрузки, хранения, обработки и выдачи медиафайлов через независимые сервисы.

Сейчас в репозитории есть корневой Maven parent-проект и первый модуль `media-service`.

## Текущий статус

Реализован базовый `media-service` для работы с карточками фото:

- создание карточки фото;
- получение фото по id;
- получение списка фото;
- частичное обновление `title`, `description` и `photoDate`;
- хранение статуса фото;
- схема БД через Liquibase.

Загрузка файлов, файловое хранилище, обработка изображений, авторизация, API Gateway и Docker Compose пока не реализованы.

## Технологии

- Java 25
- Maven
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Liquibase
- MapStruct
- Lombok

## Структура проекта

```text
.
├── pom.xml
├── README.md
├── PLANS.md
├── code_review.md
└── media-service
    ├── pom.xml
    └── src
        ├── main
        │   ├── java/io/github/artsobol/mediaservice
        │   │   ├── config
        │   │   ├── exception
        │   │   └── feature/photo
        │   └── resources
        │       ├── application.yaml
        │       └── db/changelog
        └── test
```

## media-service

`media-service` отвечает за метаданные фото. Сейчас сервис работает с таблицей `photos`.

Основные поля фото:

- `id`;
- `originalImageKey`;
- `title`;
- `description`;
- `photoDate`;
- `photoStatus`.

Статусы фото описаны в `PhotoStatus`.

## API

Базовый путь контроллера:

```text
/photos
```

Доступные методы:

```text
GET    /photos
GET    /photos/{photoId}
POST   /photos
PATCH  /photos/{photoId}
```

Swagger/OpenAPI в проект пока не добавлен.

## Локальный запуск

Для запуска нужен PostgreSQL.

Текущие настройки подключения находятся в `media-service/src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/media_service
    username: postgres
    password: postgres
```

Перед запуском создай локальную БД:

```sql
CREATE DATABASE media_service;
```

Запуск сервиса:

```bash
mvn -pl media-service spring-boot:run
```

По умолчанию Spring Boot запустит приложение на порту `8080`, если порт не переопределен через конфигурацию.

## Проверки

Минимальная проверка проекта:

```bash
mvn test
```

Если локальная PostgreSQL не запущена или база `media_service` не создана, тест `contextLoads` может упасть при поднятии Spring context.

## Планируемое развитие

В дальнейшем проект может быть расширен отдельными сервисами:

- `processing-service` - обработка фото и видео;
- `gateway-service` - единая точка входа;
- `auth-service` - аутентификация и авторизация;
- отдельное файловое хранилище для оригиналов и производных медиафайлов.

Финальный набор сервисов может меняться по мере развития проекта.
