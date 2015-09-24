package com.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.adapter.PopupMenuAdapter;
import com.cleanbm.R;
import com.javabeans.Popup_Menu_Item;
import com.parse.ParseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ratufa.Paridhi on 7/27/2015.
 */
public class Utils {

    public static void hideKeyBoard(Context c, View v) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static boolean isValidEmailAddress(String emailAddress) {
        String expression = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
        CharSequence inputStr = emailAddress;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.matches();
    }

    public static void sendExceptionReport(Exception e, Context c) {
        e.printStackTrace();

        // try {
        // Writer result = new StringWriter();
        // PrintWriter printWriter = new PrintWriter(result);
        // e.printStackTrace(printWriter);
        // String stacktrace = result.toString();
        // new CustomExceptionHandler(c).sendToServer(stacktrace);
        // } catch (Exception e1) {
        // e1.printStackTrace();
        // }

    }

    public static void setPref(Context c, String pref, String val) {
        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
        e.putString(pref, val);
        e.commit();
    }

    public static String getPref(Context c, String pref, String val) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(pref, val);
    }

    public static boolean isInternetConnected(Context mContext) {

        try {
            ConnectivityManager connect = null;
            connect = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connect != null) {
                NetworkInfo resultMobile = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                NetworkInfo resultWifi = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if ((resultMobile != null && resultMobile.isConnectedOrConnecting()) || (resultWifi != null && resultWifi.isConnectedOrConnecting())) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void setProgress(Context context,boolean visibility) {
       ProgressDialog pd = null;
        if (visibility) {
            try {
                if (pd == null) {
                    pd = new ProgressDialog(context);
                    pd.setTitle("");
                    pd.setCancelable(false);
                    pd.setMessage(context.getString(R.string.loading));
                    pd.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress_dialog));
                    pd.setCancelable(true);
                    if (!pd.isShowing())
                        pd.show();
                } else {
                    if (!pd.isShowing()) {
                        pd.show();
                    }
                }
            } catch (Exception e) {
                Utils.sendExceptionReport(e, context);
                e.printStackTrace();
            }
        } else {
            try {
                if (pd.isShowing())
                    pd.dismiss();
            } catch (Exception e) {
                Utils.sendExceptionReport(e, context);
                e.printStackTrace();
            }
        }
    }

}

