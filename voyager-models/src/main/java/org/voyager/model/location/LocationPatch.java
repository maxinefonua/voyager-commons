package org.voyager.model.location;

import lombok.*;
import org.voyager.model.validate.ValidPatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data @NoArgsConstructor
@Builder @ToString(includeFieldNames = false)
@AllArgsConstructor @ValidPatch
public class LocationPatch {
    List<String> airports;
    Status status;
}
