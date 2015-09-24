package com.cleanbm;


import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;

import com.parse.ParseUser;


public class SplashActivity extends BaseActivityy {

    // Splash screen timer
    private static  int SPLASH_TIME_OUT = 3000;
    LinearLayout splash_Screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);


       /* splash_Screen = (LinearLayout)findViewById(R.id.splash_Screen);
        splash_Screen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                //  int action = event.getAction();
                //  switch(action){
                //   case MotionEvent.ACTION_DOWN:
                Intent in = new Intent(getApplicationContext(), DashBoardActivity.class);
                startActivity(in);
                //startActivity(new Intent(SplashActivity.this, DashBoardActivity.class));
                finish();
                    //    break;
                //}
                return true;
            }
        });*/

    }

    @Override
    protected void onStart() {
        super.onStart();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            new Handler().postDelayed(new Runnable() {


                @Override
                public void run()
                {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    ParseUser curreUser = ParseUser.getCurrentUser();
                    Boolean email_verify = curreUser.getBoolean("emailVerified");
                    Log.d("Splash screen "," "+email_verify);
                    if(curreUser.getUsername()==null || email_verify==false) {
                        Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(in);
                        finish();
                    }
                    else
                    {
                        Intent in = new Intent(getApplicationContext(), SearchLocationActivity.class);
                        startActivity(in);
                        finish();
                    }
                }
                // close this activity
                // }
            }, SPLASH_TIME_OUT);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
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
}
