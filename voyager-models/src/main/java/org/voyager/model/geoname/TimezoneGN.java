package org.voyager.model.geoname;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZoneId;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class TimezoneGN {
    @JsonProperty("timeZoneId")
    private ZoneId zoneId;

    private Integer gmtOffset;
    private Integer dstOffset;
}
