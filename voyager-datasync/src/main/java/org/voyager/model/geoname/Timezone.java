package org.voyager.model.geoname;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Timezone {
    String sunrise;
    Double lng;
    String countryCode;
    Integer gmtOffset;
    Integer rawOffset;
    String sunset;
    String timezoneId;
    Integer dstOffset;
    String countryName;
    String time;
    Double lat;
}
