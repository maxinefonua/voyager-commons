package org.voyager.model.airport;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.model.validate.ValidEnum;
import org.voyager.model.validate.ValidPatch;

@Builder @Data
@AllArgsConstructor
@NoArgsConstructor @ValidPatch
@ToString(includeFieldNames = false)
public class AirportPatch {
    private String name;
    private String city;
    private String subdivision;
    @ValidEnum(enumClass = AirportType.class)
    private String type;
}
