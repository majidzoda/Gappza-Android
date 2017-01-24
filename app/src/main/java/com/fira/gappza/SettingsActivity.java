package com.fira.gappza;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    //region Fields
    // Saving directory name
    private final String PREFS_NAME = "MyPrefsFile";

    // Subviews
    private Switch fingerPringSwitch;
    private boolean isFingerPrint = false;
    private LinearLayout mainLayout;

    private TextView email;

    private Button changePassword;
    private Button deleteAccount;
    private Button logOutButton;
    private ImageButton accountSettingsActivity;
    private ProgressBar spinner;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        // Add Email TextView to Main View and load user's email
        email = (TextView)findViewById(R.id.email_textView_settingsActivity);
        email.setText(loadCurrentUserEmail());

        // Add Change Password Button to Main View
        changePassword = (Button)findViewById(R.id.changePassword_button);

        // Add Delete Account Button to Main View
        deleteAccount = (Button)findViewById(R.id.deleteAccount_button);

        // Add Log out Button to Main View
        logOutButton = (Button)findViewById(R.id.logOut_button);

        // Add Account Button to Main View
        accountSettingsActivity = (ImageButton)findViewById(R.id.account_imageButton_settingsActivity);

        // Add Spinner ProgressBar to Main View
        spinner = (ProgressBar)findViewById(R.id.loading_progressBar_settingsActivity);
    }

    /**
     *  Configure subviews
     */
    private void configureSubViews(){
       // Configure Fingerprint TextView and Switch
        fingerPrintConf();

        // Configure Change Password, Delete Account, Log Out and Account Buttons
        buttonsConf();
    }

    //region Configure Fingerprint TextView and Switch
    /**
     * Configure Fingerprint TextView and Switch if Device supports Fingerprint sensor
     */
    private void fingerPrintConf() {
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (fingerprintManager.isHardwareDetected()){
            fingerPringSwitch = new Switch(this);
            fingerPringSwitch.setId(R.id.finger_print_switch);
            fingerPringSwitch.setEnabled(true);

            ColorStateList buttonStates = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{}
                    },
                    new int[]{
                            Color.RED,
                            Color.BLUE,
                            Color.GREEN
                    }
            );

            fingerPringSwitch.setButtonTintList(buttonStates);

            TextView fingerPrintLabel = new TextView(this);
            fingerPrintLabel.setId(R.id.fingerPrint_textView);
            fingerPrintLabel.setText("Fingerprint   ");
            fingerPrintLabel.setTextColor(Color.WHITE);


            mainLayout = (LinearLayout)findViewById(R.id.fingerPrint_layout);

            RelativeLayout.LayoutParams textViewParam = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            textViewParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            RelativeLayout.LayoutParams fingerPrintSwitchParam = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            fingerPrintSwitchParam.addRule(RelativeLayout.RIGHT_OF, R.id.fingerPrint_textView);

            fingerPrintLabel.setLayoutParams(textViewParam);
            fingerPringSwitch.setLayoutParams(fingerPrintSwitchParam);

            mainLayout.addView(fingerPrintLabel);
            mainLayout.addView(fingerPringSwitch);

            loadFingerPrintButtonValue();

            fingerPringSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFingerPrint){
                        isFingerPrint = false;
                        fingerPringSwitch.setChecked(false);
                        saveFingerPrintValue(isFingerPrint, "");
                    } else {
                        isFingerPrint = true;
                        fingerPringSwitch.setChecked(true);
                        saveFingerPrintValue(isFingerPrint, loadCurrentUserEmail());
                    }
                }
            });

        }
    }

    /**
     * Load Fingerprint value, boolean and current user's email to set the Fingerprint Switch state
     */
    private void loadFingerPrintButtonValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean value = settings.getBoolean("isFingerPrint", false);
        String email = settings.getString("fingetPrintEmail", "");

        if (value && email.matches(loadCurrentUserEmail())){
            isFingerPrint = true;
            fingerPringSwitch.setChecked(true);

        } else {
            isFingerPrint = false;
            fingerPringSwitch.setChecked(false);
        }
    }

    /**
     * Save Fingerprint value, boolean for switch and associated current user's email
     * @param is - if true save current user's email, otherwise save it as ""
     * @param email - user's email
     */
    private void saveFingerPrintValue(boolean is, String email){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFingerPrint", is);
        if (isFingerPrint){
            editor.putString("fingetPrintEmail", email);
        } else {
            editor.putString("fingetPrintEmail", "");
        }
        editor.commit();
    }
    //endregion

    //region Configure Change Password, Delete Account, Log Out and Account Buttons
    /**
     * // Configure Change Password, Delete Account, Log Out and Account Buttons
     */
    private void buttonsConf(){
        accountSettingsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountActivityIntent = new Intent();
                accountActivityIntent.setClass(getApplicationContext(), AccountActivity.class);
                startActivity(accountActivityIntent);
                finish();
            }
        });


        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePasswordActivityIntent = new Intent();
                changePasswordActivityIntent.setClass(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePasswordActivityIntent);
                finish();
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountMessage();
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent logInActivityIntent = new Intent();
                logInActivityIntent.setClass(getApplicationContext(), LogInActivity.class);
                startActivity(logInActivityIntent);
                finish();
            }
        });


    }

    /**
     * Alert user with a dialog message to warn for Account delition
     */
    public void deleteAccountMessage(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setMessage("Are you sure you want to delete your account?")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        stopUserInteractionAndStartSpinner();
                        new deleteAccount().execute();
                    }
                }).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

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

    //region API Call
    /**
     * Execute Delete account AsyncTask
     */
    public class deleteAccount extends AsyncTask<Void, Void, Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(Void... params) {
            jsonString = GappzaAPI.deleteAccount(loadCurrentUserEmail());
            return null;
        }

        @Override
        protected  void onPostExecute(Void aVoid){
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");
                if (status.matches("1")){
                    String message = jsonBody.getString("message");
                    saveFingerPrintValue();
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message, true);
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

    //region API Call helpers
    /**
     * Save Fingerprint value, boolean to false for Fingerprint switch and "" for Fingerprint email, to Delete Account
     */
    private void saveFingerPrintValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFingerPrint", false);
        editor.putString("fingetPrintEmail", "");
        editor.commit();
    }

    //region Spinner ProgressBar Actions
    /**
     *  Stop user interactions and Start Spinner animating
     */
    public void stopUserInteractionAndStartSpinner(){

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        spinner.setVisibility(View.VISIBLE);
    }

    /**
     *  Start user interactions and stop Spinner animation
     */
    public void startUserInteractionAndStopSpinner(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        spinner.setVisibility(View.GONE);
    }
    //endregion

    /**
     * Alert user with a dialog message
     * @param message to be viewed to user
     * @param isIntent true/false for Intent to LogInActivity.java
     */
    public void alertUserWithMessage(String message, final boolean isIntent){
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

    //region Load current user's email
    /**
     * Loads current user's email
     * @return email string
     */
    public String loadCurrentUserEmail(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString("currentUserEmail", "");
        return value;
    }
    //endregion

}





