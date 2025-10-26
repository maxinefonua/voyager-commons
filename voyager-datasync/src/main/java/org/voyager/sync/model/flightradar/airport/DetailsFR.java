package org.voyager.sync.model.flightradar.airport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class DetailsFR {
    String name;
    CodeFR code;
    PositionPR position;
    TimezonePR timezone;
    boolean visible;
    Object website;
    StatsPR stats;
}
