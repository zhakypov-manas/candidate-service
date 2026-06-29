package kg.tunduk.cvscan.candidate.exception;

import org.slf4j.helpers.MessageFormatter;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String messagePattern, Object... messageParameters) {
        super(MessageFormatter.arrayFormat(messagePattern, messageParameters).getMessage());
    }
}
