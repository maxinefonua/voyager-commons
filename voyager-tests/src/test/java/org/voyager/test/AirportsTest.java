package org.voyager.test;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;
import org.junit.Test;
import org.voyager.config.VoyagerConfig;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.voyager.utils.ConstantsUtils.*;

public class AirportsTest {
    private static RequestSpecification requestSpec;
    @BeforeClass
    public static void setup() {
        String portConfig = VoyagerConfig.getProperty("voyager.port");
        if (portConfig != null && !portConfig.trim().isEmpty()) {
            RestAssured.port = Integer.parseInt(portConfig);
        }
        RestAssured.basePath = VoyagerConfig.getProperty("voyager.path.airports");
        String authToken = VoyagerConfig.getProperty("voyager.auth.token");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetIataCodes() {
        given()
                .spec(requestSpec)
                .basePath(VoyagerConfig.getProperty("voyager.path.iata"))
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", greaterThan(0)) // Assert the response is a non-empty array
                .body("$", hasItem("HNL"));
    }

    @Test
    public void testPostIata() {
        given()
                .spec(requestSpec)
                .basePath(VoyagerConfig.getProperty("voyager.path.iata"))
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(405);
    }

    @Test
    public void testGetAirports() {
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", greaterThan(0)) // Assert the response is a non-empty array
                .body("iata", hasItem("HNL"));
    }

    @Test
    public void testGetAirport() {
        given()
                .spec(requestSpec)
                .when()
                .get("/ITM")
                .then()
                .assertThat()
                .statusCode(200)
                .body("subdivision",equalTo("Hyogo"));
    }

    @Test
    public void testPatchAirport() {
        AirportPatch airportPatch1 = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
        AirportPatch airportPatch2 = AirportPatch.builder().type(AirportType.CIVIL.name()).build();
        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch1)
                .when()
                .patch("/SJC")
                .then()
                .assertThat()
                .statusCode(200)
                .body("type",equalTo(AirportType.UNVERIFIED.name()));

        given()
                .spec(requestSpec)
                .basePath(VoyagerConfig.getProperty("voyager.path.airports"))
                .contentType(ContentType.JSON)
                .body(airportPatch2)
                .when()
                .patch("/SJC")
                .then()
                .assertThat()
                .statusCode(200)
                .body("type",equalTo(AirportType.CIVIL.name()));
    }
}
