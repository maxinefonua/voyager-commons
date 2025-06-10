package org.voyager.model.location;

import lombok.*;
import org.voyager.model.validate.ValidEnum;
import org.voyager.model.validate.ValidPatch;

import java.util.*;

@Data @NoArgsConstructor
@Builder @ToString(includeFieldNames = false)
@AllArgsConstructor @ValidPatch
public class LocationPatch {
    List<String> airports;
    @ValidEnum(enumClass = Status.class)
    String status;
}
