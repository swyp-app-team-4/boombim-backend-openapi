FROM amazoncorretto:17-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app.jar
COPY env/prod.env /env/prod.env
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]