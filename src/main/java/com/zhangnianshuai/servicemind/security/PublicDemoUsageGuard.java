package com.zhangnianshuai.servicemind.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PublicDemoUsageGuard {

    private final ConcurrentMap<String, WindowCounter> counters =
        new ConcurrentHashMap<>();

    private final int chatPerMinute;
    private final int platformPerDay;
    private final int byokChatPerMinute;
    private final int modelSessionCreatesPerHour;

    public PublicDemoUsageGuard(
            @Value("${app.public-demo.chat-per-minute:12}")
            int chatPerMinute,

            @Value("${app.public-demo.platform-chat-per-day:100}")
            int platformPerDay,

            @Value("${app.public-demo.byok-chat-per-minute:20}")
            int byokChatPerMinute,

            @Value("${app.public-demo.model-session-create-per-hour:5}")
            int modelSessionCreatesPerHour) {

        this.chatPerMinute = requirePositive(
            chatPerMinute,
            "chat-per-minute"
        );

        this.platformPerDay = requirePositive(
            platformPerDay,
            "platform-chat-per-day"
        );

        this.byokChatPerMinute = requirePositive(
            byokChatPerMinute,
            "byok-chat-per-minute"
        );

        this.modelSessionCreatesPerHour =
            requirePositive(
                modelSessionCreatesPerHour,
                "model-session-create-per-hour"
            );
    }

    public void checkPlatformChat(String username) {
        check(
            "platform-global",
            platformPerDay,
            Duration.ofHours(24)
        );

        check(
            "platform-user:" + normalize(username),
            chatPerMinute,
            Duration.ofMinutes(1)
        );
    }

    public void checkByokChat(String username) {
        check(
            "byok-chat:" + normalize(username),
            byokChatPerMinute,
            Duration.ofMinutes(1)
        );
    }

    public void checkModelSessionCreate(
            String username) {
        check(
            "model-session-create:" + normalize(username),
            modelSessionCreatesPerHour,
            Duration.ofHours(1)
        );
    }

    private void check(
            String key,
            int limit,
            Duration window) {

        Instant now = Instant.now();
        AtomicBoolean allowed =
            new AtomicBoolean(false);

        counters.compute(key, (ignored, current) -> {
            if (current == null
                    || !current.expiresAt().isAfter(now)) {

                allowed.set(true);

                return new WindowCounter(
                    1,
                    now.plus(window)
                );
            }

            if (current.count() >= limit) {
                return current;
            }

            allowed.set(true);

            return new WindowCounter(
                current.count() + 1,
                current.expiresAt()
            );
        });

        if (!allowed.get()) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Public demo request limit exceeded. Please try again later."
            );
        }
    }

    @Scheduled(
        fixedDelayString =
            "${app.public-demo.rate-limit-cleanup-ms:300000}"
    )
    public void removeExpiredCounters() {
        Instant now = Instant.now();

        counters.entrySet().removeIf(
            entry ->
                !entry.getValue()
                    .expiresAt()
                    .isAfter(now)
        );
    }

    private static String normalize(
            String username) {
        return username == null
            ? "unknown"
            : username.trim().toLowerCase();
    }

    private static int requirePositive(
            int value,
            String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                name + " must be greater than zero."
            );
        }

        return value;
    }

    private record WindowCounter(
        int count,
        Instant expiresAt
    ) {
    }
}