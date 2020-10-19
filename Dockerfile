FROM openjdk:latest
ADD target/backend-latest.jar backend.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-jar", "backend.jar"]  