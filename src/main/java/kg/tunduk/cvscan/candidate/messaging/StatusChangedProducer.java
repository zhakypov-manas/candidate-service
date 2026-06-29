package kg.tunduk.cvscan.candidate.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.tunduk.cvscan.candidate.dto.event.StatusChangedEvent;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusChangedProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.status-changed}")
    private String topic;

    public void publish(String candidateId, CandidateStatus from, CandidateStatus to, OffsetDateTime changedAt) {
        StatusChangedEvent event = StatusChangedEvent.builder()
                .eventId(UUID.randomUUID())
                .candidateId(candidateId)
                .fromStatus(from)
                .toStatus(to)
                .changedAt(changedAt)
                .build();

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, candidateId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish status.changed for candidateId={}: {}", candidateId, ex.getMessage());
                        } else {
                            log.info("Published status.changed for candidateId={} offset={}",
                                    candidateId, result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Error serializing StatusChangedEvent for candidateId={}", candidateId, e);
        }
    }
}
