package org.voyager.model.airport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder @Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirportForm {
    String name;
    String city;
    String subdivision;
    String countryCode;
    Double latitude;
    Double longitude;
    AirportType type;
}
