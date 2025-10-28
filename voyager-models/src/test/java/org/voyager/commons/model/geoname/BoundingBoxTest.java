package org.voyager.commons.model.geoname;

import org.junit.jupiter.api.Test;
import org.voyager.commons.model.geoname.BoundingBox;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoundingBoxTest {

    @Test
    void testClass() {
        Double[] expected = new Double[]{1.0,2.0,3.0,4.0};
        Integer expectedAccuracy = 1;
        BoundingBox boundingBox = new BoundingBox();
        boundingBox.setEast(expected[0]);
        boundingBox.setWest(expected[1]);
        boundingBox.setNorth(expected[2]);
        boundingBox.setSouth(expected[3]);
        boundingBox.setAccuracyLevel(expectedAccuracy);

        assertEquals(expected[0],boundingBox.getEast());
        assertEquals(expected[1],boundingBox.getWest());
        assertEquals(expected[2],boundingBox.getNorth());
        assertEquals(expected[3],boundingBox.getSouth());
        assertEquals(expectedAccuracy,boundingBox.getAccuracyLevel());
    }
}