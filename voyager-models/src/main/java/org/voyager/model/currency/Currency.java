package org.voyager.model.currency;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class Currency {
    String code;
    String name;
    String symbol;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Double usdRate;
    Boolean isActive;
}
