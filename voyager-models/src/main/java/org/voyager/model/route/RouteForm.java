package org.voyager.model.route;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.model.Airline;
import org.voyager.model.validate.ValidEnum;

import java.util.ArrayList;
import java.util.List;

import static org.voyager.utils.ConstantsUtils.IATA_CODE_REGEX;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class RouteForm {
    @NotBlank
    @Pattern(regexp = IATA_CODE_REGEX, message = "must be a valid three-letter IATA airport code")
    String origin;

    @NotBlank
    @Pattern(regexp = IATA_CODE_REGEX, message = "must be a valid three-letter IATA airport code")
    String destination;

    @NotNull
    @Builder.Default
    List<Integer> flightIds = new ArrayList<>();
}
