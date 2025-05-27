package org.voyager.model.airport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.model.validate.ValidPatch;

@Builder @Data
@AllArgsConstructor
@NoArgsConstructor @ValidPatch
@ToString(includeFieldNames = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirportPatch {
    String name;
    String city;
    String subdivision;
    Double latitude;
    Double longitude;
    String type;
}
