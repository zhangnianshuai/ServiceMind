package com.zhangnianshuai.servicemind.application.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.domain.model.ModelProvider;

@Component
public class ModelSessionStore {

    private final ConcurrentMap<UUID, StoredSession> sessions =
        new ConcurrentHashMap<>();

    private final Duration sessionTtl;
    private final int maxActivePerUser;

    public ModelSessionStore(
            @Value("${app.model-session.ttl-minutes:30}")
            long ttlMinutes,

            @Value("${app.model-session.max-active-per-user:2}")
            int maxActivePerUser) {

        if (ttlMinutes <= 0) {
            throw new IllegalArgumentException(
                "Model session TTL must be greater than zero."
            );
        }

        if (maxActivePerUser <= 0) {
            throw new IllegalArgumentException(
                "Maximum active model sessions must be greater than zero."
            );
        }

        this.sessionTtl =
            Duration.ofMinutes(ttlMinutes);

        this.maxActivePerUser =
            maxActivePerUser;
    }

    public synchronized Registration create(
            String ownerUsername,
            ModelProvider provider,
            String apiKey,
            String model) {

        String owner =
            normalizeOwner(ownerUsername);

        removeExpiredSessions();

        long activeCount =
            sessions.values()
                .stream()
                .filter(
                    session ->
                        session.ownerUsername()
                            .equals(owner)
                )
                .count();

        if (activeCount >= maxActivePerUser) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many active model sessions."
            );
        }

        UUID sessionId =
            UUID.randomUUID();

        Instant expiresAt =
            Instant.now()
                .plus(sessionTtl);

        ModelSessionCredential credential =
            new ModelSessionCredential(
                provider,
                apiKey,
                model
            );

        sessions.put(
            sessionId,
            new StoredSession(
                owner,
                credential,
                expiresAt
            )
        );

        return new Registration(
            sessionId,
            expiresAt
        );
    }

    public ModelSessionCredential getRequired(
            UUID sessionId,
            String ownerUsername) {

        String owner =
            normalizeOwner(ownerUsername);

        Instant now =
            Instant.now();

        AtomicReference<StoredSession> result =
            new AtomicReference<>();

        sessions.compute(
            sessionId,
            (id, current) -> {

                if (current == null) {
                    return null;
                }

                if (!current.ownerUsername()
                        .equals(owner)) {
                    return current;
                }

                if (!current.expiresAt()
                        .isAfter(now)) {

                    current.credential()
                        .destroy();

                    return null;
                }

                StoredSession refreshed =
                    new StoredSession(
                        current.ownerUsername(),
                        current.credential(),
                        now.plus(sessionTtl)
                    );

                result.set(refreshed);

                return refreshed;
            }
        );

        StoredSession storedSession =
            result.get();

        if (storedSession == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Model session does not exist or has expired."
            );
        }

        return storedSession.credential();
    }

    public void revoke(
            UUID sessionId,
            String ownerUsername) {

        String owner =
            normalizeOwner(ownerUsername);

        sessions.computeIfPresent(
            sessionId,
            (id, current) -> {

                if (!current.ownerUsername()
                        .equals(owner)) {
                    return current;
                }

                current.credential()
                    .destroy();

                return null;
            }
        );
    }

    @Scheduled(
        fixedDelayString =
            "${app.model-session.cleanup-delay-ms:60000}"
    )
    public void removeExpiredSessions() {

        Instant now =
            Instant.now();

        sessions.forEach(
            (sessionId, storedSession) -> {

                if (!storedSession
                        .expiresAt()
                        .isAfter(now)
                        && sessions.remove(
                            sessionId,
                            storedSession
                        )) {

                    storedSession
                        .credential()
                        .destroy();
                }
            }
        );
    }

    private static String normalizeOwner(
            String username) {

        if (username == null
                || username.isBlank()) {
            throw new IllegalArgumentException(
                "Model session owner is required."
            );
        }

        return username
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    public record Registration(
        UUID sessionId,
        Instant expiresAt
    ) {
    }

    private record StoredSession(
        String ownerUsername,
        ModelSessionCredential credential,
        Instant expiresAt
    ) {
    }
}