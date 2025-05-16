package org.voyager.model.delta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class DeltaPatch {
    @NotBlank
    String status;

    @NotNull
    Boolean isHub;
}
