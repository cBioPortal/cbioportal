FROM maven:3-eclipse-temurin-17 as build

WORKDIR /assistant

COPY pom.xml .

RUN mvn dependency:go-offline --fail-never

COPY src ./src

RUN mvn install package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /assistant

COPY --from=build /assistant/target/assistant.jar .

ENTRYPOINT ["java","-jar","assistant.jar"]
