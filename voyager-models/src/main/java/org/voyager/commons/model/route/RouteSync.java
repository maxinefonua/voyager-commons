package org.voyager.commons.model.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RouteSync {
    private Integer id;
    private Status status;
    private String error;
    private int attempts;
    private Map<String,Object> metadata;
    private ZonedDateTime lastProcessed;
    private ZonedDateTime created;
    private ZonedDateTime updated;
}