package kg.tunduk.cvscan.candidate.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidates")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Candidate {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    String id;

    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Column(name = "phone")
    String phone;

    @Column(name = "position", nullable = false, length = 100)
    String position;

    @Column(name = "pos_label")
    String posLabel;

    @Column(name = "city")
    String city;

    @Column(name = "telegram")
    String telegram;

    @Column(name = "total_exp")
    String totalExp;

    @Column(name = "stack", columnDefinition = "TEXT")
    String stack;

    @Column(name = "education", columnDefinition = "TEXT")
    String education;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = false, length = 20)
    Verdict verdict;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    CandidateStatus status;

    @Column(name = "summary", columnDefinition = "TEXT")
    String summary;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria", columnDefinition = "jsonb")
    List<CriteriaItem> criteria = new ArrayList<>();

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experience", columnDefinition = "jsonb")
    List<ExperienceItem> experience = new ArrayList<>();

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", columnDefinition = "jsonb")
    List<String> questions = new ArrayList<>();

    @Column(name = "parsed_at")
    OffsetDateTime parsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PACKAGE)
    public static class CriteriaItem {
        String key;
        String result;
        String comment;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PACKAGE)
    public static class ExperienceItem {
        String period;
        String company;
        String title;
        String duration;
    }
}
