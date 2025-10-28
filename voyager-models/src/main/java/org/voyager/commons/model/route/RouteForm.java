package org.voyager.commons.model.route;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.commons.validate.annotations.ValidAirportCode;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class RouteForm {
    @ValidAirportCode
    String origin;

    @ValidAirportCode
    String destination;

    @NotNull
    @DecimalMin(value = "0.0")
    Double distanceKm;
}
