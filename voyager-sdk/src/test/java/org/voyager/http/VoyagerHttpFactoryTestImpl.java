package org.voyager.http;

public class VoyagerHttpFactoryTestImpl extends VoyagerHttpFactory {
    protected static VoyagerHttpClient createClient() {
        return new VoyagerHttpTestClient();
    }
}
