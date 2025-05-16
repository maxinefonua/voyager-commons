package org.voyager.model.delta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static org.voyager.utils.ConstantsUtils.IATA_CODE_REGEX;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class DeltaForm {
    @NotBlank
    @Pattern(regexp = IATA_CODE_REGEX, message = "must be a valid three-letter IATA airport code")
    String iata;

    @NotBlank
    String status = DeltaStatus.ACTIVE.name();

    @NotNull
    Boolean isHub = Boolean.FALSE;
}
