FROM eclipse-temurin:17-jre-alpine
COPY build/libs/be08-fin-200OK-CONCENTRIC-backend-0.0.1-SNAPSHOT.jar /root
WORKDIR /root
CMD [ "java", "-jar", "be08-fin-200OK-CONCENTRIC-backend-0.0.1-SNAPSHOT.jar" ]