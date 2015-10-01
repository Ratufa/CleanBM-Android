package com.cleanbm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
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
import com.iap.BillingService;
import com.javabeans.Popup_Menu_Item;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    String inappId= "com.cleanbm.premium";


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

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


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
        txtUpgradeAccount.setPaintFlags(txtUpgradeAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtUpgradeAccount.setOnClickListener(this);

        edtName = (EditText) findViewById(R.id.edtName);
        edtName.setText(currentUser.getString("name"));

        edtEmail = (EditText) findViewById(R.id.edtEmail);

        saveChangeBtn = (Button) findViewById(R.id.saveChangeBtn);

        fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());

        edtEmail.setText(currentUser.getEmail());

        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtConfirmPassword = (EditText) findViewById(R.id.edtConfirmPassword);

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                Log.d(TAG," Log4 ");
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                Log.d(TAG," Log5 ");
                mService = IInAppBillingService.Stub.asInterface(service);

                Log.d(TAG," Log6 ");
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        Log.d(TAG," Log1 ");
        serviceIntent.setPackage("com.android.vending");
        Log.d(TAG, " Log2 ");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        Log.d(TAG, " Log3 ");


        //startService(new Intent(MyAccountActivity.this, BillingService.class));

     //   BillingHelper.setCompletedHandler(mTransactionHandler);

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
        fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
        Log.d("Splash screen "," "+email_verify);
        if ((currentUser.getUsername() != null && email_verify==true) || fbUser){
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
                in.putExtra("BathDescription", "");
                startActivity(in);
                finish();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            Log.d(TAG, "Log 20"+responseCode+" "+purchaseData+" "+dataSignature ); // 4 null null

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Log.d(TAG, "Log 21"+sku );
                    alert.showAlertDialog(MyAccountActivity.this,"You have bought the " + sku + ". Excellent choice,adventurer!");
                }
                catch (JSONException e) {
                    alert.showAlertDialog(MyAccountActivity.this, "Failed to parse purchase data.");
                    e.printStackTrace();
                }
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
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

  /*  @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        popupWindow = showMenu();

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });
    }*/



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtUpgradeAccount:
                showUpgradeDialog();


              /*  if (BillingHelper.isBillingSupported()) {
                    BillingHelper.requestPurchase(MyAccountActivity.this, "com.cleanbm.premium");
                } else {
                    Log.i(TAG, "Can't purchase on this device");
                    //btn2.setEnabled(false); // XXX press button before service started will disable when it shouldnt http://stackoverflow.com/questions/6352150/in-app-billing-trouble-with-pending-intents-and-switching-activities
                }*/
             //   Toast.makeText(this, "Upgrade Button", Toast.LENGTH_SHORT).show();

                break;

            case R.id.saveChangeBtn:
                String name = edtName.getText().toString().trim();
                String pwd = edtPassword.getText().toString().trim();
                String confirmPwd = edtConfirmPassword.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();

                if(!fbUser){

                if (name.length() != 0 && pwd.length() != 0  && confirmPwd.length() != 0 && email.length() != 0) {

                    if (pwd.equals(confirmPwd))
                        saveData(name, pwd, email);
                    else
                        alert.showAlertDialog(this, getResources().getString(R.string.passwordmismatch));
                } else {
                    if (name.length() <= 0) {
                        alert.showAlertDialog(this, getResources().getString(R.string.enter_name));
                    } else if (email.length() <= 0) {
                        alert.showAlertDialog(this, getResources().getString(R.string.enter_email));
                    } else if (!Utils.isValidEmailAddress(email)) {
                        alert.showAlertDialog(this, getResources().getString(R.string.enter_email));
                    }else  if (pwd.length()<=0){
                        alert.showAlertDialog(this, getResources().getString(R.string.enter_Confirmpassword));
                    }else if (pwd.length() < 4) {
                        alert.showAlertDialog(this, getResources().getString(R.string.enter_password));
                    } else if (confirmPwd.length() <= 0) {
                        alert.showAlertDialog(this, getResources().getString(R.string.enter_Confirmpassword));
                    } else if (!(name.equals(confirmPwd))) {
                            alert.showAlertDialog(this, getResources().getString(R.string.passwordmismatch));
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
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
       // purchaseableItem.setVisibility(View.VISIBLE);
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
                        ArrayList<String> skuList = new ArrayList<String> ();
                        Log.d(TAG,"Log 11");
                        skuList.add(inappId);
                        Log.d(TAG, "Log 12"+inappId+skuList.size());
                        // skuList.add("gas");
                        Bundle querySkus = new Bundle();
                        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                        Log.d(TAG, "Log 13");
                        try {
                            Bundle skuDetails = mService.getSkuDetails(3,
                                    getPackageName(), "subs", querySkus);

                   /* Bundle skuDetails = mService.getBuyIntent(3, getPackageName(),
                            inappId, "subs","bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");*/
                            Log.d(TAG, "Log 14"+(skuDetails==null) );
                            int response = skuDetails.getInt("RESPONSE_CODE");
                            Log.d(TAG, "Log 15"+response );
                            if (response == 0) {
                                Log.d(TAG, "Log 16" );
                                ArrayList<String> responseList
                                        = skuDetails.getStringArrayList("DETAILS_LIST");
                                Log.d(TAG, "Log 17"+responseList.size()+" "+responseList.toString() );

                                for (String thisResponse : responseList) {
                                    JSONObject object = new JSONObject(thisResponse);
                                    String sku = object.getString("productId");
                                    String price = object.getString("price");
                                    Log.d(TAG, "Log 18"+sku+" "+price );
                                    if (sku.equals(inappId))
                                    {
                                        System.out.println("Price"+price);
                                        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                                sku, "subs", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                                        //mPremiumUpgradePrice = price;
                                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                        Log.d(TAG, "Log 19" + sku + " " + price);
                                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                                Integer.valueOf(0));
                                        Log.d(TAG, "Log 20" + sku + " " + price);


                                    }

                                }
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
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
