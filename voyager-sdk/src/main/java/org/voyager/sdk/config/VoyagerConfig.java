package org.voyager.sdk.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import java.net.MalformedURLException;
import java.net.URL;

public class VoyagerConfig {
    @Getter
    private final String baseURL;
    @Getter
    private final String authorizationToken;
    @Getter @Setter
    private Boolean testMode = false;

    public VoyagerConfig(@NonNull Protocol protocol, @NonNull String host, int port, @NonNull String authorizationToken) {
        this.baseURL = buildBaseURL(protocol.getValue(),host,port);
        this.authorizationToken = authorizationToken;
    }

    public VoyagerConfig(@NonNull Protocol protocol, @NonNull String host, @NonNull String authorizationToken) {
        this.baseURL = buildBaseURL(protocol.getValue(),host,-1);
        this.authorizationToken = authorizationToken;
    }

    public VoyagerConfig(@NonNull Protocol protocol, @NonNull String host, @NonNull String authorizationToken,boolean testMode) {
        this.baseURL = buildBaseURL(protocol.getValue(),host,-1);
        this.authorizationToken = authorizationToken;
        this.testMode = testMode;
    }


    private String buildBaseURL(String protocol, String host, int port) {
        try {
            return new URL(protocol,host,port,"").toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
