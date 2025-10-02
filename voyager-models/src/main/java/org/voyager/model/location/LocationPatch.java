package org.voyager.model.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.voyager.model.validate.ValidEnum;
import org.voyager.model.validate.ValidPatch;

import java.util.List;

@Data @NoArgsConstructor
@Builder @ToString(includeFieldNames = false)
@AllArgsConstructor @ValidPatch
public class LocationPatch {
    List<String> airports;
    @ValidEnum(enumClass = Status.class)
    String status;
}
