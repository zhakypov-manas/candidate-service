package kg.tunduk.cvscan.candidate.service;

import kg.tunduk.cvscan.candidate.exception.InvalidStatusTransitionException;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class StatusServiceTest {

    private final StatusService statusService = new StatusService();

    @ParameterizedTest(name = "{0} → {1} должен быть разрешён")
    @CsvSource({
            "NEW, IN_REVIEW",
            "IN_REVIEW, INVITED",
            "IN_REVIEW, REJECTED",
            "INVITED, APPROVED",
            "INVITED, REJECTED"
    })
    @DisplayName("Допустимые переходы не бросают исключение")
    void allowedTransitions(CandidateStatus from, CandidateStatus to) {
        assertThatCode(() -> statusService.validateTransition(from, to))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} → {1} должен быть запрещён")
    @CsvSource({
            "NEW, INVITED",
            "NEW, APPROVED",
            "NEW, REJECTED",
            "NEW, NEW",
            "IN_REVIEW, NEW",
            "IN_REVIEW, APPROVED",
            "IN_REVIEW, IN_REVIEW",
            "INVITED, NEW",
            "INVITED, IN_REVIEW",
            "INVITED, INVITED",
            "APPROVED, NEW",
            "APPROVED, IN_REVIEW",
            "APPROVED, INVITED",
            "APPROVED, REJECTED",
            "APPROVED, APPROVED",
            "REJECTED, NEW",
            "REJECTED, IN_REVIEW",
            "REJECTED, INVITED",
            "REJECTED, APPROVED",
            "REJECTED, REJECTED"
    })
    @DisplayName("Недопустимые переходы бросают InvalidStatusTransitionException")
    void disallowedTransitions(CandidateStatus from, CandidateStatus to) {
        assertThatThrownBy(() -> statusService.validateTransition(from, to))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining(from.name())
                .hasMessageContaining(to.name());
    }

    @Test
    @DisplayName("isTransitionAllowed возвращает true для NEW → IN_REVIEW")
    void isTransitionAllowed_true() {
        assertThatCode(() -> {
            boolean result = statusService.isTransitionAllowed(CandidateStatus.NEW, CandidateStatus.IN_REVIEW);
            assert result;
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("isTransitionAllowed возвращает false для NEW → REJECTED")
    void isTransitionAllowed_false() {
        assertThatCode(() -> {
            boolean result = statusService.isTransitionAllowed(CandidateStatus.NEW, CandidateStatus.REJECTED);
            assert !result;
        }).doesNotThrowAnyException();
    }
}
