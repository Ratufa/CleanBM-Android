package com.cleanbm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.Utils.Utils;
import com.adapter.PopupMenuAdapter;
import com.dialog.AlertDialogManager;
import com.iap.BillingHelper;
import com.javabeans.Popup_Menu_Item;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Kailash on 17-Sep-15.
 */
public class MyAccountActivity extends Activity implements View.OnClickListener {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private TextView txtUpgradeAccount;

    private Button saveChangeBtn;
    private TextView txt_Titlebar;
    PopupMenuAdapter adapter;
    PopupWindow popupWindow;
    private ImageView img_navigation_icon, img_Menu;
    private AlertDialogManager alert = new AlertDialogManager();

    private boolean fbUser = false;
    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == img_navigation_icon) {
                onBackPressed();
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount);

        img_navigation_icon = (ImageView) findViewById(R.id.img_navigation_icon);
        img_navigation_icon.setImageResource(R.drawable.back_icon);
        img_navigation_icon.setVisibility(View.VISIBLE);
        img_navigation_icon.setOnClickListener(mMenuButtonClickListener);

        txt_Titlebar = (TextView) findViewById(R.id.txt_Titlebar);
        txt_Titlebar.setText("My Account");

        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mNavigationClickListener);
        // initialize pop up window
        popupWindow = showMenu();

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });


        ParseUser currentUser = ParseUser.getCurrentUser();

        txtUpgradeAccount = (TextView) findViewById(R.id.txtUpgradeAccount);
        txtUpgradeAccount.setOnClickListener(this);

        edtName = (EditText) findViewById(R.id.edtName);
        edtName.setText(currentUser.getString("name"));

        edtEmail = (EditText) findViewById(R.id.edtEmail);

        saveChangeBtn = (Button) findViewById(R.id.saveChangeBtn);

        fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());

        edtEmail.setText(currentUser.getEmail());

        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtConfirmPassword = (EditText) findViewById(R.id.edtConfirmPassword);

       // startService(new Intent(MyAccountActivity.this, BillingService.class));

        BillingHelper.setCompletedHandler(mTransactionHandler);

        saveChangeBtn.setOnClickListener(this);

        if(fbUser){
            edtEmail.setEnabled(false);
            edtPassword.setEnabled(false);
            edtConfirmPassword.setEnabled(false);
        }else{
            edtEmail.setEnabled(true);
            edtPassword.setEnabled(true);
            edtConfirmPassword.setEnabled(true);
        }
    }

    View.OnClickListener mNavigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setActivated(!v.isActivated());
            if (popupWindow.isFocusable()) {
//                isClick = false;
                img_Menu.setImageResource(R.drawable.cancel_icon);
            } else {
//                isClick = true;
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
            popupWindow.showAsDropDown(v, -5, 0);


        }
    };

    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LayoutInflater inflater = (LayoutInflater) MyAccountActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[] = new Popup_Menu_Item[5];
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        Log.d("Splash screen "," "+email_verify);
        if (currentUser.getUsername() != null && email_verify==true) {
            menus = new Popup_Menu_Item[]{
                    new Popup_Menu_Item(R.drawable.home_icon, getResources().getString(R.string.Home)),
                    new Popup_Menu_Item(R.drawable.location_icon, getResources().getString(R.string.search_near_me)),
                    new Popup_Menu_Item(R.drawable.search_icon, getResources().getString(R.string.search_location)),
                    new Popup_Menu_Item(R.drawable.add_bathroom_icon, getResources().getString(R.string.add_new_location)),
                    new Popup_Menu_Item(R.drawable.support_icon, getResources().getString(R.string.support)),
                    new Popup_Menu_Item(R.drawable.login_icon, getResources().getString(R.string.my_account)),
                    new Popup_Menu_Item(R.drawable.sign_out_button, getResources().getString(R.string.Logout)),
            };
        } else {
            menus = new Popup_Menu_Item[]{
                    new Popup_Menu_Item(R.drawable.home_icon, getResources().getString(R.string.Home)),
                    new Popup_Menu_Item(R.drawable.location_icon, getResources().getString(R.string.search_near_me)),
                    new Popup_Menu_Item(R.drawable.search_icon, getResources().getString(R.string.search_location)),
                    new Popup_Menu_Item(R.drawable.add_bathroom_icon, getResources().getString(R.string.add_new_location)),
                    new Popup_Menu_Item(R.drawable.support_icon, getResources().getString(R.string.support)),
                    new Popup_Menu_Item(R.drawable.login_icon, getResources().getString(R.string.Login_menu))
            };
        }

        Log.e("Array size", menus.length + " ");
        adapter = new PopupMenuAdapter(MyAccountActivity.this, R.layout.popup_menu_item, menus);
        Log.e("After adapter", menus.length + " ");
        //the drop down list in a listview
        ListView lstMenu = new ListView(MyAccountActivity.this);
        //ListView lstMenu= (ListView)findViewById(R.id.listView1);
        // lstMenu.setVisibility(View.VISIBLE);

        // set our adapter and pass our pop up window content
        lstMenu.setAdapter(adapter);
        // adapter.notifyDataSetChanged();
        lstMenu.setDivider(getResources().getDrawable(R.drawable.menu_line));
        // lstMenu.setCacheColorHint(Color.TRANSPARENT);
        lstMenu.setAlpha(.93f);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        fadeInAnimation.setDuration(10);
        lstMenu.startAnimation(fadeInAnimation);

        // set the item click listener
        lstMenu.setOnItemClickListener(new DropdownOnItemClickListener());

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        // popupWindow.setWidth(width);
        //  popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        popupWindow.setContentView(lstMenu);
        return popupWindow;
    }


    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {

        String TAG = "MyAccountActivity.java";

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {

            // get the context and main activity to access variables
            Context mContext = v.getContext();
            MyAccountActivity mainActivity = ((MyAccountActivity) mContext);

            // add some animation when a list item was clicked
            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setDuration(10);
            v.startAnimation(fadeInAnimation);

            // img_Menu.setImageResource(R.drawable.cancel_icon);
            img_Menu.setImageResource(R.drawable.menu_icon);
            // dismiss the pop up
            mainActivity.popupWindow.dismiss();

            Popup_Menu_Item popup_menu_item = new Popup_Menu_Item();
            // String data=popup_menu_item.title;
            // String data=parent.getItemAtPosition(position).toString();

            Popup_Menu_Item info = (Popup_Menu_Item) parent.getItemAtPosition(position);
            String data = info.title;

            Log.e("Tag", data);
            if (data.equals(getString(R.string.Home))) {
                finish();
            } else if (data.equals(getString(R.string.search_near_me))) {
                if (Utils.isInternetConnected(MyAccountActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    alert.showAlertDialog(MyAccountActivity.this, getResources().getString(R.string.connection_not_available));
                }

            } else if (data.equals(getString(R.string.search_location))) {
                Intent intent = new Intent(getApplicationContext(), SearchAdvanceActivity.class);
                //  startActivityForResult(intent, 101);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.add_new_location))) {
                Intent intent = new Intent(getApplicationContext(), AddNewLocationActivity.class);
                //   startActivity(intent);
                startActivityForResult(intent, 777);
                finish();
            } else if (data.equals(getString(R.string.support))) {
                Intent intent = new Intent(getApplicationContext(), SupportActivity.class);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.my_account))) {
                Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.Logout))) {
                if (Utils.isInternetConnected(MyAccountActivity.this)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyAccountActivity.this);
                    // Setting Dialog Message
                    alertDialog.setMessage("Do you want to Logout?");
                    // Setting Positive "Yes" Button
                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ParseUser currentUser = ParseUser.getCurrentUser();
                                    if (currentUser.getUsername() != null) {
                                        ParseUser.logOut();
                                        popupWindow = showMenu();
                                        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                            @Override
                                            public void onDismiss() {
                                                img_Menu.setImageResource(R.drawable.menu_icon);

                                            }
                                        });
                                    }
                                }
                            });
                    // Setting Negative "NO" Button
                    alertDialog.setNegativeButton("CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Write your code here to invoke NO event
                                    dialog.cancel();
                                }
                            });
                    // Showing Alert Message
                    alertDialog.show();

                } else {
                    alert.showAlertDialog(MyAccountActivity.this, getResources().getString(R.string.connection_not_available));
                }
            } else if (data.equals(getResources().getString(R.string.Login_menu))) {
                //   flag_for_login = 1;
                Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(in);
                finish();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
//        BillingHelper.stopService();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtUpgradeAccount:
               /* if (BillingHelper.isBillingSupported()) {
                 //   BillingHelper.requestPurchase(MyAccountActivity.this, "android.test.purchased");
                } else {
                    Log.i(TAG, "Can't purchase on this device");
                    //btn2.setEnabled(false); // XXX press button before service started will disable when it shouldnt
                }
                Toast.makeText(this, "Upgrade Button", Toast.LENGTH_SHORT).show();*/
                showUpgradeDialog();
                break;

            case R.id.saveChangeBtn:
                String name = edtName.getText().toString().trim();
                String pwd = edtPassword.getText().toString().trim();
                String confirmPwd = edtConfirmPassword.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();

                if(!fbUser){

                if (name.length() != 0 && pwd.length() != 0 && confirmPwd.length() != 0 && email.length() != 0) {

                    if (pwd.equals(confirmPwd))
                        saveData(name, pwd, email);
                    else
                        Toast.makeText(getApplicationContext(), "Password not matched", Toast.LENGTH_SHORT).show();

                } else {
                    if (name.length()==0){
                        Toast.makeText(getApplicationContext(), "Name not specified", Toast.LENGTH_SHORT).show();
                    }else  if (pwd.length()==0){
                        Toast.makeText(getApplicationContext(), "Password not specified", Toast.LENGTH_SHORT).show();
                    }else  if (confirmPwd.length()==0){
                        Toast.makeText(getApplicationContext(), "Password not matched", Toast.LENGTH_SHORT).show();
                    }else  if (email.length()==0){
                        Toast.makeText(getApplicationContext(), "Email not specified", Toast.LENGTH_SHORT).show();
                    }

                }
                }else {
                    if (name.length() != 0) {
                            saveData(name, pwd, email);

                    } else {
                        Toast.makeText(getApplicationContext(), "Name not specified", Toast.LENGTH_SHORT).show();
                    }
                }


                break;
            default:
                break;
        }
    }

    private String TAG = getClass().getSimpleName();

    public Handler mTransactionHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.i(TAG, "Transaction complete");
            Log.i(TAG, "Transaction status: " + BillingHelper.latestPurchase.purchaseState);
            Log.i(TAG, "Item purchased is: " + BillingHelper.latestPurchase.productId);

            if (BillingHelper.latestPurchase.isPurchased()) {
                showItem();
            }
        }
    };

    private void showItem() {
        //purchaseableItem.setVisibility(View.VISIBLE);
    }

    private void saveData(String name, String password, String email) {

        ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.put("name", name);
        if(!fbUser) {
            parseUser.put("password", password);
            parseUser.put("email", email);
            parseUser.put("username", email);
        }
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(getApplicationContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void showUpgradeDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Upgrade account");

        // set dialog message
        alertDialogBuilder
                .setMessage("Would you like to upgrade your profile to Premium?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                     dialog.dismiss();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }




}
