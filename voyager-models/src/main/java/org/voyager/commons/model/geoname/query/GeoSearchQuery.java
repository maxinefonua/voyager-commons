package org.voyager.commons.model.geoname.query;

import jakarta.validation.constraints.*;
import lombok.*;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.geoname.fields.DocumentEncoding;
import org.voyager.commons.model.geoname.fields.ResponseType;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import org.voyager.commons.model.geoname.fields.FeatureClass;
import org.voyager.commons.model.geoname.fields.CitySize;
import org.voyager.commons.model.geoname.fields.XmlVerbosity;
import org.voyager.commons.model.geoname.fields.FeatureCode;
import org.voyager.commons.model.geoname.fields.SearchOrder;
import org.voyager.commons.validate.annotations.ValidCountryCode;
import org.voyager.commons.validate.annotations.ValidLanguageCode;
import org.voyager.commons.validate.annotations.ValidLatitude;
import org.voyager.commons.validate.annotations.ValidLongitude;

import java.util.List;

@Builder
@Getter @Setter
@AllArgsConstructor
public class GeoSearchQuery {
    @NotBlank
    @NonNull
    private String query; //q	string (q,name or name_equals required)	search over all attributes of a place : place name, country name, continent, admin codes,... (Important:urlencoded utf8)
    @Pattern(regexp = Regex.NONEMPTY_TRIMMED)
    private String name; //name	string (q,name or name_equals required)	place name only(Important:urlencoded utf8)
    @Pattern(regexp = Regex.NONEMPTY_TRIMMED)
    private String nameEquals; //name_equals	string (q,name or name_equals required)	exact place name
    @Pattern(regexp = Regex.NONEMPTY_TRIMMED)
    private String nameStartsWith; //name_startsWith	string (optional)	place name starts with given characters

    @Builder.Default
    @Min(1) @Max(100)
    private Integer maxRows = 100; //maxRows	integer (optional)	the maximal number of rows in the document returned by the service. Default is 100, the maximal allowed value is 1000.

    @Builder.Default
    @Min(0)
    private Integer startRow = 0; //startRow	integer (optional)	Used for paging results. If you want to get results 30 to 40, use startRow=30 and maxRows=10. Default is 0, the maximal allowed value is 5000 for the free services and 25000 for the premium services

    private List<@ValidCountryCode(caseSensitive = false,
            message = Regex.ConstraintMessage.COUNTRY_CODE_ELEMENTS) String> countryList; //country	string : country code, ISO-3166 (optional)	Default is all countries. The country parameter may occur more than once, example: country=FR&country=GP

    @ValidCountryCode(caseSensitive = false, allowNull = true)
    private String countryBias; //countryBias	string (option), two-letter country code ISO-3166	records from the countryBias are listed first

    private Continent continentCode; //continentCode	string : continent code : AF,AS,EU,NA,OC,SA,AN (optional)	restricts the search for toponym of the given continent.

    private String adminCode1; //adminCode1, adminCode2, adminCode3, adminCode4, adminCode5	string : admin code (optional)	code of administrative subdivision
    private String adminCode2;
    private String adminCode3;
    private String adminCode4;
    private String adminCode5;

    private FeatureClass featureClass;//featureClass	character A,H,L,P,R,S,T,U,V (optional)	feature class(es) (default= all feature classes); this parameter may occur more than once, example: featureClass=P&featureClass=A

    private List<@NotNull FeatureCode> featureCodeList; //featureCode	string (optional)	feature code(s) (default= all feature codes); this parameter may occur more than once, example: featureCode=PPLC&featureCode=PPLX
    private CitySize cities; //cities	string (optional)	optional filter parameter with three possible values 'cities1000', 'cities5000','cities15000' used to categorize the populated places into three groups according to size/relevance. See the download readme for further infos

    @Builder.Default
    @ValidLanguageCode
    private String lang = "en"; //lang	string ISO-639 2-letter language code; en,de,fr,it,es,zh,zh-Hant... (optional)	place name and country name will be returned in the specified language. Default is English. With the pseudo language code 'local' the local language will be returned. Feature classes and codes are only available in English and Bulgarian. Any help in translating is welcome.

    @Builder.Default
    private ResponseType type = ResponseType.xml; //type	string xml,json,rdf	the format type of the returned document, default = xml
    private XmlVerbosity style; //style	string SHORT,MEDIUM,LONG,FULL (optional)	verbosity of returned xml document, default = MEDIUM

    private Boolean isNameRequired; //isNameRequired	boolean (optional)	At least one of the search term needs to be part of the place name. Example : A normal search for Berlin will return all places within the state of Berlin. If we only want to find places with 'Berlin' in the name we set the parameter isNameRequired to 'true'. The difference to the name_equals parameter is that this will allow searches for 'Berlin, Germany' as only one search term needs to be part of the name.

    @Pattern(regexp = Regex.NONEMPTY_TRIMMED,
            message = Regex.ConstraintMessage.NONEMPTY_TRIMMED)
    private String tag; //tag	string (optional)	search for toponyms tagged with the specified tag

    @Builder.Default
    private SearchOperator operator = SearchOperator.AND; //operator	string (optional)	default is 'AND', with the operator 'OR' not all search terms need to be matched by the response

    @Builder.Default
    private DocumentEncoding charSet = DocumentEncoding.UTF8; //charset	string (optional)	default is 'UTF8', defines the encoding used for the document returned by the web service.

    @Builder.Default
    @Min(0) @Max(1)
    private Double fuzzy = 1.0; //fuzzy	float (optional)	default is '1', defines the fuzziness of the search terms. float between 0 and 1. The search term is only applied to the name attribute.

    @ValidLongitude(allowNull = true)
    private Double east; //east,west,north,south	float (optional)	bounding box, only features within the box are returned
    @ValidLongitude(allowNull = true)
    private Double west;
    @ValidLatitude(allowNull = true)
    private Double north;
    @ValidLatitude(allowNull = true)
    private Double south;

    @ValidLanguageCode(allowNull = true)
    private String searchLang; //search lang	string (optional)	in combination with the name parameter, the search will only consider names in the specified language. Used for instance to query for IATA airport codes.

    private SearchOrder orderBy; //order by	string (optional)[population,elevation,relevance]	in combination with the name_startsWith, if set to 'relevance' than the result is sorted by relevance.

    private Boolean inclBbox; //inclBbox include Bbox info, regardless of style setting. (normally only included with style=FULL
}
