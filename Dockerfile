# Estágio de compilação com JDK 26
FROM eclipse-temurin:26 AS builder
WORKDIR /app

# Instala Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copia pom.xml e código fonte
COPY pom.xml .
COPY src ./src

# Compila o projeto e gera os stubs do gRPC e JAR com dependências (shaded jar)
RUN mvn clean package -DskipTests

# Estágio final de execução com JDK 26
FROM eclipse-temurin:26
WORKDIR /app

# Copia o JAR empacotado com todas as dependências
COPY --from=builder /app/target/distsys-chat-1.0-SNAPSHOT.jar /app/chat.jar

# Portas padrão: 8080 (Sockets), 50051 (gRPC), 1099 (RMI Registry)
EXPOSE 8080 50051 1099

ENTRYPOINT ["java", "-cp", "/app/chat.jar"]
CMD ["com.pedrobazza.distsyschat.app.ServerApp", "socket", "8080"]
