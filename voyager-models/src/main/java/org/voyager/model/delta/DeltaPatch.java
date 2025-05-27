package org.voyager.model.delta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.voyager.model.validate.ValidPatch;

@Builder @Data
@NoArgsConstructor @ValidPatch
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class DeltaPatch {
    String status;
    Boolean isHub;
}
