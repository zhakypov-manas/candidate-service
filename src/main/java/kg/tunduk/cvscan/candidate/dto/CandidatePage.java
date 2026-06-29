package kg.tunduk.cvscan.candidate.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidatePage {
    List<CandidateResponse> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
}
