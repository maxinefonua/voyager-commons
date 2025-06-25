package org.voyager.model.datasync;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.voyager.model.Airline;

@Data
@Builder @ToString
public class FlightFR {
    String origin;
    String destination;
    String flightNumber;
    Airline airline;
    String arrivalDate;
    String departureDate;
    String departureTime;
    String arrivalTime;
}