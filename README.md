# ms-tercero

Base de un microservicio en **arquitectura hexagonal** (ports & adapters): dominio puro
(`domain/model`, `domain/ports`, `domain/usecases`) desacoplado de los frameworks, e infraestructura
que implementa esos ports (`infrastructure/driven-adapters`) y expone la API
(`infrastructure/entry-points`).

Pensado para desplegarse en Azure fuera de AKS (p. ej. Azure Container Apps o App Service for
Containers), por eso **no incluye** contratos OpenAPI contract-first con codegen, Helm charts ni
pipelines de CI/CD — se agregan según el mecanismo de despliegue que elijas.

## Estructura

```
domain/
  model/      → entidades y excepciones de dominio, sin dependencias de frameworks
  ports/      → interfaces de los gateways salientes (lo que el dominio necesita del exterior)
  usecases/   → orquestación de negocio: clases planas (sin @Service), reciben sus ports por constructor

infrastructure/
  driven-adapters/
    rest-services/example-client-api/   → implementación de ejemplo de un port vía WebClient
  entry-points/
    reactive-web/                       → controladores WebFlux, manejo global de excepciones, OpenAPI

application/
  app-service/   → arranque de Spring Boot + wiring de beans de los use cases (config/UseCaseBeanConfig)
```

Regla de dependencia: `domain` no depende de nada externo; `infrastructure` depende de `domain`
(implementa sus ports); `application` ensambla todo y es el único módulo con `@SpringBootApplication`.

## Lo que es solo un ejemplo a reemplazar

`ThirdParty` / `IThirdPartyGateway` / `GetThirdPartyUseCase` / `ThirdPartyGatewayAdapter` /
`ThirdPartyController` son un flujo de punta a punta de muestra para que quede claro el patrón.
Bórralos cuando agregues los casos de uso reales (p. ej. un adapter que llame a tus proyectos de
Azure AI Document Intelligence):

1. Crea el modelo en `domain/model`.
2. Define el port (interfaz) en `domain/ports`.
3. Implementa el use case en `domain/usecases` (clase plana, sin anotaciones de Spring).
4. Crea el módulo adapter en `infrastructure/driven-adapters/rest-services/<nombre>` con su
   `WebClient`/SDK de Azure, anotado `@Component` para que Spring lo descubra.
5. Expón el endpoint en `infrastructure/entry-points/reactive-web`.
6. Registra el bean del use case en `application/app-service/.../config/UseCaseBeanConfig.java` y
   agrega el `implementation project(...)` del nuevo módulo en `application/app-service/build.gradle`
   y en `settings.gradle`.

## Configuración local

`application.yml` está en `.gitignore` (vas a meter ahí endpoints/keys reales, p. ej. de Azure
Document Intelligence). Antes de correr el servicio, cópialo desde la plantilla:

```bash
cp application/app-service/src/main/resources/application.yml.example \
   application/app-service/src/main/resources/application.yml
```

## Comandos

```bash
./gradlew build          # compila y corre tests de todos los módulos
./gradlew test           # solo tests
./gradlew :application:app-service:bootRun   # levanta el servicio en localhost:8080
```

## Build / calidad

- Gradle multi-módulo con catálogo de versiones (`gradle/libs.versions.toml`).
- JaCoCo habilitado (`./gradlew jacocoTestReport`) solo como reporte, sin gate mínimo de cobertura
  todavía — agrégalo en `main.gradle` cuando el proyecto tenga suficiente código real.
- Sin Sonar/OWASP Dependency-Check/PIT por ahora: agrégalos si tu pipeline de CI los necesita.

## Docker

`Dockerfile` genérico (Eclipse Temurin 21 Alpine) pensado para build manual o cualquier pipeline de
contenedores de Azure, sin placeholders de pipeline:

```bash
./gradlew :application:app-service:bootJar
docker build -t ms-tercero .
docker run -p 8080:8080 ms-tercero
```
