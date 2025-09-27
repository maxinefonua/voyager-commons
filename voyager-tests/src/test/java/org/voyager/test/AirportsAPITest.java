package org.voyager.test;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;
import org.junit.Test;
import org.voyager.config.VoyagerClientConfig;
import org.voyager.model.Airline;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.voyager.utils.ConstantsUtils.*;

public class AirportsAPITest {
    private static RequestSpecification requestSpec;
    @BeforeClass
    public static void setup() {
        String portConfig = VoyagerClientConfig.getProperty("voyager.port");
        if (portConfig != null && !portConfig.trim().isEmpty()) {
            RestAssured.port = Integer.parseInt(portConfig);
        }
        RestAssured.basePath = VoyagerClientConfig.getProperty("voyager.path.airports");
        String authToken = VoyagerClientConfig.getProperty("voyager.auth.token");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetAll() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", greaterThan(0)) // Assert the response is a non-empty array
                .body("iata", hasItem("AKL"));
    }

    @Test
    public void testGetRecord() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get("/ITM")
                .then()
                .assertThat()
                .statusCode(200)
                .body("subdivision",equalTo("Hyogo"));
    }

    @Test
    public void testGetAirlines() {
        RestAssured.given()
                .spec(requestSpec)
                .basePath(VoyagerClientConfig.getProperty("voyager.path.airport-airlines"))
                .queryParam(IATA_PARAM_NAME,List.of("HEL"))
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", greaterThan(0)) // Assert the response is a non-empty array
                .body("",hasItem("FINNAIR"));
    }

    @Test
    public void testGetNearbyAirports() {
        Double latitudeOfSLC = 40.7695;
        Double longitudeOfSLC = -111.8912;
        RestAssured.given()
                .spec(requestSpec)
                .basePath(VoyagerClientConfig.getProperty("voyager.path.nearby-airports"))
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
        RestAssured.given()
                .spec(requestSpec)
                .basePath(VoyagerClientConfig.getProperty("voyager.path.nearby-airports"))
                .queryParams(LATITUDE_PARAM_NAME,latitudeOfSLC,
                        LONGITUDE_PARAM_NAME,longitudeOfSLC,
                        LIMIT_PARAM_NAME,3,
                        TYPE_PARAM_NAME,AirportType.CIVIL.name(),
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

    @Test
    public void testPatchRecord() {
        AirportPatch airportPatch1 = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
        AirportPatch airportPatch2 = AirportPatch.builder().type(AirportType.CIVIL.name()).build();
        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch1)
                .when()
                .patch("/SJC")
                .then()
                .assertThat()
                .statusCode(200)
                .body("type",equalTo(AirportType.UNVERIFIED.name()));

        RestAssured.given()
                .spec(requestSpec)
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
