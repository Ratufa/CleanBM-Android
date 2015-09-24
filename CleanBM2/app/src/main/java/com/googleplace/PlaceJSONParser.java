package com.googleplace;

import com.javabeans.PlacesSuggestionsBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceJSONParser {

    /**
     * Receives a JSONObject and returns a list
     */
    public List<HashMap<String, String>> parse(JSONObject jObject) {

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }


    public ArrayList<PlacesSuggestionsBean> getSuggestionsList(JSONObject jsonObject) {
        String id = "";
        String reference = "";
        String description = "";
        JSONArray jPlaces = null;
        ArrayList<PlacesSuggestionsBean> placeList = new ArrayList<PlacesSuggestionsBean>();
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jsonObject.getJSONArray("predictions");

            /** Taking each place, parses and adds to list object */
            for (int i = 0; i < jPlaces.length(); i++) {
                JSONObject jPlace = (JSONObject) jPlaces.get(i);
                /** Call getPlace with place JSON object to parse the place */
//                    place = getPlace((JSONObject) jPlaces.get(i));
//                    placesList.add(place);

                description = jPlace.getString("description");
                id = jPlace.getString("id");
                reference = jPlace.getString("reference");

                PlacesSuggestionsBean placesSuggestionsBean = new PlacesSuggestionsBean();
                placesSuggestionsBean.setDescription(description);
                placesSuggestionsBean.setId(id);
                placesSuggestionsBean.setReference(reference);
                placeList.add(placesSuggestionsBean);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return placeList;
    }


    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for (int i = 0; i < placesCount; i++) {
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject) jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /**
     * Parsing the Place JSON object
     */
    private HashMap<String, String> getPlace(JSONObject jPlace) {

        HashMap<String, String> place = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";

        try {
            // Extracting Place name, if available
            if (!jPlace.isNull("name")) {
                placeName = jPlace.getString("name");
            }

            // Extracting Place Vicinity, if available
            if (!jPlace.isNull("vicinity")) {
                vicinity = jPlace.getString("vicinity");
            }

            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");

            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", latitude);
            place.put("lng", longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
