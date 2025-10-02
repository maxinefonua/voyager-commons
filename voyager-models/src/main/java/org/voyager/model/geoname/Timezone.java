package org.voyager.model.geoname;

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
public class Timezone {
    private String sunrise;
    private Double lng;
    private String countryCode;
    private Integer gmtOffset;
    private Integer rawOffset;
    private String sunset;
    private String timezoneId;
    private Integer dstOffset;
    private String countryName;
    private String time;
    private Double lat;
}
