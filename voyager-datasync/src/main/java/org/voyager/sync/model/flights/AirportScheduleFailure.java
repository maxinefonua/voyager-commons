package org.voyager.sync.model.flights;

import org.voyager.commons.error.ServiceError;

public class AirportScheduleFailure {
    public String airportCode1;
    public String airportCode2;
    public ServiceError serviceError;

    public AirportScheduleFailure(String airportCode1, String airportCode2, ServiceError serviceError) {
        this.airportCode1 = airportCode1;
        this.airportCode2 = airportCode2;
        this.serviceError = serviceError;
    }
}
