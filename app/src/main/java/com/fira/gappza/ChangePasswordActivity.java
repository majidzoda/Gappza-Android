package com.fira.gappza;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ChangePasswordActivity extends AppCompatActivity {

    //region Fields
    private ChangePassword changePassword;

    // Saving directory name
    private final String PREFS_NAME = "MyPrefsFile";

    // Subviews and associates
    private Button backButton;

    private TextView currentPasswordErrorLabel;
    private EditText currentPasswordEditText;

    private TextView newPasswordErrorLabel;
    private EditText newPasswordEditText;

    private TextView confirmPasswordErrorLabel;
    private EditText confirmPasswordEditText;

    private Button submitButton;

    private ProgressBar spinner;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        changePassword = new ChangePassword();

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
        // Add Back Button to Main View
        backButton = (Button)findViewById(R.id.backChangePasswordActivitytoSettingsActivity_button);

        // Add Current Password EditText and Current Password Error TextView to Main View
        currentPasswordErrorLabel = (TextView)findViewById(R.id.currentPasswordError_textViewchangePasswordActivity);
        currentPasswordEditText  = (EditText)findViewById(R.id.currentPassword_editTextchangePasswordActivity);

        // Add New Password EditText and New Password Error TextView to Main View
        newPasswordErrorLabel = (TextView)findViewById(R.id.newPasswordError_textViewchangePasswordActivity);
        newPasswordEditText = (EditText)findViewById(R.id.newtPassword_editTextchangePasswordActivity);

        // Add Confirm Password EditText and Confirm Password Error TextView to Main View
        confirmPasswordErrorLabel = (TextView)findViewById(R.id.confirmPasswordError_textViewchangePasswordActivity);
        confirmPasswordEditText = (EditText)findViewById(R.id.confirmPassword_editTextchangePasswordActivity);

        // Add Submit Button to Main View
        submitButton = (Button)findViewById(R.id.submit_button_changePasswordActivity);
        submitButton.setEnabled(false);

        // Add Spinner ProgressBar to Main View
        spinner = (ProgressBar)findViewById(R.id.loading_progressBar_changePasswordActivity);
    }

    /**
     *  Configure subviews
     */
    private void configureSubViews(){
        // Configure Back Button
        backButtonConf();

        // Configure Current Password EditText
        currnetPasswordConf();

        // Configure New Password EditText
        newPasswordConf();

        // Configure Confirm Password Edittext
        confirmPasswordConf();

        // Configure Submit Button
        submitButtonConf();
    }

    //region Back Button Configuration
    /**
     * Configure Back Button
     */
    private void backButtonConf(){
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsActivityIntent);
                finish();
            }
        });
    }
    //endregion

    //region Current Password EditText Configuration
    /**
     * Configure Current Password EditText
     */
    private void currnetPasswordConf(){
        currentPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                changePassword.currentPassword = currentPasswordEditText.getText().toString();
                currentPasswordErrorLabel.setText(changePassword.validateCurrentPassword());
                updateSubmitButton();
            }

        });

        currentPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }
    //endregion

    //region New Password EditText Configuratoin
    /**
     * Configure New Password EditText
     */
    private void newPasswordConf(){
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                changePassword.newPassword = newPasswordEditText.getText().toString();
                newPasswordErrorLabel.setText(changePassword.validateNewPassword());
                updateSubmitButton();
            }
        });

        newPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });

    }
    //endregion

    //region Confirm Password EditText Configuration
    /**
     * Configure Confirm Password EditText
     */
    private void confirmPasswordConf(){
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                changePassword.confirmNewPassword = confirmPasswordEditText.getText().toString();
                confirmPasswordErrorLabel.setText(changePassword.validateConfirmNewPassword());
                updateSubmitButton();
            }
        });

        confirmPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }
    //endregion

    //region EditText Helper
    /**
     *  Hide keyboard if given view has no focus
     * @param view (EditText)
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    //endregion

    //region Submit Button Configuration
    /**
     * Configure Submit Button
     */
    private void submitButtonConf(){
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUserInteractionAndStartSpinner();
                new changePassword().execute();
            }
        });
    }

    //region Submit Button Validation
    /**
     * Updates Submit Button validation state
     */
    private void updateSubmitButton(){
        Log.d("updateSubmitButton", "YES");
        if (changePassword.validateChangePassword().matches("")){
            submitButton.setEnabled(true);
            submitButton.setBackgroundResource(R.drawable.button_enabled);
        } else {
            submitButton.setEnabled(false);
            submitButton.setBackgroundResource(R.drawable.button_disabled);
        }
    }
    //endregion
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
     * Execute Change Password AsyncTask
     */
    private class changePassword extends AsyncTask<String,Void,Void> {
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.changePassword(loadCurrentUserEmail(),changePassword.newPassword);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");
                String message = jsonBody.getString("message");
                if (status.matches("1")){
                    saveFingerPrintValue();
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message, true);
                } else if (status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message, true);
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.", true);
                }

            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.", true);
            }

        }
    }

    //region API Call Helpers
    //region Spinner ProgressBar Actions
    /**
     *  Stop user interactions and Start Spinner animating
     */
    private void stopUserInteractionAndStartSpinner(){

        EditText[] anArray = {
                currentPasswordEditText,
                newPasswordEditText,
                confirmPasswordEditText
        };

        for (EditText object: anArray){
            if (object.hasFocus()){
                hideKeyboard(object);
            }
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        spinner.setVisibility(View.VISIBLE);
    }

    /**
     *  Start user interactions and stop Spinner animation
     */
    private void startUserInteractionAndStopSpinner(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        spinner.setVisibility(View.GONE);
    }
    //endregion

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

    /**
     * Loads current user's email
     * @return email string
     */
    public String loadCurrentUserEmail(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString("currentUserEmail", "");
        return value;
    }

    /**
     * Save Fingerprint values, boolean to false and Fingerprint associated email to "" after password change
     */
    public void saveFingerPrintValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFingerPrint", false);
        editor.putString("fingetPrintEmail", "");
        editor.commit();
    }
    //endregion
    //endregion
}
