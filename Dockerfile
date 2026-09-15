
FROM gradle:8.7-jdk21 AS builder

WORKDIR /build

# Копируем исходный код в контейнер
COPY . .

# Собираем shadowJar. Флаг -x test отключает прогон тестов при сборке образа для скорости
RUN gradle shadowJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем собранный JAR из первой стадии
COPY --from=builder /build/build/libs/generator_mock_file-1.0-all.jar /app/app.jar

# Создаем папки, к которым обращается ваше приложение по умолчанию (AppConfig)
RUN mkdir -p /app/config /app/logs /app/generated

# Открываем порт, на котором слушает ваш HttpServer (по умолчанию 8080)
EXPOSE 8080

# Точка входа. Оставляем возможность передавать Java-аргументы (например -Dgenerator.file.property=...)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]