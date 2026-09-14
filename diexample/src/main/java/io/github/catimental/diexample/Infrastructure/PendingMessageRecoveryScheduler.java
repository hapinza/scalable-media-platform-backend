package io.github.catimental.diexample.Infrastructure;

import io.github.catimental.diexample.Consumer.Redis.MovieViewedConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessage;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingMessageRecoveryScheduler {

    private final StringRedisTemplate redisTemplate;
    private final MovieViewedConsumer movieViewedConsumer;

    private static final String STREAM = "stream:movie:viewed";
    private static final String GROUP = "movie-analytics-group";
    private static final String RECOVERY_CONSUMER = "recovery-consumer";

    @Scheduled(fixedDelay = 5000)
    public void recoverPendingMessages() {
        try {
            PendingMessagesSummary summary = redisTemplate.opsForStream().pending(STREAM, GROUP);

            if (summary == null || summary.getTotalPendingMessages() == 0) {
                return;
            }

            PendingMessages pendingMessages = redisTemplate.opsForStream().pending(
                STREAM,
               // Consumer.from(GROUP, RECOVERY_CONSUMER),
               GROUP,
                Range.unbounded(),
                10L
            );

            if (pendingMessages == null || pendingMessages.isEmpty()) {
                return;
            }

            for (PendingMessage pendingMessage : pendingMessages) {
                if (pendingMessage.getElapsedTimeSinceLastDelivery().compareTo(Duration.ofSeconds(10)) < 0) {
                    continue;
                }

                RecordId recordId = pendingMessage.getId();

                List<MapRecord<String, Object, Object>> reclaimedMessages =
                    redisTemplate.opsForStream().claim(
                        STREAM,
                        GROUP,
                        RECOVERY_CONSUMER,
                        Duration.ofSeconds(10),
                        recordId
                    );

                if (reclaimedMessages == null || reclaimedMessages.isEmpty()) {
                    continue;
                }

                for (MapRecord<String, Object, Object> msg : reclaimedMessages) {
                    log.info("Reclaimed pending message. recordId={}", msg.getId());
                    movieViewedConsumer.processMessage(msg);
                }
            }

        } catch (Exception e) {
            log.error("Failed to recover pending messages", e);
        }
    }
}