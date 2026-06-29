package kg.tunduk.cvscan.candidate.exception;

import org.slf4j.helpers.MessageFormatter;

public class CandidateNotFoundException extends RuntimeException {
    public CandidateNotFoundException(String messagePattern, Object... messageParameters) {
        super(MessageFormatter.arrayFormat(messagePattern, messageParameters).getMessage());
    }
}
