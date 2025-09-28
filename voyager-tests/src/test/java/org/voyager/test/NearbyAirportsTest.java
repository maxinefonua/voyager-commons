package org.voyager.test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;
import org.junit.Test;
import org.voyager.config.VoyagerConfig;
import org.voyager.model.Airline;
import org.voyager.model.airport.AirportType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.voyager.utils.ConstantsUtils.*;

public class NearbyAirportsTest {
    private static RequestSpecification requestSpec;
    @BeforeClass
    public static void setup() {
        String portConfig = VoyagerConfig.getProperty("voyager.port");
        if (portConfig != null && !portConfig.trim().isEmpty()) {
            RestAssured.port = Integer.parseInt(portConfig);
        }
        RestAssured.basePath = VoyagerConfig.getProperty("voyager.path.nearby-airports");
        String authToken = VoyagerConfig.getProperty("voyager.auth.token");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetNearbyAirports() {
        Double latitudeOfSLC = 40.7695;
        Double longitudeOfSLC = -111.8912;
        given()
                .spec(requestSpec)
                .queryParams(LATITUDE_PARAM_NAME,latitudeOfSLC,
                        LONGITUDE_PARAM_NAME,longitudeOfSLC,
                        LIMIT_PARAM_NAME,3)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].iata",equalTo("SLC"))
                .body("[1].iata",equalTo("BTF"))
                .body("[2].iata",equalTo("HIF"));
    }

    @Test
    public void testGetNearbyAirportsWithFilters() {
        Double latitudeOfSLC = 40.7695;
        Double longitudeOfSLC = -111.8912;
        given()
                .spec(requestSpec)
                .queryParams(LATITUDE_PARAM_NAME,latitudeOfSLC,
                        LONGITUDE_PARAM_NAME,longitudeOfSLC,
                        LIMIT_PARAM_NAME,3,
                        TYPE_PARAM_NAME, AirportType.CIVIL.name(),
                        AIRLINE_PARAM_NAME, Airline.UNITED)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].iata",equalTo("SLC"))
                .body("[1].iata",equalTo("RKS"))
                .body("[2].iata",equalTo("IDA"));
    }
}
