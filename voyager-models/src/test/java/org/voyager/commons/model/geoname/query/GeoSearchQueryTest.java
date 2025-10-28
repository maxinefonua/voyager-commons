package org.voyager.commons.model.geoname.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.geoname.fields.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoSearchQueryTest {
    GeoSearchQuery geoSearchQuery;

    @BeforeEach
    void setUp() {
        geoSearchQuery = GeoSearchQuery.builder().query("query").build();
        assertNotNull(geoSearchQuery);
    }

    @Test
    void getName() {
        geoSearchQuery.setName("name");
        assertEquals("name",geoSearchQuery.getName());
    }

    @Test
    void getNameEquals() {
        geoSearchQuery.setNameEquals("nameEquals");
        assertEquals("nameEquals",geoSearchQuery.getNameEquals());
    }

    @Test
    void getNameStartsWith() {
        geoSearchQuery.setNameStartsWith("name starts with");
        assertEquals("name starts with",geoSearchQuery.getNameStartsWith());
    }

    @Test
    void getMaxRows() {
        assertEquals(100,geoSearchQuery.getMaxRows());
        geoSearchQuery.setMaxRows(10);
        assertEquals(10,geoSearchQuery.getMaxRows());
    }

    @Test
    void getStartRow() {
        assertEquals(0,geoSearchQuery.getStartRow());
        geoSearchQuery.setStartRow(5);
        assertEquals(5,geoSearchQuery.getStartRow());
    }

    @Test
    void getCountryList() {
        geoSearchQuery.setCountryList(List.of("country"));
        assertEquals("country",geoSearchQuery.getCountryList().get(0));
    }

    @Test
    void getCountryBias() {
        geoSearchQuery.setCountryBias("to");
        assertEquals("to",geoSearchQuery.getCountryBias());
    }

    @Test
    void getContinentCode() {
        geoSearchQuery.setContinentCode(Continent.OC);
        assertEquals(Continent.OC,geoSearchQuery.getContinentCode());
    }

    @Test
    void getAdminCode1() {
        geoSearchQuery.setAdminCode1("adminCode1");
        assertEquals("adminCode1",geoSearchQuery.getAdminCode1());
    }

    @Test
    void getAdminCode2() {
        geoSearchQuery.setAdminCode2("adminCode2");
        assertEquals("adminCode2",geoSearchQuery.getAdminCode2());
    }

    @Test
    void getAdminCode3() {
        geoSearchQuery.setAdminCode3("adminCode3");
        assertEquals("adminCode3",geoSearchQuery.getAdminCode3());
    }

    @Test
    void getAdminCode4() {
        geoSearchQuery.setAdminCode4("adminCode4");
        assertEquals("adminCode4",geoSearchQuery.getAdminCode4());
    }

    @Test
    void getAdminCode5() {
        geoSearchQuery.setAdminCode5("adminCode5");
        assertEquals("adminCode5",geoSearchQuery.getAdminCode5());
    }

    @Test
    void getFeatureClass() {
        geoSearchQuery.setFeatureClass(FeatureClass.P);
        assertEquals(FeatureClass.P,geoSearchQuery.getFeatureClass());
    }

    @Test
    void getFeatureCode() {
        geoSearchQuery.setFeatureCodeList(List.of(FeatureCode.ADM1));
        assertEquals(FeatureCode.ADM1,geoSearchQuery.getFeatureCodeList().get(0));
    }

    @Test
    void getCities() {
        geoSearchQuery.setCities(CitySize.cities15000);
        assertEquals(CitySize.cities15000,geoSearchQuery.getCities());
    }

    @Test
    void getLang() {
        assertEquals("en",geoSearchQuery.getLang());
        geoSearchQuery.setLang("lang");
        assertEquals("lang",geoSearchQuery.getLang());
    }

    @Test
    void getType() {
        assertEquals(ResponseType.xml,geoSearchQuery.getType());
        geoSearchQuery.setType(ResponseType.json);
        assertEquals(ResponseType.json,geoSearchQuery.getType());
    }

    @Test
    void getStyle() {
        geoSearchQuery.setStyle(XmlVerbosity.SHORT);
        assertEquals(XmlVerbosity.SHORT,geoSearchQuery.getStyle());
    }

    @Test
    void getIsNameRequired() {
        geoSearchQuery.setIsNameRequired(true);
        assertEquals(true,geoSearchQuery.getIsNameRequired());
    }

    @Test
    void getTag() {
        geoSearchQuery.setTag("tag");
        assertEquals("tag",geoSearchQuery.getTag());
    }

    @Test
    void getOperator() {
        assertEquals(SearchOperator.AND,geoSearchQuery.getOperator());
        geoSearchQuery.setOperator(SearchOperator.OR);
        assertEquals(SearchOperator.OR,geoSearchQuery.getOperator());
    }

    @Test
    void getCharSet() {
        assertEquals(DocumentEncoding.UTF8,geoSearchQuery.getCharSet());
        geoSearchQuery.setCharSet(DocumentEncoding.UTF8);
        assertEquals(DocumentEncoding.UTF8,geoSearchQuery.getCharSet());
    }

    @Test
    void getFuzzy() {
        assertEquals(1f,geoSearchQuery.getFuzzy());
        geoSearchQuery.setFuzzy(0.5);
        assertEquals(0.5f,geoSearchQuery.getFuzzy());
    }

    @Test
    void getEast() {
        geoSearchQuery.setEast(10.5);
        assertEquals(10.5f,geoSearchQuery.getEast());
    }

    @Test
    void getWest() {
        geoSearchQuery.setWest(60.5);
        assertEquals(60.5f,geoSearchQuery.getWest());
    }

    @Test
    void getNorth() {
        geoSearchQuery.setEast(-10.5);
        assertEquals(-10.5f,geoSearchQuery.getEast());
    }

    @Test
    void getSouth() {
        geoSearchQuery.setWest(-60.5);
        assertEquals(-60.5f,geoSearchQuery.getWest());
    }

    @Test
    void getSearchLang() {
        geoSearchQuery.setSearchLang("searchLang");
        assertEquals("searchLang",geoSearchQuery.getSearchLang());
    }

    @Test
    void getOrderBy() {
        geoSearchQuery.setOrderBy(SearchOrder.elevation);
        assertEquals(SearchOrder.elevation,geoSearchQuery.getOrderBy());
    }

    @Test
    void getInclBbox() {
        geoSearchQuery.setInclBbox(true);
        assertEquals(true,geoSearchQuery.getInclBbox());
    }
}