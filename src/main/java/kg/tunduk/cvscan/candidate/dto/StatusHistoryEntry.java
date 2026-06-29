package kg.tunduk.cvscan.candidate.dto;

import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatusHistoryEntry {
    UUID id;
    String candidateId;
    CandidateStatus fromStatus;
    CandidateStatus toStatus;
    String comment;
    OffsetDateTime changedAt;
}
