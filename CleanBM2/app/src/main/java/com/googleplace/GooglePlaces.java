package com.googleplace;

import android.content.Context;
import android.util.Log;

import com.cleanbm.R;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.javabeans.PlaceDetails;
import com.javabeans.PlacesList;

//import com.google.api.client.json.jackson2.JacksonFactory;

@SuppressWarnings("deprecation")
public class GooglePlaces {

    /**
     * Global instance of the HTTP transport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Google API Key
    private static final String API_KEY = "AIzaSyAL374yoq8v29UEOg_JyiT7DN5myHfogf8"; // place your API key here :AIzaSyAL374yoq8v29UEOg_JyiT7DN5myHfogf8

    // Google Places serach url's
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private static final String PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    private double _latitude;
    private double _longitude;
    private double _radius;

    /**
     * Searching places
     *
     * @param latitude - latitude of place
     * @param radius   - radius of searchable area
     * @param types    - type of place to search
     * @return list of places
     * @params longitude - longitude of place
     */
    public PlacesList search(double latitude, double longitude, double radius, String types)
            throws Exception {

        this._latitude = latitude;
        this._longitude = longitude;
        this._radius = radius;

        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("location", _latitude + "," + _longitude);
            request.getUrl().put("radius", _radius); // in meters
            request.getUrl().put("sensor", "false");
            if (types != null)
                request.getUrl().put("types", types);

            PlacesList list = request.execute().parseAs(PlacesList.class);
            // Check log cat for places response status
            Log.d("Places Status", "" + list.status);
            return list;

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }

    }

    /**
     * Searching single place full details
     */
    public PlaceDetails getPlaceDetails(String reference) throws Exception {
        try {
            Log.d("GooglePlace", " " + API_KEY + " " + reference);
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
            Log.d("key ref", " " + API_KEY + " " + reference);
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("reference", reference);
            request.getUrl().put("sensor", "false");

            PlaceDetails place = request.execute().parseAs(PlaceDetails.class);

            return place;

        } catch (HttpResponseException e) {
            Log.e("Error in Perfs", e.getMessage());
            throw e;
        }
    }

    /**
     * Creating http request Factory
     */
    public static HttpRequestFactory createRequestFactory(
            final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                GoogleHeaders headers = new GoogleHeaders();
                headers.setApplicationName("CleanBM");
                request.setHeaders(headers);
                //			JsonHttpContent parse = new JsonHttpContent(new JacksonFactory(),request);
                JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
                request.addParser(parser);

            }
        });
    }

    public static String getAutoCompleteUrl(Context context, String place) {

        // Obtain browser key from https://code.google.com/apis/console Extra key for test : AIzaSyCdi7F8PV02m13lhPm3gRQEmsEhWHB_iXk
        String key = "key=" +context.getString(R.string.browser_key);

        // place to be be searched
        String input = "input=" + place;

        // place type to be searched
        String types = "types=geocode";

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = input + "&" + types + "&" + sensor + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

        return url;
    }

    public static String getPlaceDetailsUrl(Context context, String ref) {

        // Obtain browser key from https://code.google.com/apis/console Extra key for test : AIzaSyCdi7F8PV02m13lhPm3gRQEmsEhWHB_iXk
        String key = "key=" + context.getString(R.string.browser_key);

        // reference of place
        String reference = "reference=" + ref;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = reference + "&" + sensor + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/" + output + "?" + parameters;

        return url;
    }

}
