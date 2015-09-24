package com.javabeans;

import android.util.Log;

import com.google.api.client.util.Key;

import java.io.Serializable;

/** Implement this class from "Serializable"
* So that you can pass this class Object to another using Intents
* Otherwise you can't pass to another actitivy
* */
public class PlaceDetails implements Serializable {

	@Key
	public String status;
	
	@Key
	public Place result;

	@Override
	public String toString() {
		if (result!=null) {
			Log.d("PlaceDetails",result.toString());
			return result.toString();
		}
		return super.toString();
	}
}
