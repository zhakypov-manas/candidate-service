package kg.tunduk.cvscan.candidate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.tunduk.cvscan.candidate.dto.event.CvParsedEvent;
import kg.tunduk.cvscan.candidate.model.Candidate;
import kg.tunduk.cvscan.candidate.repository.CandidateRepository;
import kg.tunduk.cvscan.candidate.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topics.cv-parsed}")
    private String cvParsedTopic;

    @BeforeEach
    void setUp() {
        statusHistoryRepository.deleteAll();
        candidateRepository.deleteAll();
    }

    private CvParsedEvent buildEvent(String candidateId, OffsetDateTime parsedAt) {
        return CvParsedEvent.builder()
                .eventId(UUID.randomUUID())
                .candidateId(candidateId)
                .parsedAt(parsedAt)
                .name("Иванов Иван Иванович")
                .position("java-middle")
                .posLabel("Java — ведущий программист")
                .email(candidateId + "@email.com")
                .phone("+996 700 123456")
                .verdict("FIT")
                .summary("Хороший кандидат")
                .criteria(List.of())
                .experience(List.of())
                .questions(List.of())
                .build();
    }

    @Test
    @DisplayName("Сообщение в cv.parsed создаёт кандидата в БД")
    void consumeCvParsed_createsCandidate() throws Exception {
        OffsetDateTime parsedAt = OffsetDateTime.now();
        CvParsedEvent event = buildEvent("kafka-test-001", parsedAt);
        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(cvParsedTopic, "kafka-test-001", payload);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<Candidate> candidate = candidateRepository.findById("kafka-test-001");
                    assertThat(candidate).isPresent();
                    assertThat(candidate.get().getName()).isEqualTo("Иванов Иван Иванович");
                    assertThat(candidate.get().getStatus().name()).isEqualTo("NEW");
                });
    }

    @Test
    @DisplayName("Повторное сообщение в cv.parsed не создаёт дубль")
    void consumeCvParsed_duplicateEvent_noDuplicate() throws Exception {
        OffsetDateTime parsedAt = OffsetDateTime.now();
        CvParsedEvent event = buildEvent("kafka-dup-001", parsedAt);
        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(cvParsedTopic, "kafka-dup-001", payload);

        // Wait for first to be created
        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> candidateRepository.existsById("kafka-dup-001"));

        // Send duplicate
        kafkaTemplate.send(cvParsedTopic, "kafka-dup-001", payload);

        // Give consumer time to process second message
        TimeUnit.SECONDS.sleep(3);

        long count = candidateRepository.count();
        assertThat(count).isEqualTo(1);
    }
}
