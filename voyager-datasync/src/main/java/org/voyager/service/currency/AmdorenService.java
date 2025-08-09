package org.voyager.service.currency;

public class AmdorenService {
    private static final String baseURL = "https://www.amdoren.com";
    private static final String currencyPath = "/api/currency.php";
    private static final String currencyParams = "?api_key=%s&from=%s&to=%s";
    // amount	The amount to convert from. This parameter is optional. Default is a value of 1.
    private static final String AMDOREN_API_KEY = System.getenv("AMDOREN_API_KEY");

    //{ "error" : 0, "error_message" : "-", "amount" : 0.90168 }
}
