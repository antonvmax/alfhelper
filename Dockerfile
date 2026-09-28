FROM nx-docker-adt.alfastrah.ru/mvn-base:17-jdk AS build
COPY adapter-contract-signed /app/adapter-contract-signed
COPY model-contract-signed /app/model-contract-signed
COPY ws-contract-signed /app/ws-contract-signed
COPY pom.xml /app/pom.xml
RUN mvn -f /app/pom.xml clean package

FROM nx-docker-adt.alfastrah.ru/openjdk:11-jre-slim

COPY --from=build /app/ws-contract-signed/target/*.jar /app/contract-signed.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar", "/app/contract-signed.jar"]
#2
