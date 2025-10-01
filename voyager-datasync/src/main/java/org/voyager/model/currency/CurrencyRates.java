package org.voyager.model.currency;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(includeFieldNames = false)
public class CurrencyRates {
    String disclaimer;
    String license;
    Long timestamp;
    @JsonProperty("base")
    String baseCurrency;
    @JsonProperty("rates")
    Map<String,Double> mappedRates;
}
