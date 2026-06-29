package kg.tunduk.cvscan.candidate.repository;

import kg.tunduk.cvscan.candidate.model.Candidate;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import kg.tunduk.cvscan.candidate.model.Verdict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, String>, JpaSpecificationExecutor<Candidate> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    Optional<Candidate> findByIdAndParsedAt(String id, OffsetDateTime parsedAt);

    @Query("""
            SELECT c FROM Candidate c
            WHERE (:verdict IS NULL OR c.verdict = :verdict)
              AND (:status IS NULL OR c.status = :status)
              AND (:position IS NULL OR c.position = :position)
              AND (CAST(:search AS string) IS NULL OR c.name ILIKE CONCAT('%', CAST(:search AS string), '%'))
            """)
    Page<Candidate> findWithFilters(
            @Param("verdict") Verdict verdict,
            @Param("status") CandidateStatus status,
            @Param("position") String position,
            @Param("search") String search,
            Pageable pageable
    );
}
