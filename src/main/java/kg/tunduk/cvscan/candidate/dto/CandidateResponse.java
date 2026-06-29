package kg.tunduk.cvscan.candidate.dto;

import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import kg.tunduk.cvscan.candidate.model.Verdict;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateResponse {
    String id;
    String name;
    String email;
    String phone;
    String position;
    String posLabel;
    String city;
    String telegram;
    String totalExp;
    String stack;
    String education;
    Verdict verdict;
    CandidateStatus status;
    String summary;
    List<CriteriaItemDto> criteria;
    List<ExperienceItemDto> experience;
    List<String> questions;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CriteriaItemDto {
        String key;
        String result;
        String comment;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExperienceItemDto {
        String period;
        String company;
        String title;
        String duration;
    }
}
