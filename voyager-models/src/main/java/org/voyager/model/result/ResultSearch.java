package org.voyager.model.result;

import lombok.*;
import org.voyager.utils.MapperUtils;

@Builder @Getter @AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class ResultSearch {
    String source;
    String sourceId;
    String name;
    String subdivision;
    String countryCode;
    String countryName;
    Double latitude;
    Double longitude;
//    [west, south, east, north]
    Double[] bounds;
    String type;
}
