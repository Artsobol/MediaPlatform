#!/bin/sh
set -e

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  -v media_user="$MEDIA_SERVICE_USER" \
  -v media_password="$MEDIA_SERVICE_PASSWORD" \
  -v media_db="$MEDIA_SERVICE_DB" <<-'EOSQL'
CREATE USER :"media_user" WITH PASSWORD :'media_password';
CREATE DATABASE :"media_db" OWNER :"media_user";
GRANT ALL PRIVILEGES ON DATABASE :"media_db" TO :"media_user";
EOSQL

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$MEDIA_SERVICE_DB" \
  -v media_user="$MEDIA_SERVICE_USER" <<-'EOSQL'
GRANT USAGE, CREATE ON SCHEMA public TO :"media_user";
ALTER SCHEMA public OWNER TO :"media_user";
EOSQL