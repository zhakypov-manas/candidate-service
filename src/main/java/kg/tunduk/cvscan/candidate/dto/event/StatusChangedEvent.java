package kg.tunduk.cvscan.candidate.dto.event;

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
public class StatusChangedEvent {
    UUID eventId;
    String candidateId;
    CandidateStatus fromStatus;
    CandidateStatus toStatus;
    OffsetDateTime changedAt;
}
