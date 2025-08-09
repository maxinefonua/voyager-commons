package org.voyager.model.currency;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(includeFieldNames = false)
public class CurrencyNames {
    private Map<String, String> currencies = new HashMap<>();

    @JsonAnyGetter
    public Map<String, String> getMappedNames() {
        return currencies;
    }

    @JsonAnySetter
    public void setCurrency(String code, String name) {
        currencies.put(code, name);
    }
}
