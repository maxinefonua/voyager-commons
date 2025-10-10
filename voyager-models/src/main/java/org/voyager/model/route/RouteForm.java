package org.voyager.model.route;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.utils.Constants;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class RouteForm {
    @NotBlank
    @Pattern(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3, message = Constants.Voyager.ConstraintMessage.IATA_CODE)
    String origin;

    @NotBlank
    @Pattern(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3, message = "must be a valid three-letter IATA airport code")
    String destination;

    @NotNull
    @DecimalMin(value = "0.0")
    Double distanceKm;
}
