package org.voyager.model.geoname;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class TimezoneGN {
    @JsonProperty("timeZoneId")
    ZoneId zoneId;

    Integer gmtOffset;
    Integer dstOffset;

    public void setZoneId(String timezoneId) {
        if (StringUtils.isNotBlank(timezoneId)) this.zoneId = ZoneId.of(timezoneId);
    }
}
