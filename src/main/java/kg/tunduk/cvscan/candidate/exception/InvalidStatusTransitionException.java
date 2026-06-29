package kg.tunduk.cvscan.candidate.exception;

import org.slf4j.helpers.MessageFormatter;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String messagePattern, Object... messageParameters) {
        super(MessageFormatter.arrayFormat(messagePattern, messageParameters).getMessage());
    }
}
