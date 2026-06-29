package kg.tunduk.cvscan.candidate.service;

import kg.tunduk.cvscan.candidate.dto.CandidateResponse;
import kg.tunduk.cvscan.candidate.dto.CandidateWriteRequest;
import kg.tunduk.cvscan.candidate.dto.StatusHistoryEntry;
import kg.tunduk.cvscan.candidate.dto.event.CvParsedEvent;
import kg.tunduk.cvscan.candidate.model.Candidate;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import kg.tunduk.cvscan.candidate.model.StatusHistory;
import kg.tunduk.cvscan.candidate.model.Verdict;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CandidateMapper {

    public CandidateResponse toResponse(Candidate c) {
        return CandidateResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .position(c.getPosition())
                .posLabel(c.getPosLabel())
                .city(c.getCity())
                .telegram(c.getTelegram())
                .totalExp(c.getTotalExp())
                .stack(c.getStack())
                .education(c.getEducation())
                .verdict(c.getVerdict())
                .status(c.getStatus())
                .summary(c.getSummary())
                .criteria(mapCriteria(c.getCriteria()))
                .experience(mapExperience(c.getExperience()))
                .questions(c.getQuestions())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    public void updateFromRequest(Candidate candidate, CandidateWriteRequest req) {
        candidate.setName(req.getName());
        candidate.setEmail(req.getEmail());
        candidate.setPhone(req.getPhone());
        candidate.setPosition(req.getPosition());
        candidate.setPosLabel(req.getPosLabel());
        candidate.setCity(req.getCity());
        candidate.setTelegram(req.getTelegram());
        candidate.setTotalExp(req.getTotalExp());
        candidate.setStack(req.getStack());
        candidate.setEducation(req.getEducation());
        candidate.setVerdict(req.getVerdict());
        candidate.setSummary(req.getSummary());
        candidate.setCriteria(mapCriteriaFromDto(req.getCriteria()));
        candidate.setExperience(mapExperienceFromDto(req.getExperience()));
        candidate.setQuestions(req.getQuestions());
    }

    public Candidate fromRequest(String id, CandidateWriteRequest req) {
        Candidate candidate = Candidate.builder()
                .id(id)
                .status(CandidateStatus.NEW)
                .build();
        updateFromRequest(candidate, req);
        return candidate;
    }

    public Candidate fromEvent(CvParsedEvent event) {
        return Candidate.builder()
                .id(event.getCandidateId())
                .name(event.getName())
                .email(event.getEmail())
                .phone(event.getPhone())
                .position(event.getPosition())
                .posLabel(event.getPosLabel())
                .city(event.getCity())
                .telegram(event.getTelegram())
                .totalExp(event.getTotalExp())
                .stack(event.getStack())
                .education(event.getEducation())
                .verdict(Verdict.valueOf(event.getVerdict()))
                .status(CandidateStatus.NEW)
                .summary(event.getSummary())
                .criteria(mapCriteriaFromEvent(event.getCriteria()))
                .experience(mapExperienceFromEvent(event.getExperience()))
                .questions(event.getQuestions())
                .parsedAt(event.getParsedAt())
                .build();
    }

    public StatusHistoryEntry toHistoryEntry(StatusHistory h) {
        return StatusHistoryEntry.builder()
                .id(h.getId())
                .candidateId(h.getCandidateId())
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .comment(h.getComment())
                .changedAt(h.getChangedAt())
                .build();
    }

    private List<CandidateResponse.CriteriaItemDto> mapCriteria(List<Candidate.CriteriaItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> CandidateResponse.CriteriaItemDto.builder()
                        .key(i.getKey()).result(i.getResult()).comment(i.getComment()).build())
                .collect(Collectors.toList());
    }

    private List<CandidateResponse.ExperienceItemDto> mapExperience(List<Candidate.ExperienceItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> CandidateResponse.ExperienceItemDto.builder()
                        .period(i.getPeriod()).company(i.getCompany()).title(i.getTitle()).duration(i.getDuration()).build())
                .collect(Collectors.toList());
    }

    private List<Candidate.CriteriaItem> mapCriteriaFromDto(List<CandidateWriteRequest.CriteriaItemDto> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> new Candidate.CriteriaItem(i.getKey(), i.getResult(), i.getComment()))
                .collect(Collectors.toList());
    }

    private List<Candidate.ExperienceItem> mapExperienceFromDto(List<CandidateWriteRequest.ExperienceItemDto> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> new Candidate.ExperienceItem(i.getPeriod(), i.getCompany(), i.getTitle(), i.getDuration()))
                .collect(Collectors.toList());
    }

    private List<Candidate.CriteriaItem> mapCriteriaFromEvent(List<CvParsedEvent.CriteriaItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> new Candidate.CriteriaItem(i.getKey(), i.getResult(), i.getComment()))
                .collect(Collectors.toList());
    }

    private List<Candidate.ExperienceItem> mapExperienceFromEvent(List<CvParsedEvent.ExperienceItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> new Candidate.ExperienceItem(i.getPeriod(), i.getCompany(), i.getTitle(), i.getDuration()))
                .collect(Collectors.toList());
    }
}
