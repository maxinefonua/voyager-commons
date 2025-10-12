package org.voyager.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.config.*;
import org.voyager.model.Airline;
import org.voyager.model.airport.AirportType;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class DatasyncProgramArgumentsTest {
    private static final String HOST = "TEST_HOST";
    private static final String VALID_HOST_ARG = String.format("%s=%s",DatasyncConfig.Flag.HOSTNAME,HOST);
    private static final String ACCESS_TOKEN = "TEST_ACCESS";
    private static final String VALID_ACCESS_TOKEN_ARG = String.format("%s=%s", DatasyncConfig.Flag.AUTH_TOKEN,ACCESS_TOKEN);

    @Test
    @DisplayName("valid constructor")
    void constructor() {
        int expectedThreadCount = 45;
        String tcArg = String.format("%s=%d",DatasyncConfig.Flag.THREAD_COUNT,expectedThreadCount);

        String expectedHost = "localtest";
        String hArg = String.format("%s=%s",DatasyncConfig.Flag.HOSTNAME,expectedHost);

        int expectedPort = 1000;
        String pArg = String.format("%s=%d",DatasyncConfig.Flag.PORT,expectedPort);

        int expectedLimit = 5000;
//        String lArg = String.format("%s=%d",DatasyncConfig.Flag.PROCESS_L,expectedLimit);

        String expectedToken = "accessToken";
        String atArg = String.format("%s=%s", DatasyncConfig.Flag.AUTH_TOKEN,expectedToken);

        Airline expectedAirline = Airline.DELTA;
        String aArg = String.format("%s=%s", AirlineSyncConfig.Flag.AIRLINE,"delta");

        String tpArg = String.format("%s=%s,%s",AirportsSyncConfig.Flag.AIRPORT_TYPES,AirportType.OTHER,AirportType.UNVERIFIED);

        AirportsSyncConfig airportsSyncConfig = new AirportsSyncConfig(new String[]{
                tcArg,hArg,pArg,atArg,aArg,tpArg});

        assertEquals(expectedThreadCount,airportsSyncConfig.getThreadCount());
        assertEquals(expectedHost,airportsSyncConfig.getHostname());
        assertEquals(expectedPort,airportsSyncConfig.getPort());
        assertEquals(expectedToken,airportsSyncConfig.getAccessToken());
    }

    @Test
    @DisplayName("default constructor")
    void constructorDefault() {
        CountriesSyncConfig datasynced = new CountriesSyncConfig(new String[]{
                VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG});
        assertEquals(DatasyncConfig.Defaults.THREAD_COUNT,datasynced.getThreadCount());
        assertEquals(DatasyncConfig.Defaults.PORT,datasynced.getPort());
    }

    @Test
    @DisplayName("ignore non-flag argument")
    void ignoreNonFlag() {
        String nonFlagArg = "nonFlagArg";
        CountriesSyncConfig datasynced = new CountriesSyncConfig(new String[]{
                nonFlagArg,VALID_ACCESS_TOKEN_ARG,VALID_HOST_ARG});
    }

    @Test
    @DisplayName("missing access token on default")
    void constructorDefaultMissingAccess() {
        assertThrows(RuntimeException.class,() -> new CountriesSyncConfig(new String[]{
                VALID_HOST_ARG
        }));
        try {
            new CountriesSyncConfig(new String[]{VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(DatasyncConfig.Flag.AUTH_TOKEN));
        }
    }

    @Test
    @DisplayName("missing host on default")
    void constructorDefaultMissingHost() {
        assertThrows(RuntimeException.class,() -> new CountriesSyncConfig(new String[]{
                VALID_ACCESS_TOKEN_ARG
        }));
        try {
            new CountriesSyncConfig(new String[]{VALID_ACCESS_TOKEN_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(DatasyncConfig.Flag.HOSTNAME));
        }
    }

    @Test
    @DisplayName("missing airline on default")
    void constructorDefaultMissingAirline() {
        assertThrows(RuntimeException.class,() -> new FlightSyncConfig(new String[]{
                VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG
        }));
        try {
            new FlightSyncConfig(new String[]{VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(AirlineSyncConfig.Flag.AIRLINE));
        }
    }


    @Test
    @DisplayName("missing airport types on default")
    void constructorDefaultMissingAirportTypes() {
        assertThrows(RuntimeException.class,() -> new AirportsSyncConfig(new String[]{
                VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG
        }));
        try {
            new AirportsSyncConfig(new String[]{VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(AirportsSyncConfig.Flag.AIRPORT_TYPES));
        }
    }

    @Test
    @DisplayName("unrecognized flag")
    void unrecognizedFlaggedArg() {
        String unrecognized = "-unknown=flag";
        assertDoesNotThrow(() -> new CountriesSyncConfig(new String[]{
                VALID_ACCESS_TOKEN_ARG,unrecognized,VALID_HOST_ARG
        }));
    }

    @Test
    @DisplayName("invalid to default thread counts")
    void extractThreadCountInvalid() {
        int invalid = -20;
        int expected = 1;
        String tcArg = String.format("%s=%d",DatasyncConfig.Flag.THREAD_COUNT,invalid);
        CountriesSyncConfig datasynced = new CountriesSyncConfig(new String[]{
                tcArg,VALID_ACCESS_TOKEN_ARG,VALID_HOST_ARG});
        assertEquals(expected,datasynced.getThreadCount());

        invalid = 2000;
        expected = 1000;
        tcArg = String.format("%s=%d",DatasyncConfig.Flag.THREAD_COUNT,invalid);
        datasynced = new CountriesSyncConfig(new String[]{
                tcArg,VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG});
        assertEquals(expected,datasynced.getThreadCount());
    }


    @Test
    @DisplayName("invalid input thread count")
    void extractThreadCountInvalidInput() {
        String invalidArg = String.format("%s=%s",DatasyncConfig.Flag.THREAD_COUNT,"invalid");
        assertThrows(RuntimeException.class,() -> new CountriesSyncConfig(new String[]{invalidArg,VALID_HOST_ARG}));
        try {
            new CountriesSyncConfig(new String[]{invalidArg,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(DatasyncConfig.Flag.THREAD_COUNT));
        }

        int valid = 10;
        String invalidArg2 = String.format("%s=%d=pl",DatasyncConfig.Flag.THREAD_COUNT,valid);
        assertThrows(RuntimeException.class,() -> new CountriesSyncConfig(new String[]{invalidArg2,VALID_HOST_ARG}));
        try {
            new CountriesSyncConfig(new String[]{invalidArg2,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(DatasyncConfig.Flag.THREAD_COUNT));
        }
    }

    @Test
    @DisplayName("invalid airline")
    void airlineInvalid() {
        String invalid = "invalid";
        String aArg = String.format("%s=%s",AirlineSyncConfig.Flag.AIRLINE,invalid);
        assertThrows(RuntimeException.class,() -> new RoutesSyncConfig(new String[]{aArg,VALID_HOST_ARG}));
        try {
            new RoutesSyncConfig(new String[]{aArg,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(AirlineSyncConfig.Flag.AIRLINE));
        }
    }

    @Test
    @DisplayName("invalid port")
    void portInvalid() {
        int invalid = -20;
        String pArg = String.format("%s=%d",DatasyncConfig.Flag.PORT,invalid);
        assertThrows(RuntimeException.class,() -> new CountriesSyncConfig(new String[]{pArg,VALID_HOST_ARG}));
        try {
            new CountriesSyncConfig(new String[]{pArg,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(DatasyncConfig.Flag.PORT));
        }

        invalid = 700000;
        String pArg2 = String.format("%s=%d",DatasyncConfig.Flag.PORT,invalid);
        assertThrows(RuntimeException.class,() -> new CountriesSyncConfig(new String[]{pArg2,VALID_HOST_ARG}));
        try {
            new CountriesSyncConfig(new String[]{pArg2,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(DatasyncConfig.Flag.PORT));
        }
    }

    @Test
    @DisplayName("back to args")
    void convertBackToArgs() {
        DatasyncConfig datasynced = new CountriesSyncConfig(new String[]{
                VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG
        });
        String[] actual = datasynced.toArgs();
        assertTrue(Arrays.stream(actual).anyMatch(arg -> arg.equals(DatasyncConfig.Flag.THREAD_COUNT.concat("=").concat(String.valueOf(DatasyncConfig.Defaults.THREAD_COUNT)))));
        datasynced.setThreadCountMax(1);

        Arrays.stream(actual).forEach(System.out::println);
    }
}