package org.voyager.commons.model.geoname.fields;

@SuppressWarnings("SpellCheckingInspection")
public enum CitySize {
    cities1000, // all cities with a population > 1000 or seats of adm div down to PPLA3 (ca 130.000)
    cities5000, // all cities with a population > 5000 or PPLA (ca 50.000)
    cities15000 // all cities with a population > 15000 or capitals (ca 25.000)
}
