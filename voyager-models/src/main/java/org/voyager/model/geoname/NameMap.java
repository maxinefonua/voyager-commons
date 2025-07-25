package org.voyager.model.geoname;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NameMap {
    Boolean isPreferredName;
    Boolean isShortName;
    Boolean isColloquial;
    Boolean isHistoric;

    @JsonProperty("from")
    String fromYear;

    @JsonProperty("to")
    String toYear;

    String name;
    String lang;
}
