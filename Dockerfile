# Etapa 1: compila o projeto e gera o .jar.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Baixa as dependências antes de copiar o código: enquanto o pom não mudar, esta camada fica em cache.
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# Etapa 2: imagem final, com só o JRE e o .jar. Nada de Maven ou código-fonte.
FROM eclipse-temurin:25-jre
WORKDIR /app

# Não roda como root.
RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

# Configuração (banco, senha, perfil) vem de variáveis de ambiente, não da imagem.
ENTRYPOINT ["java", "-jar", "app.jar"]
