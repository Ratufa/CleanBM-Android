package com.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.javabeans.PlacesSuggestionsBean;

/** Customizing AutoCompleteTextView to return Place Description   
 *  corresponding to the selected item
 */
public class CustomAutoCompleteTextView extends AutoCompleteTextView {
	
	public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/** Returns the Place Description corresponding to the selected item */
	@Override
	protected CharSequence convertSelectionToString(Object selectedItem) {
		/** Each item in the autocompetetextview suggestion list is a hashmap object */
//		HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
		PlacesSuggestionsBean hm = (PlacesSuggestionsBean) selectedItem;
		return hm.getDescription();
	}
}
