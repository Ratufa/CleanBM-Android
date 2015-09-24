package com.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.cleanbm.R;

/**
 * Created by Ratufa.Paridhi on 7/27/2015.
 */
public class AlertDialogManager
{
    public void showAlertDialog(Activity context, String message) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();

            // Setting Dialog Title
            // alertDialog.setTitle(title);

            // Setting Dialog Message
            alertDialog.setMessage(message);
            //
            // if(status != null)
            // // Setting alert dialog icon
            // alertDialog
            // .setIcon((status) ? R.drawable.success : R.drawable.fail);

            // Setting OK Button
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.OK), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            // Showing Alert Message
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
          //  Utils.sendExceptionReport(e, context);
        }

    }
}
