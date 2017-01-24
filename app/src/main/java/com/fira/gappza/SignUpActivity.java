package com.fira.gappza;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.SyncStateContract;
import android.support.v4.content.res.ResourcesCompat;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    //region Fields
    private Customer newCustomer = null;

    // Subviews and associates
    private Button back = null;

    private EditText firstNameEditText = null;
    private TextView firstNameErrorLabel = null;

    private EditText lastNameEditText = null;
    private TextView lastNameErrorLabel = null;

    private EditText phoneNumberEditText = null;
    private TextView phoneNumberErrorLabel = null;

    private EditText emailEditText = null;
    private TextView emailErrorLabel = null;

    private TextView userName = null;

    private EditText passwordEditText = null;
    private TextView passwordErrorLabel = null;

    private EditText confirmPasswordEditText = null;
    private TextView confirmPasswordErrorLabel = null;

    private CheckBox termsAndConditionsAndPrivacyCheckBox = null;

    private Button termsAndConditions = null;
    private Button privacyPolicy = null;

    private Button signUpButton = null;

    private ProgressBar spinner = null;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Create new Customer
        newCustomer = new Customer();

        addSubView();
        configureSubViews();

        // Get saved Customer after intenting Terms & Conditions or Privacy Policy Activities
        getCustomer();
    }

    /**
     *  Add subviews to the main view
     */
    private void addSubView(){
        // Add Back Button to Main View
        back = (Button)findViewById(R.id.backFromSignUpActivitytoLogInActivity_button);

        // Add Sign up Button to Main View
        signUpButton = (Button)findViewById(R.id.signUp_button);

        // Add First name EditText & First name Error TextView to Main View
        firstNameEditText = (EditText)findViewById(R.id.firstName_editText);
        firstNameErrorLabel = (TextView)findViewById(R.id.firstNameError_textView);

        // Add Last name EditText & Last name Error TextView to Main View
        lastNameEditText = (EditText) findViewById(R.id.lastName_editText);
        lastNameErrorLabel = (TextView)findViewById(R.id.lastNameError_textView);

        // Add Phone number EditText & Phone number Error TextView to Main View
        phoneNumberEditText = (EditText)findViewById(R.id.phoneNumber_editText);
        phoneNumberErrorLabel = (TextView)findViewById(R.id.phoneNumberError_textView);

        // Add Email EditText & Email Error TextView to Main View
        emailEditText = (EditText)findViewById(R.id.email_editText);
        emailErrorLabel = (TextView)findViewById(R.id.emailError_textView);

        // Add Password EditText & Password Error TextView to Main View
        passwordEditText = (EditText)findViewById(R.id.password_editText);
        passwordErrorLabel = (TextView)findViewById(R.id.passwordError_textView);

        // Add Confirm Password EditText & Confirm Password Error TextView to Main View
        confirmPasswordEditText = (EditText)findViewById(R.id.confirmPassword_editText);
        confirmPasswordErrorLabel = (TextView)findViewById(R.id.confirmPasswordError_textView);

        // Add Terms And Conditions & Privacy Policy CheckBox to Main View
        termsAndConditionsAndPrivacyCheckBox = (CheckBox)findViewById(R.id.termsAndConditions_checkbox);

        // Add Terms And Conditions Button to Main View
        termsAndConditions = (Button)findViewById(R.id.termsAndConditions_button);

        // Add Privacy And Policy Button to Main View
        privacyPolicy = (Button)findViewById(R.id.privacyPolicy_button);

        // Add Spinner ProgressBar to Main View
        spinner = (ProgressBar) findViewById(R.id.loading_progressBar_signUpActivity);
        spinner.getIndeterminateDrawable().setColorFilter(0xff80cbc4, android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    /**
     *  Configure subviews
     */
    private void configureSubViews(){
        // Configure Back Button
        backButtonConf();

        // Configure Sign up Button
        signUpButtonConf();

        // Configure First name EditText & First name ErrorLabel
        firstNameEditTextConf();

        // Configure Last name EditText & Last name ErrorLabel
        lastNameEditTextConf();

        // Configure Phone number EditText & Phone number ErrorLabel
        phoneNumberEditTextConf();

        // Configure Email EditText & Email ErrorLabel
        emailEditTextConfiguration();

        // Configure Password EditText & Password ErrorLabel
        passwordEditTextConfigurtion();

        // Configure Confirm Password EditText & Confirm Password ErrorLabel
        confirmPasswordConfiguration();

        // Configure Terms And Conditions & Privacy Policy CheckBox
        termsAndConditionsAndPrivacyPolicyCheckBoxConf();

        // Configure Terms & Conditions and Privacy Policy Button
        termsAndConditionsAndPrivacyPolicyButtonsConf();
    }

    //region Back Button Configuration
    /**
     *  Configure Back Button
     */
    private void backButtonConf(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logInActivityInent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(logInActivityInent);
                finish();
            }
        });
    }
    //endregion

    //region Sign up Button Configuration
    /**
     *  Configure Sign up Button
     */
    private void signUpButtonConf(){
        signUpButton.setEnabled(false);
        validateSignUpButton();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUserInteractionAndStartSpinner();
                new signUp().execute();
            }
        });
    }
    //endregion

    //region First name EditText Configuration
    /**
     *  Configure First name EditText & First name Error TextView
     */
    private void firstNameEditTextConf(){
        firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newCustomer.firstName = firstNameEditText.getText().toString();
                firstNameErrorLabel.setText(newCustomer.validateFirstName());
                validateSignUpButton();
            }
        });

        firstNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }
    //endregion

    //region Last name EditText Configuration
    /**
     * Configure Last name EditText & Last name Error TextView
     */
    private void lastNameEditTextConf(){
        lastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newCustomer.lastName = lastNameEditText.getText().toString();
                lastNameErrorLabel.setText(newCustomer.validateLastName());
                validateSignUpButton();
            }
        });

        lastNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }
    //endregion

    //region Phone number EditText Configuration
    /**
     *  Configure Phone number EditText & Phone number Error TextView
     */
    private void phoneNumberEditTextConf(){
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newCustomer.phoneNumber = phoneNumberEditText.getText().toString();
                phoneNumberErrorLabel.setText(newCustomer.validatePhoneNumber());
                validateSignUpButton();
            }
        });

        phoneNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }
    //endregion

    //region Email EditText Configuration
    /**
     *  Configure Email EditText & Email Error TextView
     */
    private void emailEditTextConfiguration(){
        userName = (TextView)findViewById(R.id.userName_textView);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newCustomer.email = emailEditText.getText().toString();
                emailErrorLabel.setText(newCustomer.validateEmail());
                if (newCustomer.validateEmail() == ""){
                    userName.setText(newCustomer.email);
                }
                validateSignUpButton();
            }
        });

        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }
    //endregion

    //region Password EditText Configuration
    /**
     *  Configure Password EditText & Password Error TextView
     */
    private void passwordEditTextConfigurtion(){
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newCustomer.password = passwordEditText.getText().toString();
                passwordErrorLabel.setText(newCustomer.validatePassword());
                validateSignUpButton();
            }
        });
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
     *  Configure Confirm Password EditText & Confirm Password Error TextView
     */
    private void confirmPasswordConfiguration(){
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newCustomer.confirmPassword = confirmPasswordEditText.getText().toString();
                confirmPasswordErrorLabel.setText(newCustomer.validateConfirmPassword());
                validateSignUpButton();
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

    //region EditTexts helpers
    /**
     *  Validate Sign up Button
     */
    private void validateSignUpButton(){
        if (newCustomer.validateCustomer().equals("")){
            signUpButton.setEnabled(true);
            signUpButton.setBackgroundResource(R.drawable.button_enabled);
        } else {
            signUpButton.setEnabled(false);
            signUpButton.setBackgroundResource(R.drawable.button_disabled);
        }
    }

    /**
     *  Hide keyboard if given view has no focus
     * @param view (EditText)
     */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    //endregion

    //region Terms and Conditions & Privacy Policy CheckBox Configuration
    /**
     *  Configure Terms And Conditions & Privacy Policy CheckBox
     */
    private void termsAndConditionsAndPrivacyPolicyCheckBoxConf(){
        termsAndConditionsAndPrivacyCheckBox = (CheckBox)findViewById(R.id.termsAndConditions_checkbox);
        termsAndConditionsAndPrivacyCheckBox.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (termsAndConditionsAndPrivacyCheckBox.isChecked()){
                    newCustomer.isTermsAndConditionChecked = true;
                    termsAndConditionsAndPrivacyCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_checked, null), null, null, null);
                } else {
                    newCustomer.isTermsAndConditionChecked = false;
                    termsAndConditionsAndPrivacyCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_unchecked, null), null, null, null);
                }
                validateSignUpButton();
            }
        });
    }
    //endregion

    //region Terms and Conditions & Privacy Policy Button Configuration
    /**
     *  Configure Terms & Conditions and Privacy Policy Button
     */
    private void termsAndConditionsAndPrivacyPolicyButtonsConf(){
        termsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent termsAndConditionsIntent = new Intent();
                Bundle b = new Bundle();
                b.putString("firstName", newCustomer.firstName.toString());
                b.putString("lastName", newCustomer.lastName.toString());
                b.putString("phoneNumber", newCustomer.phoneNumber.toString());
                b.putString("email", newCustomer.email.toString());
                b.putBoolean("isTermsAndConditionChecked", newCustomer.isTermsAndConditionChecked);
                termsAndConditionsIntent.putExtras(b);
                termsAndConditionsIntent.setClass(getApplicationContext(), TermsAndConditionsActivity.class);
                startActivity(termsAndConditionsIntent);
                finish();
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent privacyPolicyActivityInent = new Intent();
                Bundle b = new Bundle();
                b.putString("firstName", newCustomer.firstName.toString());
                b.putString("lastName", newCustomer.lastName.toString());
                b.putString("phoneNumber", newCustomer.phoneNumber.toString());
                b.putString("email", newCustomer.email.toString());
                b.putBoolean("isTermsAndConditionChecked", newCustomer.isTermsAndConditionChecked);
                privacyPolicyActivityInent.putExtras(b);
                privacyPolicyActivityInent.setClass(getApplicationContext(), PrivacyPolicyActivity.class);
                startActivity(privacyPolicyActivityInent);
                finish();
            }
        });
    }
    //endregion


    //region Get saved Customer
    /**
     *  Get saved Customer after intenting Terms & Conditions or Privacy Policy Activities
     */
    private void getCustomer() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            firstNameEditText.setText(bundle.getString("firstName"));
            lastNameEditText.setText(bundle.getString("lastName"));
            phoneNumberEditText.setText(bundle.getString("phoneNumber"));
            emailEditText.setText(bundle.getString("email"));
            newCustomer.isTermsAndConditionChecked = bundle.getBoolean("isTermsAndConditionChecked");
            if (newCustomer.isTermsAndConditionChecked){
                termsAndConditionsAndPrivacyCheckBox.setChecked(true);
                termsAndConditionsAndPrivacyCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_checked, null), null, null, null);
            } else {
                termsAndConditionsAndPrivacyCheckBox.setChecked(false);
                termsAndConditionsAndPrivacyCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_unchecked, null), null, null, null);
            }
            validateSignUpButton();
        }
    }
    //endregion


    //region API Calls
    /**
     *  Execute Sign up AsyncTask
     */
    private class signUp extends  AsyncTask<Void, Void, Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(Void... params) {
            jsonString = GappzaAPI.validateEmail(newCustomer.email);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");
                String message = jsonBody.getString("message");
                if (status.matches("1")){
                    new createAccount().execute();
                } else if(status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message, false);
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.", false);
                }

            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error ocured, please try again later.", true);
            }
        }
    }

    /**
     *  Execute Create account AsyncTask
     */
    private class createAccount extends  AsyncTask<Void, Void, Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(Void... params) {
            try{
                jsonString = GappzaAPI.cteateAccount(newCustomer);
            } catch (NoSuchAlgorithmException e){
                try {
                    jsonBody = new JSONObject("{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}");
                } catch (JSONException j){

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");
                String message = jsonBody.getString("message");
                if (status.matches("1")){
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message, true);
                } else if (status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message, false);
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.", false);
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
                firstNameEditText,
                lastNameEditText,
                phoneNumberEditText,
                emailEditText,
                passwordEditText,
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
                            Intent logInActivityIntent = new Intent();
                            logInActivityIntent.setClass(getApplicationContext(), LogInActivity.class);
                            startActivity(logInActivityIntent);
                            finish();
                        }
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
    //endregion
    //endregion



}
