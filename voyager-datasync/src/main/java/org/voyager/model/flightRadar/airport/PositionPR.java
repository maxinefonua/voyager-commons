package org.voyager.model.flightRadar.airport;

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
public class PositionPR {
    Double latitude;
    Double longitude;
    Integer altitude;
    @JsonProperty("country")
    Map<String,Object> countryMap;
    RegionFR region;
}
