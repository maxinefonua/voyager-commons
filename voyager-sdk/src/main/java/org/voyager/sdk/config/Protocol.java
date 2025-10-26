package org.voyager.sdk.config;

public enum Protocol {
    HTTP("http");
    private String value;
    Protocol(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
}
