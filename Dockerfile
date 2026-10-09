FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/generator_mock_file-1.0-all.jar /app/app.jar

RUN mkdir -p /app/config /app/logs /app/generated

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]