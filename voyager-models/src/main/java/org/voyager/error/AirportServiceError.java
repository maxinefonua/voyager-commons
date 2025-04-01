package org.voyager.error;

public class AirportServiceError extends ServiceError {
    public AirportServiceError(HttpStatus httpStatus) {
        super(httpStatus);
    }
}
