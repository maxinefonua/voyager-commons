package org.voyager.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.utils.MapperUtils;

import java.util.List;

@Builder @Getter @AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class ResultSearch {
    Source source;
    String sourceId;
    @Setter
    Status status;
    String name;
    String subdivision;
    String countryCode;
    String countryName;
    Double latitude;
    Double longitude;
    Double[] bounds;
    String type;
}
