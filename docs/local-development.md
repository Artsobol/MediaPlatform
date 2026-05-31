# Локальный запуск

## Docker Compose

Скопируй пример переменных окружения:

```bash
cp .env.example .env
```

Запусти инфраструктуру и сервис:

```bash
docker compose up -d --build
```

Адреса:

```text
media-service: http://localhost:8080
MinIO API:     http://localhost:9000
MinIO Console: http://localhost:9001
```

MinIO по умолчанию:

```text
login:    admin
password: adminadmin
```

Для загрузки файлов нужен bucket:

```text
photos
```

Сейчас bucket создается вручную через MinIO Console. Автоматическое создание bucket пока не реализовано.

Остановить контейнеры:

```bash
docker compose down
```

## Переменные окружения

PostgreSQL-переменные описаны в `.env.example`.

MinIO-переменные можно добавить при необходимости:

```text
MINIO_ROOT_USER
MINIO_ROOT_PASSWORD
MINIO_BUCKET
```

Для `media-service` в `docker-compose.yml` задаются:

```text
MEDIA_SERVICE_DATASOURCE_URL
MEDIA_SERVICE_DATASOURCE_USER
MEDIA_SERVICE_DATASOURCE_PASSWORD
APP_MINIO_ENDPOINT
APP_MINIO_ACCESS_KEY
APP_MINIO_SECRET_KEY
APP_MINIO_BUCKET
```

## Запуск без Docker

Для запуска без Docker нужны PostgreSQL и S3-compatible хранилище, например MinIO.

Настройки можно переопределить переменными окружения:

```text
MEDIA_SERVICE_DATASOURCE_URL
MEDIA_SERVICE_DATASOURCE_USER
MEDIA_SERVICE_DATASOURCE_PASSWORD
APP_MINIO_ENDPOINT
APP_MINIO_ACCESS_KEY
APP_MINIO_SECRET_KEY
APP_MINIO_BUCKET
```

Запуск:

```bash
mvn -pl media-service spring-boot:run
```

## Проверки

```bash
mvn test
mvn -pl media-service -am clean package -DskipTests
docker compose config
```
