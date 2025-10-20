package org.voyager.model.flightRadar.airport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class TimezonePR {
    @JsonProperty("name")
    String zoneId;
    Long offset;
    String offsetHours;
    String abbr;
    String abbrName;
    Boolean isDst;
}
