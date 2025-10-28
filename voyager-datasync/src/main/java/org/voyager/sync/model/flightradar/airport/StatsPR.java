package org.voyager.sync.model.flightradar.airport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class StatsPR {
    @JsonProperty("arrivals")
    Map<String,Object> arrivalStats;
    @JsonProperty("departures")
    Map<String,Object> departureStats;
}
