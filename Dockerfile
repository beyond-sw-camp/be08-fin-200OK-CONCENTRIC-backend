FROM eclipse-temurin:17-jre-alpine
COPY build/libs/backend-0.0.1-SNAPSHOT.jar /root
WORKDIR /root
CMD [ "java", "-jar", "backend-0.0.1-SNAPSHOT.jar" ]