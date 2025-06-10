package org.voyager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.service.AirportService;
import org.voyager.service.Voyager;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountriesSync {
    private static Voyager voyager;
    private static AirportService airportService;
    private static ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from airports sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        int processLimit = datasyncProgramArguments.getProcessLimit();

        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);

        executorService = Executors.newFixedThreadPool(maxConcurrentRequests);
        voyager = new Voyager(voyagerConfig);

        // load countries from files/fetch from urls
        // load countries from API
        // patch any discrepancies
        // POST any new countries
    }

}
