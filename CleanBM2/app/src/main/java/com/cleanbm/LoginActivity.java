package com.cleanbm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Utils.Utils;
import com.dialog.AlertDialogManager;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

// http://www.androidbegin.com/tutorial/android-parse-com-simple-login-and-signup-tutorial/

public class LoginActivity extends Activity {

    TextView txtLogin,txtSignUp, txtForgetPass,txtFbLogin,txtSkip;
    EditText edtEmail, edtPassword;
    String email,password;
    public static final String LOGIN_PREFERENCES = "LoginPrefs";
    private SharedPreferences.Editor loginPrefEditor;
    private Boolean saveLogin;
    private ProgressDialog pd;

    AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // For parse Object
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        // Check if there is a currently logged in user
        // and it's linked to a Facebook account.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            // Go to the user info activity
         //   Toast.makeText(getApplicationContext(),"Linked to FB",Toast.LENGTH_SHORT).show();
        }


        edtEmail = (EditText)findViewById(R.id.edtEmailLogin);
        edtPassword = (EditText)findViewById(R.id.edtPasswordLogin);

        txtLogin = (TextView)findViewById(R.id.txtLogin);
        txtLogin.setOnClickListener(mButtonClick);

        txtSignUp = (TextView)findViewById(R.id.txtSignUp);
        txtSignUp.setOnClickListener(mButtonClick);

        txtSkip =(TextView)findViewById(R.id.txtSkip);
        txtSkip.setOnClickListener(mButtonClick);
        if (isFirstTime()) {
            // show dialog
            txtSkip.setVisibility(View.VISIBLE);
        }
        else
        {
            txtSkip.setVisibility(View.GONE);
        }

        txtForgetPass = (TextView)findViewById(R.id.txtForgetPassword);
        txtForgetPass.setOnClickListener(mButtonClick);

        txtFbLogin =(TextView)findViewById(R.id.txtFbLogin);
        txtFbLogin.setOnClickListener(mButtonClick);
        // get the intent that we have passed from ActivityOne
        Intent intent = getIntent();

        String color = intent.getStringExtra("BathDescription");
        Log.d("BathDescription"," "+color);

    }

    private EditText mEtForgotPassword ;
    View.OnClickListener mButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == txtLogin)
            {  Utils.hideKeyBoard(getApplicationContext(), v);
                if(Utils.isInternetConnected(LoginActivity.this)) {
                    onLoginClick();
                }
                else
                {
                    alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.connection_not_available));
                }
            }
            else if(v == txtSignUp)
            {
                Intent intent = new Intent(getApplicationContext(),RegistrationActivity.class);
                startActivity(intent);
            }
            else if(v == txtForgetPass)
            {
                mEtForgotPassword = new EditText(LoginActivity.this);
                mEtForgotPassword.setText("");
                mEtForgotPassword.setHint(getString(R.string.enter_email_hint));
                new AlertDialog.Builder(LoginActivity.this).setTitle(getString(R.string.forgotpass)).setCancelable(true).setView(mEtForgotPassword).setPositiveButton("Reset", mAppidCommitListener).setNegativeButton("Cancel", mAppidCommitListener).show();
            }
            else if(v == txtFbLogin)
            {
                if(Utils.isInternetConnected(LoginActivity.this)) {
                    //  setProgress(true);
                    List<String> permissions = Arrays.asList("public_profile", "email");
                    // NOTE: for extended permissions, like "user_about_me", your app must be reviewed by the Facebook team
                    // (https://developers.facebook.com/docs/facebook-login/permissions/)

                    ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                        @Override
                        public void done(final ParseUser parseUser, com.parse.ParseException e) {
                            if (parseUser == null) {
                                //           Log.e("Parse user is null ","Uh no The user cancelled the fb login"+e.getLocalizedMessage());
                            } else if (parseUser.isNew()) {
                                Log.d("Is new user", "User signed up and logged in through fb");
                            } else {
                                Log.d("Message", "User logged in through FB!!");
                                Toast.makeText(getApplicationContext(), "You are logged in successfully.", Toast.LENGTH_SHORT).show();
                                if (getIntent().getExtras() != null) {
                                                   /*String  bath_desc = getIntent().getStringExtra("BathDescription");
                                                   String bath_type = getIntent().getStringExtra("BathType");
                                                   float bath_rate = getIntent().getFloatExtra("BathRating",0.0f);

                                                   Log.d("LoginActivity","get intent"+bath_desc+" "+bath_type+" "+bath_rate);

                                                   Log.d("LoginActivity", "  " + (getIntent().getExtras() != null));
                                                   Intent intent1=new Intent();
                                                   intent1.putExtra("BathDescription", bath_desc);
                                                   intent1.putExtra("BathType",bath_type);
                                                   intent1.putExtra("BathRating",bath_rate);
                                                   setResult(101, intent1);*/
                                    finish();
                                } else {
                                    //   setProgress(false);

                                    // Uncomment and access the facebook user detail
                                    GraphRequest request = GraphRequest.newMeRequest(
                                            AccessToken.getCurrentAccessToken(),
                                            new GraphRequest.GraphJSONObjectCallback() {
                                                @Override
                                                public void onCompleted(
                                                        JSONObject object,
                                                        GraphResponse response) {
                                                    // Application code
                                                    try {
                                                        //   final ParseUser user = new ParseUser();
                                                        String name = object.getString("name").toString();
                                                        //String email = object.get;
                                                        Log.e("Detail", object.getString("name").toString());
                                                        parseUser.setUsername(name);
                                                        parseUser.put("name",name);
                                                        //  user.setEmail(email);
                                                        //user.put("name", name);
                                                        parseUser.saveInBackground();
                                                        Intent in = new Intent(getApplicationContext(), SearchLocationActivity.class);
                                                         startActivity(in);
                                                        finish();
                                                    } catch (JSONException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                    Log.e("Graph Response", response.toString());
                                                }
                                            });
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields", "name,email");
                                    Log.e("Parameter", parameters.toString());
                                    request.setParameters(parameters);
                                    request.executeAsync();
                                }
                            }
                        }
                    });
                }
                else {
                    alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.connection_not_available));
                }

            }
            else if(v== txtSkip)
            {
                Intent in = new Intent(getApplicationContext(), SearchLocationActivity.class);
                startActivity(in);
                finish();
            }
        }
    };

    private DialogInterface.OnClickListener mAppidCommitListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (mEtForgotPassword != null) {
                        if (mEtForgotPassword.length() <= 0) {
                            alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.enter_Confirmpassword));
                        }
                        else if(Utils.isInternetConnected(LoginActivity.this)) {

                            if (Utils.isValidEmailAddress(mEtForgotPassword.getText().toString())) {
                                ParseUser.requestPasswordResetInBackground(mEtForgotPassword.getText().toString(), new RequestPasswordResetCallback() {
                                    @Override
                                    public void done(com.parse.ParseException e) {
                                        if (e == null) {
                                            // An email was successfully sent with reset instructions.
                                            alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.reset_password));
                                        } else {
                                            // Something went wrong. Look at the ParseException to see what's up.
                                            alert.showAlertDialog(LoginActivity.this, e.getMessage());

                                        }
                                    }

                                });
                            } else {
                                alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.enter_email));
                            }
                        }
                        else
                        {
                            alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.connection_not_available));
                        }
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            // first time
            txtSkip.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();
        }
        return !ranBefore;
    }

    public void onLoginClick()
    {


      /*  if(edtEmail.getText().toString().trim().length() <=0)
        {
            alert.showAlertDialog(this,getResources().getString(R.string.enter_email));
        }
        else if(edtPassword.getText().toString().trim().length() <=0 || edtPassword.getText().toString().trim().length() <4)
        {
            alert.showAlertDialog(this,getResources().getString(R.string.enter_password));
        }else if (!Utils.isValidEmailAddress(edtEmail.getText().toString())) {
            alert.showAlertDialog(this, getResources().getString(R.string.enter_email));
        }
        else
        {*/
            email = edtEmail.getText().toString();
            password = edtPassword.getText().toString();
          //  final  ParseUser user = new ParseUser();

            ParseUser parseUser = new ParseUser();


            if (email.equals("")) {
                alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.enter_login_username));

            } else if (password.equals("")) {
                alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.enter_login_password));

            }
          else {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        setProgress(true);
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        ParseUser.logInInBackground(email, password, new LogInCallback() {
                                    @Override
                                    public void done(ParseUser parseUser, com.parse.ParseException e) {
                                        if (parseUser != null) {
                                            Boolean verify = parseUser.getBoolean("emailVerified");
                                            if (verify == false) {
                                                alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.verify_after_login));
                                            } else {
                                                // Hooray! The user is logged in.
                                                // Log.e("User login", "Sucess");
                                                Toast.makeText(getApplicationContext(), "You are logged in successfully.", Toast.LENGTH_SHORT).show();
                                                // edtEmail.setText("");
                                                // edtPassword.setText("");
                                                setProgress(false);

                                                if (getIntent().getExtras() != null) {
                                                   /*String  bath_desc = getIntent().getStringExtra("BathDescription");
                                                   String bath_type = getIntent().getStringExtra("BathType");
                                                   float bath_rate = getIntent().getFloatExtra("BathRating",0.0f);

                                                   Log.d("LoginActivity","get intent"+bath_desc+" "+bath_type+" "+bath_rate);

                                                   Log.d("LoginActivity", "  " + (getIntent().getExtras() != null));
                                                   Intent intent1=new Intent();
                                                   intent1.putExtra("BathDescription", bath_desc);
                                                   intent1.putExtra("BathType",bath_type);
                                                   intent1.putExtra("BathRating",bath_rate);
                                                   setResult(101, intent1);*/
                                                    finish();
                                                } else {
                                                    Intent in = new Intent(getApplicationContext(), SearchLocationActivity.class);
                                                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(in);
                                                    finish();
                                                    //Log.d("LoginActivity", "  " + "DashBoard");
                                                }

                                            }


                                        } else {
                                            // Signup failed. Look at the ParseException to see what happened.
                                            Log.e("Errorr", e.toString());
                                            alert.showAlertDialog(LoginActivity.this, getResources().getString(R.string.login_invalid));
                                        }
                                    }
                                }

                        );
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        setProgress(false);
                    }
                }.execute();



                }



     //   }
    }

    public void setProgress(boolean visibility) {
        if (visibility) {
            try {
                if (pd == null) {
                    pd = new ProgressDialog(getApplicationContext());
                    pd.setTitle("");
                    pd.setMessage(getString(R.string.loading));
                    pd.setCancelable(true);
                    if (!pd.isShowing())
                        pd.show();
                } else {
                    if (!pd.isShowing())
                        pd.show();
                }
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        } else {
            try {

                if (pd.isShowing())
                    pd.dismiss();
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

    }
}
