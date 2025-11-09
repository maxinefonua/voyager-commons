package org.voyager.commons.model.route;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.commons.validate.annotations.ValidNonNullField;

@Data
@NoArgsConstructor
@Builder
@ToString(includeFieldNames = false)
@AllArgsConstructor
@ValidNonNullField
public class RoutePatch {
    @DecimalMin(value = "0.0")
    Double distanceKm;
}
