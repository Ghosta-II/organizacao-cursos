# Etapa 1: compila o projeto e gera o .jar
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia primeiro só o necessário para baixar as dependências (aproveita o cache do Docker)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
# Os testes já rodam no CI (./mvnw verify), então aqui são pulados
RUN ./mvnw -B -DskipTests package

# Etapa 2: imagem final, só com o JRE e o .jar
FROM eclipse-temurin:21-jre
WORKDIR /app

# Roda a aplicação com um usuário sem privilégios de root
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
