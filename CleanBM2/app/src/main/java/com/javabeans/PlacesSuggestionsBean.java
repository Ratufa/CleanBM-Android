package com.javabeans;

import java.io.Serializable;

/**
 * Created by Kailash on 15-Sep-15.
 */
public class PlacesSuggestionsBean implements Serializable {

    String description, id, reference;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
