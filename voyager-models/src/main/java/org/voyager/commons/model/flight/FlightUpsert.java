package org.voyager.commons.model.flight;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidBoolean;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidFlightNumber;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data @NoArgsConstructor
@Builder @AllArgsConstructor
public class FlightUpsert {
    @NotNull
    @Pattern(regexp = "^[1-9]\\d*$", message = "must be a valid route id")
    String routeId;

    @ValidFlightNumber
    String flightNumber;

    @ValidEnum(enumClass = Airline.class)
    String airline;

    @NotEmpty
    List<ZonedDateTime> zonedDateTimeList;

    @ValidBoolean
    String isArrival;
}
