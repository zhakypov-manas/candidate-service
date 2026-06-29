package kg.tunduk.cvscan.candidate.service;

import kg.tunduk.cvscan.candidate.dto.*;
import kg.tunduk.cvscan.candidate.dto.event.CvParsedEvent;
import kg.tunduk.cvscan.candidate.exception.CandidateNotFoundException;
import kg.tunduk.cvscan.candidate.exception.DuplicateEmailException;
import kg.tunduk.cvscan.candidate.messaging.StatusChangedProducer;
import kg.tunduk.cvscan.candidate.model.Candidate;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import kg.tunduk.cvscan.candidate.model.StatusHistory;
import kg.tunduk.cvscan.candidate.model.Verdict;
import kg.tunduk.cvscan.candidate.repository.CandidateRepository;
import kg.tunduk.cvscan.candidate.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final StatusService statusService;
    private final CandidateMapper mapper;
    private final StatusChangedProducer statusChangedProducer;

    @Transactional(readOnly = true)
    public CandidatePage list(Verdict verdict,
                              CandidateStatus status,
                              String position,
                              String search,
                              int page, int size,
                              String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        Page<Candidate> result = candidateRepository.findWithFilters(verdict, status, position, search, pageable);

        return CandidatePage.builder()
                .content(result.getContent().stream().map(mapper::toResponse).collect(Collectors.toList()))
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public CandidateResponse getById(String id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public CandidateResponse create(CandidateWriteRequest req) {
        if (candidateRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateEmailException(req.getEmail());
        }
        String id = generateId(req.getName());
        Candidate candidate = mapper.fromRequest(id, req);
        candidate = candidateRepository.save(candidate);
        log.info("[{}] :: Created candidate id", candidate.getId());
        return mapper.toResponse(candidate);
    }

    @Transactional
    public CandidateResponse update(String id, CandidateWriteRequest req) {
        Candidate candidate = findOrThrow(id);
        if (candidateRepository.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new DuplicateEmailException(req.getEmail());
        }
        CandidateStatus currentStatus = candidate.getStatus();
        mapper.updateFromRequest(candidate, req);
        // PUT does not change status
        candidate.setStatus(currentStatus);
        candidate = candidateRepository.save(candidate);
        log.info("[{}] :: Updated candidate id", id);
        return mapper.toResponse(candidate);
    }

    @Transactional
    public void delete(String id) {
        if (!candidateRepository.existsById(id)) {
            throw new CandidateNotFoundException(id);
        }
        candidateRepository.deleteById(id);
        log.info("[{}] :: Deleted candidate id", id);
    }

    @Transactional
    public CandidateResponse changeStatus(String id, StatusChangeRequest req) {
        Candidate candidate = findOrThrow(id);
        CandidateStatus from = candidate.getStatus();
        CandidateStatus to = req.getStatus();

        statusService.validateTransition(from, to);

        candidate.setStatus(to);
        candidate = candidateRepository.save(candidate);

        StatusHistory history = StatusHistory.builder()
                .id(UUID.randomUUID())
                .candidateId(id)
                .fromStatus(from)
                .toStatus(to)
                .comment(req.getComment())
                .changedAt(OffsetDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        statusChangedProducer.publish(id, from, to, history.getChangedAt());

        log.info("[{}] :: Status changed candidate from={} to={}", id, from, to);
        return mapper.toResponse(candidate);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryEntry> getStatusHistory(String id) {
        if (!candidateRepository.existsById(id)) {
            throw new CandidateNotFoundException(id);
        }
        return statusHistoryRepository.findByCandidateIdOrderByChangedAtDesc(id)
                .stream()
                .map(mapper::toHistoryEntry)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createFromEvent(CvParsedEvent event) {
        if (candidateRepository.findByIdAndParsedAt(event.getCandidateId(), event.getParsedAt()).isPresent()) {
            log.info("[candidateId: {}, parsedAt: {}] :: Duplicate cv.parsed event ignored", event.getCandidateId(), event.getParsedAt());
            return;
        }
        if (candidateRepository.existsById(event.getCandidateId())) {
            log.warn("[{}] :: Candidate by id already exists, skipping event", event.getCandidateId());
            return;
        }
        Candidate candidate = mapper.fromEvent(event);
        candidateRepository.save(candidate);
        log.info("[{}] :: Created candidate from Kafka event", event.getCandidateId());
    }

    private Candidate findOrThrow(String id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException("[{}] :: Candidate not found by id", id));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    private String generateId(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-zа-я0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-");
        if (base.isBlank()) {
            return "candidate-" + UUID.randomUUID().toString().substring(0, 8);
        }

        String transliterated = transliterate(base);
        String candidate = transliterated.length() > 50 ? transliterated.substring(0, 50) : transliterated;
        if (candidateRepository.existsById(candidate)) {
            candidate = candidate + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return candidate;
    }

    private String transliterate(String input) {
        String[] ru = {"а","б","в","г","д","е","ё","ж","з","и","й","к","л","м","н","о","п","р","с","т","у","ф","х","ц","ч","ш","щ","ъ","ы","ь","э","ю","я"};
        String[] en = {"a","b","v","g","d","e","yo","zh","z","i","y","k","l","m","n","o","p","r","s","t","u","f","h","ts","ch","sh","sch","","y","","e","yu","ya"};
        for (int i = 0; i < ru.length; i++) {
            input = input.replace(ru[i], en[i]);
        }
        return input.replaceAll("[^a-z0-9-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
