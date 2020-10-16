FROM adoptopenjdk/openjdk11:alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
#COPY src/main/resources/application.properties .
COPY docker_application.properties application.properties
ENTRYPOINT ["java","-jar","/app.jar", "--spring.config.location=file:./application.properties"]
