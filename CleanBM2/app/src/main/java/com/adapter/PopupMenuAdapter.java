package com.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cleanbm.R;
import com.javabeans.Popup_Menu_Item;

/**
 * Created by Ratufa.Paridhi on 8/6/2015.
 */
public class PopupMenuAdapter extends ArrayAdapter<Popup_Menu_Item> {
    Context context;
    int layoutResourceId;
    Popup_Menu_Item menu[] = null;

    public PopupMenuAdapter(Context context, int layoutResourceId, Popup_Menu_Item[] menu) {
        super(context, layoutResourceId, menu);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.menu = menu;

    }

    @Override
    public int getCount() {
        return menu.length;
    }


/*

    @Override
    public long getItemId(int position) {

        return 0;
    }
*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        MenuHolder menuHolder = null;

        if (view == null) {
            Log.e("View is null", (view == null) + "");
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            menuHolder = new MenuHolder();
            menuHolder.txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            menuHolder.imgIcon = (ImageView) view.findViewById(R.id.imgIcon);

            view.setTag(menuHolder);
        } else {
            Log.e("View is null.....", (view == null) + "");
            menuHolder = (MenuHolder) view.getTag();
        }
        Popup_Menu_Item menus = menu[position];
        menuHolder.txtTitle.setText(menus.title);
        menuHolder.imgIcon.setImageResource(menus.icon);
        Log.e("Vidffdf..", (view == null) + "");
        return view;
    }

    static class MenuHolder {
        ImageView imgIcon;
        TextView txtTitle;
    }
}
