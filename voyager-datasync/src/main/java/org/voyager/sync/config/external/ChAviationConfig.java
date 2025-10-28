package org.voyager.sync.config.external;

import lombok.Getter;

@Getter
public class ChAviationConfig {
    private final String baseURL = "https://www.ch-aviation.com";
    private final String airportsPath = baseURL.concat("/airports");
}
