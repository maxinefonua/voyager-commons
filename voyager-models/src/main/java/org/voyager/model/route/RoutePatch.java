package org.voyager.model.route;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.voyager.model.validate.ValidPatch;

import java.util.List;

@Builder
@Data @ValidPatch
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class RoutePatch {
    List<Integer> flightIds;
}
