package kg.tunduk.cvscan.candidate.dto;

import jakarta.validation.constraints.NotNull;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatusChangeRequest {

    @NotNull
    CandidateStatus status;
    String comment;
}
