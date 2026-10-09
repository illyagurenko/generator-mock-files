# Generator Service

Сервис генерации тестовых банковских файлов и тестовых записей для контура GRU.

## Назначение

`generator-service` используется в двух сценариях:

1. Генерация банковских файлов фиксированного формата и доставка их в `food-card`.
2. Прямая генерация тестовых записей в Oracle-таблицу `GRU.GRU_VISTA_TAB`.

Основной сценарий полного E2E-теста:

```text
Generator
   ↓ файл
Food-card
   ↓
Oracle GRU.GRU_VISTA_TAB
   ↓
App-adapter
   ↓
Kafka gru.balance.request
```

Альтернативный быстрый сценарий без файла:

```text
Generator
   ↓ JDBC
Oracle GRU.GRU_VISTA_TAB
```

## Стек

- Java 21
- Gradle
- Shadow JAR
- Oracle JDBC (`ojdbc11`)
- HTTP server без Spring Boot
- Faker для генерации тестовых данных
- Docker

## Порт

По умолчанию:

```text
8080
```

## REST API

### Генерация файлов

```http
POST /api/parametres
Content-Type: application/json
```

Пример:

```json
{
  "codeBank": "044525225",
  "codeFilial": "0001",
  "nameAES": "TEST_AES",
  "countRecords": 10,
  "countFiles": 1,
  "inTime": null
}
```

Основные поля:

| Поле | Назначение |
|---|---|
| `codeBank` | код банка |
| `codeFilial` | код филиала |
| `nameAES` | имя/идентификатор AES |
| `countRecords` | количество записей в одном файле |
| `countFiles` | количество генерируемых файлов |
| `inTime` | время отложенной обработки; при `null` создаётся IMMEDIATE-файл |

`inTime` записывается в заголовок файла. Сам generator не ждёт этого времени.

Генерация выполняется асинхронно: HTTP-ответ подтверждает принятие задания, а не завершение генерации всех файлов.

### Прямая генерация в Oracle

```http
POST /api/db/populate
Content-Type: application/json
```

Используется для тестирования цепочки:

```text
Generator → GRU.GRU_VISTA_TAB → App-adapter → Kafka
```

без прохождения через `food-card`.

## Формат файла

Файл состоит из заголовка и body-записей фиксированной длины.

Длина body-записи:

```text
152 символа
```

Для чистого позитивного теста:

```properties
generator.files.percentage.invalid=0
```

## Способы доставки файла

Поддерживаются:

- multipart HTTP;
- raw/chunked HTTP;
- gRPC.

В текущем тестовом контуре используется multipart:

```properties
generator.files.file.send.chunked.enabled=false
generator.files.file.send.multipart.enabled=true
generator.files.file.send.grpc.enabled=false
```

Получатель:

```properties
generator.files.receiver.url=http://food-card-app:8081/api/files/upload
```

gRPC endpoint при необходимости:

```properties
generator.files.grpc.receiver.url=food-card-app:9090
```

> Не рекомендуется одновременно отправлять один и тот же файл через multipart и складывать его в shared-directory, которую сканирует `food-card`: один файл может попасть в обработку двумя путями.

## Актуальный `application.properties`

Для Docker:

```properties
generator.files.server.port=8080

generator.files.file.out=/app/generated
generator.files.file.charset=UTF-8
generator.files.faker.locale=ru

generator.files.server.endpoint.post=/api/parametres

generator.files.log.file-path=./logs/app.log
generator.files.logback.config.path=./config/file-logback.xml
generator.files.crypto.key.path=./config/secret.key

generator.files.percentage.invalid=0

generator.files.file.send.chunked.enabled=false
generator.files.file.send.multipart.enabled=true
generator.files.file.send.grpc.enabled=false

generator.files.grpc.receiver.url=food-card-app:9090
generator.files.receiver.url=http://food-card-app:8081/api/files/upload

generator.db.url=jdbc:oracle:thin:@//food_card_oracle:1521/XE
generator.db.user=acc_app_adapter
generator.db.password=root
generator.db.endpoint.post=/api/db/populate
```

