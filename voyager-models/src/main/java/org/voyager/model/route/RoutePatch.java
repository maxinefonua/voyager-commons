package org.voyager.model.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.voyager.model.validate.ValidPatch;

@Builder
@Data @ValidPatch
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class RoutePatch {
    Boolean isActive;
}
