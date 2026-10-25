FROM eclipse-temurin:21-jre-alpine

ENV TZ=America/Bogota
ENV JAVA_OPTS="-XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY application/app-service/build/libs/*.jar /app/app.jar
RUN chown -R appuser:appgroup /app

USER appuser

HEALTHCHECK --interval=60s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --spider -q http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]
