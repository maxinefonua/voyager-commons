package org.voyager.model.flights;

import lombok.Getter;
import org.voyager.error.ServiceError;

import java.util.List;

public class FlightSyncTasks {
    @Getter
    public static class TaskResult {
        String origin;
        String destination;
        int flightsProcessed;
        List<String> flightNumberErrors;

        public TaskResult(String origin, String destination, Integer flightsProcessed, List<String> flightNumberErrors) {
            this.origin = origin;
            this.destination = destination;
            this.flightsProcessed = flightsProcessed;
            this.flightNumberErrors = flightNumberErrors;
        }
    }
    @Getter
    public static class TaskFailure {
        String origin;
        String destination;
        ServiceError serviceError;

        public TaskFailure(String origin, String destination, ServiceError serviceError) {
            this.origin = origin;
            this.destination = destination;
            this.serviceError = serviceError;
        }
    }
}
