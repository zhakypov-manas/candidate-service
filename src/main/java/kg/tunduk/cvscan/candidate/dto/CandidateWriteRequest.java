package kg.tunduk.cvscan.candidate.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kg.tunduk.cvscan.candidate.model.Verdict;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateWriteRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    String name;

    @NotBlank
    @Email
    String email;

    @Pattern(regexp = "^\\+\\d[\\d ]{6,20}$")
    String phone;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]+$")
    String position;

    String posLabel;

    String city;

    String telegram;

    String totalExp;

    String stack;

    String education;

    @NotNull
    Verdict verdict;

    String summary;

    @Valid
    @Builder.Default
    List<CriteriaItemDto> criteria = new ArrayList<>();

    @Valid
    @Builder.Default
    List<ExperienceItemDto> experience = new ArrayList<>();

    @Builder.Default
    List<String> questions = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CriteriaItemDto {
        @NotBlank
        String key;
        @NotBlank
        String result;
        @NotBlank
        String comment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExperienceItemDto {
        @NotBlank
        String period;
        @NotBlank
        String company;
        @NotBlank
        String title;
        @NotBlank
        String duration;
    }
}
