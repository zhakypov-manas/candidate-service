package kg.tunduk.cvscan.candidate.service;

import kg.tunduk.cvscan.candidate.exception.InvalidStatusTransitionException;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class StatusService {

    private static final Map<CandidateStatus, Set<CandidateStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(CandidateStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CandidateStatus.NEW, EnumSet.of(CandidateStatus.IN_REVIEW));
        ALLOWED_TRANSITIONS.put(CandidateStatus.IN_REVIEW, EnumSet.of(CandidateStatus.INVITED, CandidateStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(CandidateStatus.INVITED, EnumSet.of(CandidateStatus.APPROVED, CandidateStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(CandidateStatus.APPROVED, EnumSet.noneOf(CandidateStatus.class));
        ALLOWED_TRANSITIONS.put(CandidateStatus.REJECTED, EnumSet.noneOf(CandidateStatus.class));
    }

    public void validateTransition(CandidateStatus from, CandidateStatus to) {
        Set<CandidateStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(CandidateStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException("Invalid transition status - from: {}, to: {}",from, to);
        }
    }

    public boolean isTransitionAllowed(CandidateStatus from, CandidateStatus to) {
        Set<CandidateStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(CandidateStatus.class));
        return allowed.contains(to);
    }
}
