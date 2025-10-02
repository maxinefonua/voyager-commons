package org.voyager.model.route;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
