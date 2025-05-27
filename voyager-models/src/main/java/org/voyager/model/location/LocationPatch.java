package org.voyager.model.location;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder @ToString(includeFieldNames = false)
@AllArgsConstructor
public class LocationPatch {
    @NotNull  // can be empty
    Set<String> airports = new HashSet<>();
    Status status;
}