При локальном запуске вместо Docker hostname Oracle используется:

```properties
generator.db.url=jdbc:oracle:thin:@//localhost:1521/XE
```

## Внешний конфигурационный файл

Generator читает внешний `application.properties`, только если передан JVM-параметр:

```text
-Dgenerator.file.property=/app/config/application.properties
```

Для внешнего Logback:

```text
-Dgenerator.file.logback=/app/config/file-logback.xml
```

Пример запуска:

```bash
java \
  -Dgenerator.file.property=/app/config/application.properties \
  -Dgenerator.file.logback=/app/config/file-logback.xml \
  -jar /app/app.jar
```

## Сборка

Рекомендуемый способ:

```bash
./gradlew clean shadowJar -x test
```

Windows:

```cmd
gradlew.bat clean shadowJar -x test
```

Для Shadow JAR рекомендуется:

```gradle
shadowJar {
    mergeServiceFiles()
}
```

Это необходимо, чтобы JDBC service metadata корректно попала в fat JAR.

Готовый JAR:

```text
build/libs/generator_mock_file-1.0-all.jar
```

## Docker

Из-за проблем с загрузкой Gradle-plugin внутри Docker используется runtime-only образ: проект собирается локально, а в image копируется готовый fat JAR.

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/generator_mock_file-1.0-all.jar /app/app.jar

RUN mkdir -p /app/config /app/logs /app/generated

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]
```

### Пример `docker-compose.yml`

```yaml
version: "3.8"

services:
  generator-app:
    build: .
    container_name: file-generator-service

    ports:
      - "8080:8080"

    volumes:
      - ./config:/app/config
      - ./logs:/app/logs
      - ./generated:/app/generated

    environment:
      TZ: Europe/Moscow

    command:
      - java
      - -Dgenerator.file.property=/app/config/application.properties
      - -Dgenerator.file.logback=/app/config/file-logback.xml
      - -jar
      - /app/app.jar

    restart: unless-stopped

    networks:
      - food_bridge

networks:
  food_bridge:
    external: true
```

Если тестируется shared-directory сценарий, `/app/generated` можно примонтировать в общую директорию с `food-card`.

## Запуск

1. Поднять Oracle и `food-card`.
2. Убедиться, что существует Docker network:

```bash
docker network inspect food_bridge
```

3. Собрать JAR:

```cmd
gradlew.bat clean shadowJar -x test
```

4. Собрать и запустить generator:

```bash
docker compose up -d --build
```

5. Проверить:

```bash
docker logs -f file-generator-service
```

## Проверка Docker DNS

Oracle:

```bash
docker exec file-generator-service getent hosts food_card_oracle
```

Food-card:

```bash
docker exec file-generator-service getent hosts food-card-app
```

## Быстрый E2E-тест через файл

1. Запустить `food-card`.
2. Запустить `app-adapter`.
3. Запустить Kafka.
4. Запустить Docker console consumer на `gru.balance.request`.
5. Вызвать:

```http
POST http://localhost:8080/api/parametres
```

6. Проверить:
  - generator создал и отправил файл;
  - food-card обработал его;
  - в `GRU.GRU_VISTA_TAB` появились строки `WAIT`;
  - adapter забрал их;
  - сообщение появилось в `gru.balance.request`.

## Быстрый тест через БД

Вызвать:

```http
POST http://localhost:8080/api/db/populate
```

После этого записи должны появиться непосредственно в:

```text
GRU.GRU_VISTA_TAB
```

## Полезные замечания

- `food-card-app` — hostname приложения `food-card`; его используют HTTP/gRPC.
- `food_card_oracle` — hostname Oracle-контейнера; его используют JDBC.
- Для Oracle XE используется service-name URL:

```text
jdbc:oracle:thin:@//host:1521/XE
```

а не SID-форма.
