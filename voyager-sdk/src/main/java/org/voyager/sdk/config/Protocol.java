package org.voyager.sdk.config;

import lombok.Getter;

@Getter
public enum Protocol {
    HTTPS("https"),
    HTTP("http");
    private final String value;
    Protocol(String value) {
        this.value = value;
    }
}
