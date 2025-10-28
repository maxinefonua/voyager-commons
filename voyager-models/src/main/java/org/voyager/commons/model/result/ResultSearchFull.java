package org.voyager.commons.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.ZoneId;

@Builder
@Getter @AllArgsConstructor
@Setter
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class ResultSearchFull {
    ResultSearch resultSearch;

    @SuppressWarnings("SpellCheckingInspection")
    Double[] bbox;
    ZoneId zoneId;
}
