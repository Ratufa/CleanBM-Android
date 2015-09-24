package com.javabeans;

/**
 * Created by Ratufa.Paridhi on 8/21/2015.
 */
public class ReviewListItem {
    String username;

    public String getReview_user_id() {
        return review_user_id;
    }

    public void setReview_user_id(String review_user_id) {
        this.review_user_id = review_user_id;
    }

    String review_user_id;

    public String getReview_id() {
        return review_id;
    }

    public void setReview_id(String review_id) {
        this.review_id = review_id;
    }

    String review_id;

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    String Description;
    double rating;

    public ReviewListItem(String username, String Description, double rating,String review_id,String review_user_id)
    {
        this.username= username;
        this.Description=Description;
        this.rating=rating;
        this.review_id=review_id;
        this.review_user_id=review_user_id;
    }

}
