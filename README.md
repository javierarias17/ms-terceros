# ms-tercero

Microservicio en **arquitectura hexagonal** (ports & adapters) que valida los documentos
(cédula, RUT) que un tercero carga en el formulario de `frontend/universidad-boques-terceros`,
usando tres modelos personalizados de Azure AI Document Intelligence: un clasificador de
documentos y dos extractores de campos (uno por tipo de documento).

Dos validaciones por documento:
1. **Tipo de documento**: el clasificador confirma que el PDF cargado sea realmente del tipo
   esperado (p. ej. que lo subido como "cédula" sea una cédula y no un RUT).
2. **Campos del documento**: si el tipo coincide, el extractor correspondiente lee sus campos y
   se comparan contra lo que la persona escribió en el formulario.

Pensado para desplegarse en Azure fuera de AKS (p. ej. Azure Container Apps o App Service for
Containers), por eso **no incluye** contratos OpenAPI contract-first con codegen, Helm charts ni
pipelines de CI/CD — se agregan según el mecanismo de despliegue que elijas.

## Estructura

```
domain/
  model/      → DocumentType, ApplicantType, DocumentValidationRequest/Result, FieldComparison
  ports/      → IDocumentClassifierGateway, IDocumentExtractorGateway
  usecases/   → ValidateDocumentUseCase: clasifica, compara tipo, extrae y compara campos

infrastructure/
  driven-adapters/
    rest-services/azure-document-intelligence-client/   → llama al clasificador y a los 2
                                                            extractores de Azure Document Intelligence
                                                            (maneja el polling asíncrono de su API)
  entry-points/
    reactive-web/   → POST /v1/document-validations (multipart), manejo global de excepciones, OpenAPI

application/
  app-service/   → arranque de Spring Boot + wiring de beans (config/UseCaseBeanConfig,
                    config/RutValidationProperties)
```

Regla de dependencia: `domain` no depende de nada externo; `infrastructure` depende de `domain`
(implementa sus ports); `application` ensambla todo y es el único módulo con `@SpringBootApplication`.

## Endpoint

`POST /v1/document-validations` — multipart/form-data:

| Parte | Obligatorio | Descripción |
|---|---|---|
| `file` | sí | el PDF a validar |
| `expectedDocumentType` | sí | `CEDULA` o `RUT` |
| `numeroDocumento` | solo para `CEDULA` | número de cédula escrito en el formulario |
| `nombreCompleto` | solo para `CEDULA` | nombre completo escrito en el formulario |
| `applicantType` | solo para `RUT` | `NATURAL` o `JURIDICA` (según el campo "Persona" del formulario) |

Respuesta:
```json
{
  "documentType": { "expected": "CEDULA", "detected": "CEDULA", "match": true },
  "fields": [
    { "field": "numeroDocumento", "formValue": "123456789", "documentValue": "123456789", "match": true },
    { "field": "nombreCompleto", "formValue": "Ana Gomez", "documentValue": "ANA GOMEZ", "match": true }
  ],
  "valid": true
}
```

Campos que compara cada tipo de documento (ver `ValidateDocumentUseCase`):
- **CEDULA**: `cedula_numero` contra `numeroDocumento`; `cedula_nombres` + `cedula_apellidos`
  (comparación por conjunto de palabras, sin tildes ni mayúsculas) contra `nombreCompleto`.
- **RUT**: `tipo_contribuyente` contra el texto esperado según `applicantType`
  (`rut-validation.tipo-contribuyente-natural` / `tipo-contribuyente-juridica` en `application.yml`).

Umbral de confianza (`azure.document-intelligence.min-confidence`, por defecto `0.85`): si la
clasificación o un campo extraído viene con menos confianza que eso, se descarta — la clasificación
pasa a `UNKNOWN` (fuerza rechazo en la validación 1) y el campo queda como no extraído (fuerza
rechazo en la validación 2), en vez de confiar en un resultado dudoso de Azure.

## Configuración local

`application.yml` está en `.gitignore` porque ahí van las credenciales reales de Azure. Antes de
correr el servicio, cópialo desde la plantilla y completa los valores:

```bash
cp application/app-service/src/main/resources/application.yml.example \
   application/app-service/src/main/resources/application.yml
```

Lo que necesitas del recurso de Azure AI Document Intelligence (Portal → tu recurso →
**"Keys and Endpoint"**, y Document Intelligence Studio para los nombres de los modelos):

| Propiedad | De dónde sale |
|---|---|
| `azure.document-intelligence.endpoint` | Endpoint del recurso |
| `azure.document-intelligence.api-key` | Una de las dos keys del recurso |
| `azure.document-intelligence.classifier.model-id` | Nombre del modelo clasificador en el Studio |
| `azure.document-intelligence.classifier.label-cedula` / `label-rut` | Etiquetas que devuelve el clasificador para cada tipo |
| `azure.document-intelligence.models.cedula-model-id` / `rut-model-id` | Nombre de cada modelo extractor en el Studio |

## Frontend

`frontend/universidad-boques-terceros/terceros.html` ya llama a este endpoint para los documentos
de cédula y RUT (`tipoDocumentoConModelo` en su script). El resto de documentos del catálogo
(cámara de comercio, certificación bancaria, etc.) sigue mostrándose como **"Simulado"** hasta que
existan sus modelos. Si el backend corre en otro host/puerto, ajusta `API_BASE_URL` en ese mismo
script.

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
