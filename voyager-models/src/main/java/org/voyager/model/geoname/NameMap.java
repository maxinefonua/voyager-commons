package org.voyager.model.geoname;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NameMap {
    private Boolean isPreferredName;
    private Boolean isShortName;
    private Boolean isColloquial;
    private Boolean isHistoric;

    @JsonProperty("from")
    private String fromYear;

    @JsonProperty("to")
    private String toYear;

    private String name;
    private String lang;
}
