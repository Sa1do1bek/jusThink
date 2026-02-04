# 1. Используем официальный образ Java с Maven
FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем pom.xml и зависимости заранее (чтобы кешировалось)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники
COPY src ./src

# Собираем проект
RUN mvn clean package -DskipTests

# 2. Используем минимальный JDK образ для запуска
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Копируем jar из предыдущего build stage
COPY --from=build /app/target/*.jar app.jar

# Указываем порт и команду запуска
# Fly.io и Railway используют переменную PORT из окружения
EXPOSE 8081
# Используем переменную PORT из окружения, если она установлена
CMD ["sh", "-c", "java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default} -jar app.jar"]
