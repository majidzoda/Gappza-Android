package com.fira.gappza;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class AccountActivity extends FragmentActivity {

    //region Fields
    // Saving directory name
    private final String PREFS_NAME = "MyPrefsFile";

    // Email and user Firs name
    private String email;
    private TextView userFirstNameTextView = null;
    private String currentUserEmail;


    // TransactionList Fragment
    private FragmentManager fm;
    private FragmentTransaction ft;
    private TransactionListFragment transactionListFragment;
    private final String TAG_FRAGMENT = "TransactionListFragment";

    // Subviews
    private ImageButton settingsButton;
    private Button makePaymentButton;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        addSubViews();
        configureSubViews();

        startUserActiveTime();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check user inactive time and log out if it is greater than 15 mins
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        long value = settings.getLong("time", 0);

        if (value != 0){
            Date resumeDate = new Date();
            long diff = resumeDate.getTime() - value;
            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (diffMinutes >= 15){
                Intent logInActivity = new Intent();
                Bundle b = new Bundle();
                b.putBoolean("loggedOut", true);
                logInActivity.putExtras(b);
                logInActivity.setClass(getApplicationContext(), LogInActivity.class);
                startActivity(logInActivity);
                finish();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        commitFragment();
        firstNameTextViewConf();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Seek user inactivity time
        Date myDate = new Date();
        long time = myDate.getTime();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("time", time);
        editor.commit();
    }

    /**
     *  Add subviews to the main view
     */
    private void addSubViews(){
        // Add Make payment Button to Main View
        makePaymentButton = (Button)findViewById(R.id.makePayment_button);

        // Add Settings Button to Main View
        settingsButton = (ImageButton)findViewById(R.id.settings_imageButton);
    }

    /**
     *  Configure subviews
     */
    private void configureSubViews(){
        // Configure Make payment Button
        configureMakePaymentButton();

        // Configure Settings Button
        configureSettingsButton();
    }

    //region Make payment Button Configuration
    /**
     * // Configure Make payment Button
     */
    private void configureMakePaymentButton(){
        makePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent makePaymentActivity = new Intent(getApplicationContext(), MakePaymentActivity.class);
                startActivity(makePaymentActivity);
                finish();
            }
        });
    }
    //endregion

    //region Settings Button Configuration
    /**
     *  Configure Settings Button
     */
    private void configureSettingsButton(){
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsActivity);
                finish();


            }
        });
    }
    //endregion

    //region Start user actyvity time
    /**
     *  Set user active time to 0
     */
    private void startUserActiveTime(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("time", 0);
        editor.commit();
    }
    //endregion


    //region User First name TextView Configuration
    //region Configure User First name
    /**
     * Configure User first name
     */
    public void firstNameTextViewConf(){
        userFirstNameTextView = (TextView)findViewById(R.id.accountFirstName_textView);
        email = loadCurrentUserEmail();
        currentUserEmail = email;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            new getUserFirstName().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new getUserFirstName().execute();
        }
    }
    //endregion


    //region API Call
    /**
     *  Execute Get user first name AsyncTask
     */
    private class getUserFirstName extends AsyncTask<String,Void,Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.getUserFirstName(email);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");
                if (status.matches("1")){
                    String message = jsonBody.getString("message");
                    saveUserFirstName(message);
                    userFirstNameTextView.setText(message);
                } else if (status.matches("-1")){
                    alertUserWithMessage("Unexpected error occurred, please try again later.", true);
                } else {
                    alertUserWithMessage("Unexpected error occurred, please try again later.", true);
                }

            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.", true);
            }
        }
    }
    //endregion

    //region Load current user email
    /**
     * Load current user email
     * @return email string
     */
    public String loadCurrentUserEmail(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString("currentUserEmail", "");
        return value;
    }
    //endregion

    //region Save curent user First name
    /**
     * Save user First name
     * @param name - User First name
     */
    public void saveUserFirstName(String name){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userFirstName", name);
        editor.commit();
    }
    //endregion
    //endregion


    //region TransactionList Fragment Commit
    /**
     *  Add and commit TransactionList Fragment
     */
    private void commitFragment(){
        RelativeLayout fragmentLayout = new RelativeLayout(getApplicationContext());
        fragmentLayout.setId(R.id.fragment_container);

        RelativeLayout.LayoutParams fragmentLayoutParam = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        fragmentLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        fragmentLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL);

        fragmentLayout.setLayoutParams(fragmentLayoutParam);
        RelativeLayout outterLayout = (RelativeLayout)findViewById(R.id.fragment_containerOutter);
        outterLayout.removeAllViewsInLayout();
        outterLayout.addView(fragmentLayout);

        fm = getSupportFragmentManager();
        transactionListFragment = (TransactionListFragment) fm.findFragmentByTag(TAG_FRAGMENT);

        if (transactionListFragment == null){
            transactionListFragment = new TransactionListFragment();
            fm.beginTransaction().add(R.id.fragment_container, transactionListFragment, TAG_FRAGMENT).addToBackStack(TAG_FRAGMENT).commit();

        } else {
            transactionListFragment = new TransactionListFragment();
            fm.beginTransaction().replace(R.id.fragment_container, transactionListFragment, TAG_FRAGMENT).addToBackStack(TAG_FRAGMENT).commit();
        }
    }
    //endregion

    //region Alert user with dialog message
    /**
     * Alert user with a dialog message
     * @param message to be viewed to user
     * @param isIntent true/false for Intent to LogInActivity.java
     */
    private void alertUserWithMessage(String message, final boolean isIntent){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        if (isIntent){
                            Intent logInActivity = new Intent(getApplicationContext(), LogInActivity.class);
                            startActivity(logInActivity);
                            finish();
                        }
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
    //endregion

}
