package org.voyager.error;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class ServiceError {
    private final HttpStatus httpStatus;

    public ServiceError(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
