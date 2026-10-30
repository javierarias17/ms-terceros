FROM eclipse-temurin:21-jre-alpine

ENV TZ=America/Bogota
# Sin esto, la imagen base Alpine no tiene LANG definido y la JVM detecta
# sun.jnu.encoding=ANSI_X3.4-1968 (ASCII puro) al leer variables de entorno — cualquier
# tilde/ñ en un env var (ej. RUT_VALIDATION_TIPO_CONTRIBUYENTE_NATURAL) llega corrupta.
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_OPTS="-XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY application/app-service/build/libs/*.jar /app/app.jar
RUN chown -R appuser:appgroup /app

USER appuser

HEALTHCHECK --interval=60s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --spider -q http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]
