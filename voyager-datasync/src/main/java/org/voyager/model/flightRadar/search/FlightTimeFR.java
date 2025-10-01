package org.voyager.model.flightRadar.search;

import lombok.Data;

@Data
public class FlightTimeFR {
    String aircraft;
    String time;
    Long timestamp;
    Long offset;
}
