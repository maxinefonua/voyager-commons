package org.voyager.sync.model.flightradar.search;

import lombok.Data;

@Data
public class FlightTimeFR {
    String aircraft;
    String time;
    Long timestamp;
    Integer offset;
}
