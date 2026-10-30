package co.edu.unbosque.tercero.infrastructure.adapters.postgres;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroDocument;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import co.edu.unbosque.tercero.infrastructure.adapters.postgres.entity.TerceroDocumentEntity;
import co.edu.unbosque.tercero.infrastructure.adapters.postgres.entity.TerceroRegistrationEntity;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Persists a {@link TerceroRegistration} in PostgreSQL via R2DBC, across two tables:
 * {@code tercero_registrations} (one row) and {@code tercero_documents} (one row per document,
 * linked by {@code tercero_registration_id}) — a real relation, not a JSON blob of URLs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresTerceroRegistrationAdapter implements ITerceroRegistrationRepository {

    private final TerceroRegistrationR2dbcRepository registrationRepository;
    private final TerceroDocumentR2dbcRepository documentRepository;

    @Override
    public Mono<TerceroRegistration> save(TerceroRegistration registration) {
        String registrationId = UUID.randomUUID().toString();
        log.info("Guardando registro de tercero '{}' (id={}) con {} documento(s)",
                registration.getNombre(), registrationId, registration.getDocuments().size());

        return registrationRepository.save(toRegistrationEntity(registration, registrationId))
                .doOnError(ex -> log.error("Fallo al guardar el registro (id={}) en tercero_registrations: {}",
                        registrationId, ex.getMessage(), ex))
                .flatMap(savedRegistration -> saveDocuments(registrationId, registration.getDocuments())
                        .collectList()
                        .map(documents -> toDomain(savedRegistration, documents)))
                .doOnSuccess(saved -> log.info("Registro '{}' (id={}) guardado correctamente", saved.getNombre(), saved.getId()));
    }

    private Flux<TerceroDocument> saveDocuments(String registrationId, List<TerceroDocument> documents) {
        return Flux.fromIterable(documents)
                .flatMap(document -> documentRepository.save(toDocumentEntity(registrationId, document))
                        .doOnError(ex -> log.error("Fallo al guardar el documento '{}' (registro id={}) en tercero_documents: {}",
                                document.getDocumentKey(), registrationId, ex.getMessage(), ex)))
                .map(this::toDocumentDomain);
    }

    private TerceroRegistrationEntity toRegistrationEntity(TerceroRegistration registration, String id) {
        return TerceroRegistrationEntity.builder()
                .id(id)
                .calidad(registration.getCalidad())
                .nacionalidad(registration.getNacionalidad())
                .tipoPersona(registration.getTipoPersona())
                .correo(registration.getCorreo())
                .nombre(registration.getNombre())
                .cedula(registration.getCedula())
                .fechaExpedicionCedula(registration.getFechaExpedicionCedula())
                .lugarNacimiento(registration.getLugarNacimiento())
                .paisNacimiento(registration.getPaisNacimiento())
                .nit(registration.getNit())
                .celular(registration.getCelular())
                .direccion(registration.getDireccion())
                .estado(registration.getEstado().name())
                .fechaRegistro(registration.getFechaRegistro())
                .build();
    }

    private TerceroDocumentEntity toDocumentEntity(String registrationId, TerceroDocument document) {
        return TerceroDocumentEntity.builder()
                .id(UUID.randomUUID().toString())
                .terceroRegistrationId(registrationId)
                .documentKey(document.getDocumentKey())
                .url(document.getUrl())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    private TerceroDocument toDocumentDomain(TerceroDocumentEntity entity) {
        return TerceroDocument.builder()
                .id(entity.getId())
                .terceroRegistrationId(entity.getTerceroRegistrationId())
                .documentKey(entity.getDocumentKey())
                .url(entity.getUrl())
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }

    private TerceroRegistration toDomain(TerceroRegistrationEntity entity, List<TerceroDocument> documents) {
        return TerceroRegistration.builder()
                .id(entity.getId())
                .calidad(entity.getCalidad())
                .nacionalidad(entity.getNacionalidad())
                .tipoPersona(entity.getTipoPersona())
                .correo(entity.getCorreo())
                .nombre(entity.getNombre())
                .cedula(entity.getCedula())
                .fechaExpedicionCedula(entity.getFechaExpedicionCedula())
                .lugarNacimiento(entity.getLugarNacimiento())
                .paisNacimiento(entity.getPaisNacimiento())
                .nit(entity.getNit())
                .celular(entity.getCelular())
                .direccion(entity.getDireccion())
                .documents(documents)
                .estado(RegistrationStatus.valueOf(entity.getEstado()))
                .fechaRegistro(entity.getFechaRegistro())
                .build();
    }
}
