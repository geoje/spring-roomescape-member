package roomescape.service.exception;

import roomescape.exception.BadRequestException;

public class PreviousTimeException extends BadRequestException {

    public PreviousTimeException(final String message) {
        super(message);
    }
}
