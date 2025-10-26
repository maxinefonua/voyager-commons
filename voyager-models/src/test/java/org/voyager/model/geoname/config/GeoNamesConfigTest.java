package org.voyager.model.geoname.config;

import org.junit.jupiter.api.Test;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.geoname.config.GeoNamesConfig;
import org.voyager.commons.model.geoname.fields.*;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeoNamesConfigTest {
    public static final String TEST_USERNAME = "geoUsername";
    public GeoNamesConfig geoNamesConfig = new GeoNamesConfig(TEST_USERNAME);

    @Test
    void getNearbyPlaceURL() {
        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().longitude(10.0).latitude(9.0).radius(10).build();
        String url = geoNamesConfig.getNearbyPlaceURL(geoNearbyQuery);
        assertEquals("https://secure.geonames.org/findNearbyPlaceNameJSON?username=geoUsername&lat=9.000000&lng=10.000000&radius=10",url);

        geoNearbyQuery.setRadius(null);
        url = geoNamesConfig.getNearbyPlaceURL(geoNearbyQuery);
        assertEquals("https://secure.geonames.org/findNearbyPlaceNameJSON?username=geoUsername&lat=9.000000&lng=10.000000",url);
    }

    @Test
    void getTimezoneURL() {
        GeoTimezoneQuery geoTimezoneQuery = GeoTimezoneQuery.builder().longitude(10.0).latitude(9.0).radius(10)
                .language("en").date("date").build();
        String url = geoNamesConfig.getTimezoneURL(geoTimezoneQuery);
        assertEquals("https://secure.geonames.org/timezoneJSON?username=geoUsername&lat=9.000000&lng=9.000000&lang=en&date=date&radius=10",url);

        geoTimezoneQuery = GeoTimezoneQuery.builder().longitude(10.0).latitude(9.0).build();
        url = geoNamesConfig.getTimezoneURL(geoTimezoneQuery);
        assertEquals("https://secure.geonames.org/timezoneJSON?username=geoUsername&lat=9.000000&lng=9.000000",url);
    }

    @Test
    void getCountriesURL() {
        String url = geoNamesConfig.getCountriesURL();
        assertEquals("https://secure.geonames.org/countryInfoJSON?username=geoUsername",url);
    }

    @Test
    void getByIdURL() {
        String url = geoNamesConfig.getByIdURL(1001L);
        assertEquals("https://secure.geonames.org/getJSON?username=geoUsername&geonameId=1001",url);
    }

    @Test
    void getSearchURL() {
        GeoSearchQuery geoSearchQuery = GeoSearchQuery.builder().query("query").adminCode1("admin").adminCode2("admin")
                .adminCode3("admin").adminCode4("admin").adminCode5("admin").continentCode(Continent.OC)
                .east(0.1f).west(0.1f).north(0.1f).south(0.1f).name("name").fuzzy(null).lang(null).type(null)
                .cities(CitySize.cities15000).countryBias("to").isNameRequired(true).searchLang("searchLang")
                .countryList(List.of("country")).maxRows(null).startRow(null).nameEquals("name")
                .nameStartsWith("name").orderBy(SearchOrder.elevation)
                .charSet(DocumentEncoding.UTF8).featureClass(FeatureClass.P).featureCode(FeatureCode.ADM1)
                .tag("tag").inclBbox(true).operator(null).style(XmlVerbosity.FULL).build();
        String url = geoNamesConfig.getSearchURL(geoSearchQuery);
        assertEquals("https://secure.geonames.org/searchJSON?username=geoUsername&q=query&name=name&name_equals=name&name_startsWith=name&country=country&countryBias=to&continentCode=OC&adminCode1=admin&adminCode2=admin&adminCode3=admin&adminCode4=admin&adminCode5=admin&featureClass=P&featureCode=ADM1&cities=cities15000&style=FULL&isNameRequired=true&tag=tag&charset=UTF8&east=0.100000&west=0.100000&south=0.100000&north=0.100000&searchlang=searchLang&orderby=elevation&inclBbox=true",url);

        geoSearchQuery = GeoSearchQuery.builder().query("query").countryList(List.of()).charSet(null).orderBy(null).build();
        url = geoNamesConfig.getSearchURL(geoSearchQuery);
        assertEquals("https://secure.geonames.org/searchJSON?username=geoUsername&q=query&maxRows=100&startRow=0&lang=en&type=xml&operator=AND&fuzzy=1.000000",url);
    }

    @Test
    void getSearchURLDefault() {
        GeoSearchQuery geoSearchQuery = GeoSearchQuery.builder().query("query").build();
        String url = geoNamesConfig.getSearchURL(geoSearchQuery);
        assertEquals("https://secure.geonames.org/searchJSON?username=geoUsername&q=query&maxRows=100&startRow=0&lang=en&type=xml&operator=AND&charset=UTF8&fuzzy=1.000000",url);
    }

    @Test
    void getSearchURLThrowsException() {
        assertThrows(NullPointerException.class,()->GeoSearchQuery.builder().query(null).build());
        assertThrows(NullPointerException.class,()->GeoSearchQuery.builder().build());
    }
}