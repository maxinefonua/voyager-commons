package org.voyager.model.result;

import lombok.*;

import java.time.ZoneId;

@Builder
@Getter @AllArgsConstructor
@Setter
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class ResultSearchFull {
    ResultSearch resultSearch;
    Double[] bbox;
    ZoneId zoneId;
}
