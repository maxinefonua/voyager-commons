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
    private Integer id;
    private String flightNumber;
    private Integer routeId;
    private ZonedDateTime zonedDateTimeDeparture;
    private ZonedDateTime zonedDateTimeArrival;
    private Boolean isActive;
    private Airline airline;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Duration duration;
}
