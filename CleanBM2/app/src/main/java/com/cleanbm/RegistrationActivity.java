package com.cleanbm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.Utils.Utils;
import com.dialog.AlertDialogManager;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by Ratufa.Paridhi on 7/27/2015.
 */
public class RegistrationActivity extends Activity {

    TextView txtRegister;
    EditText editTxtName, editTxtEmail, editTxtPassword, editTxtConfirmPass;
    String name, email, password, confirm_password;

    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // For parse Object
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        txtRegister = (TextView) findViewById(R.id.txtRegister);
        txtRegister.setOnClickListener(mRegisterButtonClick);

        editTxtName = (EditText) findViewById(R.id.edtName);
        editTxtEmail = (EditText) findViewById(R.id.edtEmail);
        editTxtPassword = (EditText) findViewById(R.id.edtPassword);
        editTxtConfirmPass = (EditText) findViewById(R.id.edtConfirmPassword);

    }

    View.OnClickListener mRegisterButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == txtRegister) {
                Utils.hideKeyBoard(getApplicationContext(), v);
                onClickRegister();
            }
        }
    };

    public void onClickRegister() {
        if (Utils.isInternetConnected(RegistrationActivity.this)) {
            if (editTxtName.getText().toString().trim().length() <= 0) {
                alert.showAlertDialog(this, getResources().getString(R.string.enter_name));
            } else if (editTxtEmail.getText().toString().trim().length() <= 0) {
                alert.showAlertDialog(this, getResources().getString(R.string.enter_email));
            } else if (!Utils.isValidEmailAddress(editTxtEmail.getText().toString())) {
                alert.showAlertDialog(this, getResources().getString(R.string.enter_email));
            } else if (editTxtPassword.getText().toString().length() <= 0) {
                alert.showAlertDialog(this, getResources().getString(R.string.enter_Confirmpassword));
            } else if (editTxtPassword.getText().toString().length() < 4) {
                alert.showAlertDialog(this, getResources().getString(R.string.enter_password));
            } else if (editTxtConfirmPass.getText().toString().length() <= 0) {
                alert.showAlertDialog(this, getResources().getString(R.string.enter_Confirmpassword));
            } else {
                if (!(editTxtPassword.getText().toString().equals(editTxtConfirmPass.getText().toString()))) {
                    alert.showAlertDialog(this, getResources().getString(R.string.passwordmismatch));
                } else {
                    name = editTxtName.getText().toString();
                    email = editTxtEmail.getText().toString();
                    password = editTxtPassword.getText().toString();
                    confirm_password = editTxtConfirmPass.getText().toString();

                    // Inserting values in the database of table Users
          /*  ParseObject insert_user_record = new ParseObject("User");
            insert_user_record.put("username",email);
            insert_user_record.put("email",email);
            insert_user_record.put("name",name);
            insert_user_record.put("password",password);
            insert_user_record.saveInBackground();*/

                    final ParseUser user = new ParseUser();
                    user.setUsername(email);
                    user.setPassword(password);
                    user.setEmail(email);
                    // other fields can be set just like with ParseObject
                    user.put("name", name);
                    user.put("userProfile","basic");
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                // Hooray! Let them use the app now.
                                Log.e("Success", "Hurrey");
                                // Toast.makeText(RegistrationActivity.this, "Successfully register", Toast.LENGTH_SHORT).show();
                                // alert.showAlertDialog(RegistrationActivity.this, getResources().getString(R.string.verify_email));
                                AlertDialog alertDialog = new AlertDialog.Builder(RegistrationActivity.this).create();

                                // Setting Dialog Title
                                // alertDialog.setTitle(title);

                                // Setting Dialog Message
                                alertDialog.setMessage(getResources().getString(R.string.verify_email));
                                //
                                // if(status != null)
                                // // Setting alert dialog icon
                                // alertDialog
                                // .setIcon((status) ? R.drawable.success : R.drawable.fail);

                                // Setting OK Button
                                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getApplicationContext().getString(R.string.OK), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(in);
                                        finish();
                                    }
                                });

                                // Showing Alert Message
                                alertDialog.show();


                            } else {
                                Log.e("Error", " " + e.getCode());
                                alert.showAlertDialog(RegistrationActivity.this, "Sorry, This email is already registered with us.");
                            }
                        }
                    });

                }
            }
        } else {
            alert.showAlertDialog(RegistrationActivity.this, getResources().getString(R.string.connection_not_available));
        }
    }

}
