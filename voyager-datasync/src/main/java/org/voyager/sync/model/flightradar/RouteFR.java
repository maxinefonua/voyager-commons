package org.voyager.sync.model.flightradar;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @NoArgsConstructor
@ToString
public class RouteFR {
    AirportFR airport1;
    AirportFR airport2;
}
