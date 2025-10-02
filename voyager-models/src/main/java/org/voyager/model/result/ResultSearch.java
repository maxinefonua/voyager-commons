package org.voyager.model.result;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;

@Builder
@Getter @AllArgsConstructor
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
    String type;
}
