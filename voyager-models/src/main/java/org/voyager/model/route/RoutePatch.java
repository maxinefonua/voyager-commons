package org.voyager.model.route;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.voyager.model.validate.ValidPatch;

@Data
@NoArgsConstructor
@Builder
@ToString(includeFieldNames = false)
@AllArgsConstructor
@ValidPatch
public class RoutePatch {
    @DecimalMin(value = "0.0")
    Double distanceKm;
}
