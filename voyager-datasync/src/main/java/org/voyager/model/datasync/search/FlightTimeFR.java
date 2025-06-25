package org.voyager.model.datasync.search;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class FlightTimeFR {
    String aircraft;
    String time;
    Long timestamp;
    Long offset;
}
