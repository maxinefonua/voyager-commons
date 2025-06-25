package org.voyager.model.flight;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.Airline;
import java.time.ZonedDateTime;
import java.time.Duration;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
public class Flight {
    Integer id;
    String flightNumber;
    Integer routeId;
    ZonedDateTime zonedDateTimeDeparture;
    ZonedDateTime zonedDateTimeArrival;
    Boolean isActive;
    Airline airline;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Duration duration;
}
