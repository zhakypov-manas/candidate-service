package kg.tunduk.cvscan.candidate.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CvParsedEvent {
    UUID eventId;
    String candidateId;
    OffsetDateTime parsedAt;
    String name;
    String position;
    String posLabel;
    String email;
    String phone;
    String city;
    String telegram;
    String totalExp;
    String stack;
    String education;
    String verdict;
    String summary;

    @Builder.Default
    List<CriteriaItem> criteria = new ArrayList<>();

    @Builder.Default
    List<ExperienceItem> experience = new ArrayList<>();

    @Builder.Default
    List<String> questions = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CriteriaItem {
        String key;
        String result;
        String comment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExperienceItem {
        String period;
        String company;
        String title;
        String duration;
    }
}
