# API media-service

Базовый путь:

```text
/photos
```

Методы:

```text
GET    /photos
GET    /photos/{photoId}
POST   /photos
PATCH  /photos/{photoId}
```

Swagger/OpenAPI пока не добавлен.

## Создание фото

`POST /photos` принимает `multipart/form-data`:

```text
metadata - JSON с title, description, photoDate
file     - файл фото
```

Пример `metadata`:

```json
{
  "title": "Это котик",
  "description": "Тут описание котика красивого",
  "photoDate": "2025-06-01"
}
```

Пример `curl`:

```bash
curl -X POST http://localhost:8080/photos \
  -F "metadata={\"title\":\"Это котик\",\"description\":\"Тут описание котика красивого\",\"photoDate\":\"2025-06-01\"};type=application/json" \
  -F "file=@cat.jpg;type=image/jpeg"
```

При локальном запуске через Docker ссылка на файл может содержать host `minio`, потому что presigned URL генерируется внутри Docker-сети.
