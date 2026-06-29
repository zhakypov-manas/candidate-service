package kg.tunduk.cvscan.candidate.integration;

import kg.tunduk.cvscan.candidate.dto.CandidateWriteRequest;
import kg.tunduk.cvscan.candidate.dto.StatusChangeRequest;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import kg.tunduk.cvscan.candidate.model.Verdict;
import kg.tunduk.cvscan.candidate.repository.CandidateRepository;
import kg.tunduk.cvscan.candidate.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CandidateApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @BeforeEach
    void setUp() {
        statusHistoryRepository.deleteAll();
        candidateRepository.deleteAll();
    }

    private CandidateWriteRequest buildRequest(String name, String email) {
        return CandidateWriteRequest.builder()
                .name(name)
                .email(email)
                .position("java-middle")
                .verdict(Verdict.FIT)
                .criteria(List.of())
                .experience(List.of())
                .questions(List.of())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/candidates создаёт кандидата и возвращает 201")
    void createCandidate_returns201() {
        CandidateWriteRequest req = buildRequest("Иванов Иван Иванович", "ivanov-test@email.com");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/candidates", req, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody().get("status")).isEqualTo("NEW");
    }

    @Test
    @DisplayName("POST /api/v1/candidates с дублирующимся email возвращает 409")
    void createCandidate_duplicateEmail_returns409() {
        CandidateWriteRequest req = buildRequest("Иванов Иван", "dup@email.com");
        restTemplate.postForEntity("/api/v1/candidates", req, Map.class);

        CandidateWriteRequest req2 = buildRequest("Другой Иван", "dup@email.com");
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/candidates", req2, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("DUPLICATE_EMAIL");
    }

    @Test
    @DisplayName("GET /api/v1/candidates с фильтром по verdict возвращает правильные результаты")
    void listCandidates_filterByVerdict() {
        restTemplate.postForEntity("/api/v1/candidates", buildRequest("Кандидат FIT", "fit@email.com"), Map.class);
        CandidateWriteRequest partial = buildRequest("Кандидат PARTIAL", "partial@email.com");
        partial.setVerdict(Verdict.PARTIAL);
        restTemplate.postForEntity("/api/v1/candidates", partial, Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/candidates?verdict=FIT", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> content = (List<?>) response.getBody().get("content");
        assertThat(content).hasSize(1);
        Map<?, ?> candidate = (Map<?, ?>) content.get(0);
        assertThat(candidate.get("verdict")).isEqualTo("FIT");
    }

    @Test
    @DisplayName("GET /api/v1/candidates с поиском по имени работает")
    void listCandidates_searchByName() {
        restTemplate.postForEntity("/api/v1/candidates", buildRequest("Петров Пётр", "petrov@email.com"), Map.class);
        restTemplate.postForEntity("/api/v1/candidates", buildRequest("Сидоров Сидор", "sidorov@email.com"), Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/candidates?search=Петров", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> content = (List<?>) response.getBody().get("content");
        assertThat(content).hasSize(1);
    }

    @Test
    @DisplayName("PATCH /api/v1/candidates/{id}/status — допустимый переход сохраняет историю")
    void changeStatus_validTransition_savesHistory() {
        ResponseEntity<Map> created = restTemplate.postForEntity(
                "/api/v1/candidates", buildRequest("Иванов И.И.", "history@email.com"), Map.class);
        String id = (String) created.getBody().get("id");

        StatusChangeRequest req = StatusChangeRequest.builder()
                .status(CandidateStatus.IN_REVIEW)
                .comment("На проверку")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/candidates/" + id + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(req, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("IN_REVIEW");

        ResponseEntity<List> history = restTemplate.getForEntity(
                "/api/v1/candidates/" + id + "/status-history", List.class);
        assertThat(history.getBody()).hasSize(1);
        Map<?, ?> entry = (Map<?, ?>) history.getBody().get(0);
        assertThat(entry.get("fromStatus")).isEqualTo("NEW");
        assertThat(entry.get("toStatus")).isEqualTo("IN_REVIEW");
    }

    @Test
    @DisplayName("PATCH /api/v1/candidates/{id}/status — недопустимый переход возвращает 422")
    void changeStatus_invalidTransition_returns422() {
        ResponseEntity<Map> created = restTemplate.postForEntity(
                "/api/v1/candidates", buildRequest("Тест Тестов", "invalid@email.com"), Map.class);
        String id = (String) created.getBody().get("id");

        StatusChangeRequest req = StatusChangeRequest.builder()
                .status(CandidateStatus.APPROVED)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/candidates/" + id + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(req, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("error")).isEqualTo("INVALID_STATUS_TRANSITION");
    }

    @Test
    @DisplayName("GET /api/v1/candidates/{id} несуществующего кандидата возвращает 404")
    void getCandidate_notFound_returns404() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/candidates/non-existing-id", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error")).isEqualTo("CANDIDATE_NOT_FOUND");
    }

    @Test
    @DisplayName("DELETE /api/v1/candidates/{id} возвращает 204")
    void deleteCandidate_returns204() {
        ResponseEntity<Map> created = restTemplate.postForEntity(
                "/api/v1/candidates", buildRequest("Удаляемый Кандидат", "delete@email.com"), Map.class);
        String id = (String) created.getBody().get("id");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/candidates/" + id, HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("PUT /api/v1/candidates/{id} не меняет статус")
    void updateCandidate_doesNotChangeStatus() {
        ResponseEntity<Map> created = restTemplate.postForEntity(
                "/api/v1/candidates", buildRequest("Обновляемый", "update@email.com"), Map.class);
        String id = (String) created.getBody().get("id");

        // Change status to IN_REVIEW
        StatusChangeRequest statusReq = StatusChangeRequest.builder().status(CandidateStatus.IN_REVIEW).build();
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange("/api/v1/candidates/" + id + "/status", HttpMethod.PATCH,
                new HttpEntity<>(statusReq, h), Map.class);

        // Now PUT - should not reset status
        CandidateWriteRequest updateReq = buildRequest("Обновлённое Имя", "update@email.com");
        ResponseEntity<Map> updated = restTemplate.exchange(
                "/api/v1/candidates/" + id, HttpMethod.PUT,
                new HttpEntity<>(updateReq, h), Map.class);

        assertThat(updated.getBody().get("status")).isEqualTo("IN_REVIEW");
        assertThat(updated.getBody().get("name")).isEqualTo("Обновлённое Имя");
    }
}
