package kg.tunduk.cvscan.candidate.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.tunduk.cvscan.candidate.dto.event.CvParsedEvent;
import kg.tunduk.cvscan.candidate.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CvParsedConsumer {

    private final CandidateService candidateService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.cv-parsed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Received cv.parsed message: offset={} key={}", record.offset(), record.key());
        try {
            CvParsedEvent event = objectMapper.readValue(record.value(), CvParsedEvent.class);
            candidateService.createFromEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process cv.parsed message offset={}: {}", record.offset(), e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
