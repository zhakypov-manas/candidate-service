package kg.tunduk.cvscan.candidate.service;

import kg.tunduk.cvscan.candidate.dto.event.CvParsedEvent;
import kg.tunduk.cvscan.candidate.messaging.StatusChangedProducer;
import kg.tunduk.cvscan.candidate.model.Candidate;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import kg.tunduk.cvscan.candidate.model.Verdict;
import kg.tunduk.cvscan.candidate.repository.CandidateRepository;
import kg.tunduk.cvscan.candidate.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CvParsedConsumerTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private StatusService statusService;

    @Mock
    private StatusChangedProducer statusChangedProducer;

    @InjectMocks
    private CandidateService candidateService;

    private CandidateMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CandidateMapper();
        candidateService = new CandidateService(
                candidateRepository,
                statusHistoryRepository,
                statusService,
                mapper,
                statusChangedProducer
        );
    }

    private CvParsedEvent buildEvent(String candidateId, OffsetDateTime parsedAt) {
        return CvParsedEvent.builder()
                .eventId(UUID.randomUUID())
                .candidateId(candidateId)
                .parsedAt(parsedAt)
                .name("Иванов Иван Иванович")
                .position("java-middle")
                .email("ivanov@email.com")
                .verdict("FIT")
                .criteria(List.of())
                .experience(List.of())
                .questions(List.of())
                .build();
    }

    @Test
    @DisplayName("Новое событие cv.parsed создаёт кандидата")
    void createFromEvent_newEvent_createsCandidante() {
        OffsetDateTime parsedAt = OffsetDateTime.now();
        CvParsedEvent event = buildEvent("ivanov", parsedAt);

        when(candidateRepository.findByIdAndParsedAt("ivanov", parsedAt)).thenReturn(Optional.empty());
        when(candidateRepository.existsById("ivanov")).thenReturn(false);
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(inv -> inv.getArgument(0));

        candidateService.createFromEvent(event);

        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Повторное событие с тем же candidateId + parsedAt игнорируется (нет дубля)")
    void createFromEvent_duplicateEvent_isIgnored() {
        OffsetDateTime parsedAt = OffsetDateTime.now();
        CvParsedEvent event = buildEvent("ivanov", parsedAt);

        Candidate existing = Candidate.builder()
                .id("ivanov")
                .name("Иванов Иван Иванович")
                .email("ivanov@email.com")
                .position("java-middle")
                .verdict(Verdict.FIT)
                .status(CandidateStatus.NEW)
                .parsedAt(parsedAt)
                .criteria(List.of())
                .experience(List.of())
                .questions(List.of())
                .build();

        when(candidateRepository.findByIdAndParsedAt("ivanov", parsedAt)).thenReturn(Optional.of(existing));

        candidateService.createFromEvent(event);

        verify(candidateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Событие с тем же candidateId но другим parsedAt тоже игнорируется (кандидат уже существует)")
    void createFromEvent_sameIdDifferentParsedAt_isIgnored() {
        OffsetDateTime parsedAt = OffsetDateTime.now();
        OffsetDateTime newParsedAt = parsedAt.plusHours(1);
        CvParsedEvent event = buildEvent("ivanov", newParsedAt);

        when(candidateRepository.findByIdAndParsedAt("ivanov", newParsedAt)).thenReturn(Optional.empty());
        when(candidateRepository.existsById("ivanov")).thenReturn(true);

        candidateService.createFromEvent(event);

        verify(candidateRepository, never()).save(any());
    }
}
