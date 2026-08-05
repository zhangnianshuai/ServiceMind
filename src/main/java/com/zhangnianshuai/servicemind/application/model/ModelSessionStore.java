package com.zhangnianshuai.servicemind.application.model;

import java.time.Duration;
import java.time.Instant;
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

    public ModelSessionStore(
            @Value("${app.model-session.ttl-minutes:30}")
            long ttlMinutes) {
        if (ttlMinutes <= 0) {
            throw new IllegalArgumentException(
                "Model session TTL must be greater than zero."
            );
        }

        this.sessionTtl = Duration.ofMinutes(ttlMinutes);
    }

    public Registration create(
            ModelProvider provider,
            String apiKey,
            String model) {
        UUID sessionId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(sessionTtl);

        ModelSessionCredential credential =
            new ModelSessionCredential(provider, apiKey, model);

        sessions.put(
            sessionId,
            new StoredSession(credential, expiresAt)
        );

        return new Registration(sessionId, expiresAt);
    }

    public ModelSessionCredential getRequired(UUID sessionId) {
        Instant now = Instant.now();
        AtomicReference<StoredSession> result =
            new AtomicReference<>();

        sessions.compute(sessionId, (id, current) -> {
            if (current == null) {
                return null;
            }

            if (!current.expiresAt().isAfter(now)) {
                current.credential().destroy();
                return null;
            }

            StoredSession refreshed = new StoredSession(
                current.credential(),
                now.plus(sessionTtl)
            );

            result.set(refreshed);
            return refreshed;
        });

        StoredSession storedSession = result.get();

        if (storedSession == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Model session does not exist or has expired."
            );
        }

        return storedSession.credential();
    }

    public void revoke(UUID sessionId) {
        StoredSession removed = sessions.remove(sessionId);

        if (removed != null) {
            removed.credential().destroy();
        }
    }

    @Scheduled(
        fixedDelayString =
            "${app.model-session.cleanup-delay-ms:60000}"
    )
    public void removeExpiredSessions() {
        Instant now = Instant.now();

        sessions.forEach((sessionId, storedSession) -> {
            if (!storedSession.expiresAt().isAfter(now)
                    && sessions.remove(sessionId, storedSession)) {
                storedSession.credential().destroy();
            }
        });
    }

    public record Registration(
        UUID sessionId,
        Instant expiresAt
    ) {
    }

    private record StoredSession(
        ModelSessionCredential credential,
        Instant expiresAt
    ) {
    }
}