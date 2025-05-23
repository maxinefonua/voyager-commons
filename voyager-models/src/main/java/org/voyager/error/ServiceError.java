package org.voyager.error;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter
public class ServiceError {
    private final HttpStatus httpStatus;
    private final Exception exception;
    private final String message;

    public ServiceError(@NonNull HttpStatus httpStatus,@NonNull Exception exception) {
        this.httpStatus = httpStatus;
        this.exception = exception;
        this.message = exception.getMessage();
    }

    public ServiceError(int statusCode, @NonNull Exception exception) {
        this.httpStatus = HttpStatus.getStatusFromCode(statusCode);
        this.exception = exception;
        this.message = exception.getMessage();
    }

    public ServiceError(@NonNull HttpStatus httpStatus, @NonNull String message, @NonNull Exception exception) {
        this.httpStatus = httpStatus;
        this.exception = exception;
        this.message = message;
    }
}
