package org.voyager.model.currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static org.voyager.utils.ConstantsUtils.ALPHA3_CODE_REGEX;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class CurrencyForm {
    @NotBlank
    @Pattern(regexp = ALPHA3_CODE_REGEX, message = "must be a valid three-letter currency code")
    String code;
    @NotBlank
    String name;
    @NotBlank
    String symbol;
    @DecimalMin(value = "0.0")
    Double usdRate;
    @NonNull
    Boolean isActive;
}
