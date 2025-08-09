package org.voyager.model.currency;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.validate.ValidPatch;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidPatch
public class CurrencyPatch {
    @DecimalMin(value = "0.0")
    Double usdRate;
    Boolean isActive;
    String symbol;
}
