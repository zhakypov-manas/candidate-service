package kg.tunduk.cvscan.candidate.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidate_status_history")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatusHistory {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    UUID id;

    @Column(name = "candidate_id", nullable = false, length = 100)
    String candidateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, length = 20)
    CandidateStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    CandidateStatus toStatus;

    @Column(name = "comment", columnDefinition = "TEXT")
    String comment;

    @Column(name = "changed_at", nullable = false)
    OffsetDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (changedAt == null) {
            changedAt = OffsetDateTime.now();
        }
    }
}
