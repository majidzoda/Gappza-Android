package com.fira.gappza;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class LogInActivity extends AppCompatActivity {

    //region Fields
    // Saving directory name
    private final String PREFS_NAME = "MyPrefsFile";

    // Subviews and associates
    private EditText emailTextEdit = null;
    private boolean isEmailValid = false;

    private CheckBox rememberMeCheckBox = null;
    private boolean isRememberMeCheckBox = false;

    private EditText passwordTextEdit = null;
    private boolean isPasswordValid = false;

    private Button forgotPasswordButton = null;

    private Button logInButton = null;
    private Button registerButton = null;

    private RelativeLayout mainLayout= null;

    private TextView fingerPrintLabel = null;
    private ImageButton fingerPrint = null;

    // Fingerprint details
    static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    private boolean isFingerPrint = false;

    private ProgressBar spinner = null;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        addSubViews();
        configureSubviews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Configure Fingerprint onResume if user configured phone's Fingerprint settings while Gappza was running
        fingerPrintConf();

        // Check bundle to inform user if he/she was logged out based on staying logged in longer than 15 minutes
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            boolean value = bundle.getBoolean("loggedOut", false);

            if (value){
                alertUserWithMessage("We logged you out for security purposes.");
            }
        }

    }

    /**
     *  Add subviews to the main view
     */
    private void addSubViews(){
        // Add Email TextEdit
        emailTextEdit = (EditText) findViewById(R.id.email_editText);
        emailTextEdit.setSelected(false);

        // Add Remember Me CheckBox to Main View
        rememberMeCheckBox = (CheckBox)findViewById(R.id.remember_me_checkbox);

        // Add Password TextEdit
        passwordTextEdit = (EditText) findViewById(R.id.password_editText);

        // Add Forgot password Button to Main View
        forgotPasswordButton = (Button)findViewById(R.id.forgotPassword_button);
        forgotPasswordButton.setEnabled(false);

        // Add Log in Button to Main View
        logInButton = (Button) findViewById(R.id.login_button);
        logInButton.setEnabled(false);

        // Add Register Button to Main View
        registerButton = (Button)findViewById(R.id.register_button);
        registerButton.setEnabled(true);

        // Add Spinner ProgressBar to Main View
        spinner = (ProgressBar) findViewById(R.id.loading_progressBar_logInActivity);
        spinner.getIndeterminateDrawable().setColorFilter(0xff80cbc4, android.graphics.PorterDuff.Mode.MULTIPLY);

        // Add Fingerpring Button and Info TextView to Main View and Configure them
        fingerPrintConf();
    }

    /**
     *  Configure subviews
     */
    private void configureSubviews(){
        // Configure Remember Me CheckBox
        checkBoxConf();

        // Configure Email EditText
        emailTextFieldConf();

        // Configure Password EditText
        passwordEditTextConf();

        // Configure Forgot password Button
        forgotPasswordButtonConf();

        // Configure Log in Button
        logInButtonConf();

        // Configure Register Button
        registerButtonConf();
    }

    //region Remember Me CheckBox Configuration
    /**
     *  Configure Remember Me CheckBox
     */
    private void checkBoxConf(){
        loadRememberMeCheckBoxValue();
        rememberMeCheckBox.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                if (rememberMeCheckBox.isChecked()){
                    isRememberMeCheckBox = true;
                    saveRememberMeCheckBoxValue();
                    saveEmailEditTextValue(emailTextEdit.getText().toString());
                    rememberMeCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_checked, null), null, null, null);
                } else {
                    isRememberMeCheckBox = false;
                    saveRememberMeCheckBoxValue();
                    rememberMeCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_unchecked, null), null, null, null);
                    saveEmailEditTextValue("");
                }
            }
        });
    }

    /**
     *  Load Remember Me CheckBox value
     */
    private void loadRememberMeCheckBoxValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean value = settings.getBoolean("rememberMeCheckBox", false);
        if (value){
            isRememberMeCheckBox = true;
            rememberMeCheckBox.setChecked(true);
            rememberMeCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_checked, null), null, null, null);
            loadEmailEditTextValue();
        } else {
            isRememberMeCheckBox = false;
            rememberMeCheckBox.setChecked(true);
            rememberMeCheckBox.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_unchecked, null), null, null, null);
        }
    }

    /**
     *  Save Remember Me CheckBox value
     */
    private void saveRememberMeCheckBoxValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("rememberMeCheckBox", isRememberMeCheckBox);
        editor.commit();
    }

    /**
     *  Save Email EditText value
     * @param value (email address)
     */
    private void saveEmailEditTextValue(String value){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("email", value);
        editor.commit();
    }

    /**
     *  Load saved email from Remember Me CheckBox and set to Email EditText
     */
    private void loadEmailEditTextValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString("email", "");
        emailTextEdit.setText(value);
        emailTextEdit.setSelected(false);
        if (isEmailValid(value)){
            isEmailValid = true;
        } else {
            isEmailValid = false;
        }
    }
    //endregion

    //region Email EditText Configuration
    /**
     *  Configure Email EditText
     */
    private void emailTextFieldConf() {
        emailTextEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEmailValid(emailTextEdit.getText().toString())) {
                    isEmailValid = true;
                    forgotPasswordButton.setEnabled(true);
                    forgotPasswordButton.setTextColor(Color.parseColor("#ffffffff"));
                } else {
                    isEmailValid = false;
                    forgotPasswordButton.setEnabled(false);
                    forgotPasswordButton.setTextColor(Color.parseColor("#FF515966"));
                }
                if (isRememberMeCheckBox){
                    saveEmailEditTextValue(emailTextEdit.getText().toString());
                }
                validateLogInButton();

                if (fingerPrint != null && fingerPrintLabel != null){
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    boolean value = settings.getBoolean("isFingerPrint", false);
                    String email = settings.getString("fingetPrintEmail", "");

                    if (value && email.matches(emailTextEdit.getText().toString())){
                        fingerPrint.setBackgroundResource(R.drawable.finger_print_enebled);
                        fingerPrint.setEnabled(true);
                        fingerPrintLabel.setText("");
                    } else {
                        fingerPrint.setBackgroundResource(R.drawable.finger_print_disabled);
                        fingerPrint.setEnabled(false);
                        fingerPrintLabel.setText("Sign in to enable Fingerprint");
                    }
                }
            }
        });

        emailTextEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }

    /**
     * Validate email
     * @param email (email address)
     * @return true or false (valid email or invalid email)
     */
    private boolean isEmailValid(String email) {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            return true;
        else
            return false;
    }
    //endregion

    //region Password EdiText Configuration
    /**
     *  Configure Password EditText
     */
    private void passwordEditTextConf() {
        passwordTextEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordTextEdit.getText().toString().isEmpty()) {
                    isPasswordValid = false;
                } else {
                    isPasswordValid = true;
                }
                validateLogInButton();
            }
        });

        passwordTextEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
     *  Hide keyboard if given view has no focus
     * @param view (EditText)
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /**
     *  Validate Log In Button
     */
    private void validateLogInButton() {
        if (isPasswordValid && isEmailValid) {
            logInButton.setEnabled(true);
            logInButton.setBackgroundResource(R.drawable.button_enabled);
        } else {
            logInButton.setEnabled(false);
            logInButton.setBackgroundResource(R.drawable.button_disabled);
        }
    }
    //endregion

    //region Forgot password Button Configuration
    /**
     *  Configure Forgot password Button
     */
    private void forgotPasswordButtonConf(){
        forgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUserInteractionAndStartSpinner();
                new forgotPassword().execute(emailTextEdit.getText().toString().toLowerCase());
            }
        });
    }
    //endregion

    //region Log in Button Configuration
    /**
     *  Configure Log in Button
     */
    private void logInButtonConf() {
        logInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUserInteractionAndStartSpinner();
                new logIn().execute(emailTextEdit.getText().toString().toLowerCase());
            }
        });
    }
    //endregion

    //region Register Button Configuration
    /**
     *  Configure Register Button
     */
    private void registerButtonConf(){
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpActivityInent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(signUpActivityInent);
                finish();
            }
        });
    }
    //endregion


    //region Fingerprint configuration
    /**
     *  Configure Fingerprint Button and Info Label
     */
    private void fingerPrintConf() {
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
            mainLayout = (RelativeLayout)findViewById(R.id.fingerPrint_layout);

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

            // Check if device has Fingerprint sensor
            if (fingerprintManager.isHardwareDetected()){
                if (fingerPrint != null){
                    mainLayout.removeAllViewsInLayout();
                    fingerPrint.setEnabled(false);
                } else {
                    fingerPrint = new ImageButton(this);
                    fingerPrint.setId(R.id.finger_print_imageButton);
                    fingerPrint.setEnabled(false);




                    RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);



                    fingerPrint.setLayoutParams(buttonParams);
                }

                // Device has Fingerprint sensor but user is not enrolled
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    // User hasn't enrolled any fingerprints to authenticate with
                    fingerPrint.setBackgroundResource(R.drawable.finger_print_disabled);
                    fingerPrint.setEnabled(false);
                    mainLayout.addView(fingerPrint);

                    RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    labelParams.addRule(RelativeLayout.ABOVE, R.id.finger_print_imageButton);
                    labelParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    if (fingerPrintLabel !=  null){
                        fingerPrintLabel.setText("Configure your Fingerprint in your settings");
                        fingerPrintLabel.setTextColor(Color.parseColor("#ffffffff"));
                        mainLayout.addView(fingerPrintLabel);
                    } else {
                        fingerPrintLabel = new TextView(this);
                        fingerPrintLabel.setLayoutParams(labelParams);

                        mainLayout.addView(fingerPrintLabel);
                        fingerPrintLabel.setText("Configure your Fingerprint in your settings");
                        fingerPrintLabel.setTextColor(Color.parseColor("#ffffffff"));
                    }


                } else {
                    // Everything is ready for fingerprint authentication
                    loadFingerPrintButtonValue();
                    mainLayout.addView(fingerPrint);

                    fingerPrintSecurity();
                    generateKey();
                    fingerPrint.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            exeuteFingerPrint();
                        }
                    });
                }
                isFingerPrint = true;

            }
    }

    /**
     *  Load Fingerprint saved values and configure subviews
     */
    private void loadFingerPrintButtonValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean value = settings.getBoolean("isFingerPrint", false);
        String email = settings.getString("fingetPrintEmail", "");

        if (value && email.matches(emailTextEdit.getText().toString())){
            fingerPrint.setBackgroundResource(R.drawable.finger_print_enebled);
            fingerPrint.setEnabled(true);

        } else {
            RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            labelParams.addRule(RelativeLayout.ABOVE, R.id.finger_print_imageButton);
            labelParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            if (fingerPrintLabel != null){
                fingerPrintLabel.setTextColor(Color.parseColor("#ffffffff"));
                fingerPrintLabel.setText("Sign in to enable Fingerprint");
                fingerPrint.setBackgroundResource(R.drawable.finger_print_disabled);
                mainLayout.addView(fingerPrintLabel);
                fingerPrint.setEnabled(false);
            } else {
                fingerPrintLabel = new TextView(this);
                fingerPrintLabel.setLayoutParams(labelParams);

                mainLayout.addView(fingerPrintLabel);
                fingerPrintLabel.setTextColor(Color.parseColor("#ffffffff"));
                fingerPrintLabel.setText("Sign in to enable Fingerprint");
                fingerPrint.setBackgroundResource(R.drawable.finger_print_disabled);
                fingerPrint.setEnabled(false);
            }

        }
    }

    /**
     *  Save Fingerprint values
     */
    private void saveFingerPrintValue(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFingerPrint", false);
        editor.putString("fingetPrintEmail", "");
        editor.commit();

        fingerPrint.setEnabled(false);
        fingerPrintLabel.setText("Sign in to enable Fingerprint");
    }

    /**
     *  Check Fingerprint security
     */
    private void fingerPrintSecurity(){
        if (!keyguardManager.isKeyguardSecure()) {

            Toast.makeText(this,
                    "Lock screen security not enabled in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Fingerprint authentication permission not enabled",
                    Toast.LENGTH_LONG).show();

            return;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {

            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Register at least one fingerprint in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }
    }


    /**
     *  Generete keychain
     */
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize chiper
     * @return
     */
    private boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     *  Execute Fingerprint recognition
     */
    private void exeuteFingerPrint(){
        if (cipherInit()) {
            Toast.makeText(this,
                    "Place your finger on sensor",
                    Toast.LENGTH_LONG).show();

            cryptoObject = new FingerprintManager.CryptoObject(cipher);
            FingerprintHandler helper = new FingerprintHandler(this);
            helper.startAuth(fingerprintManager, cryptoObject, this);

            if (helper.isSucceed){
                saveCurrentUserEmail();
                Intent accountActivity = new Intent(getApplicationContext(), AccountActivity.class);
                startActivity(accountActivity);
                finish();
            }


        }
    }
    //endregion


    //region API Calls
    /**
     *  Execute Log in AsyncTask
     */
    private class logIn extends AsyncTask<String,Void,Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.validateEmail(params[0]);
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
                    alertUserWithMessage(message);
                } else if (status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    new checkIfAccountActivated().execute(emailTextEdit.getText().toString().toLowerCase());
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.");
                }


            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.");
            }

        }
    }

    /**
     *  Execute Account validation AsyncTask
     */
    private class checkIfAccountActivated extends AsyncTask<String,Void,Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.validateActivation(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");

                if (status.matches("1")){
                    try {
                        new validatePassword().execute(emailTextEdit.getText().toString().toLowerCase(),sha1(passwordTextEdit.getText().toString()));
                    } catch (NoSuchAlgorithmException e) {
                        startUserInteractionAndStopSpinner();
                        alertUserWithMessage("Unexpected error occurred, please try again later.");
                    }
                } else if (status.matches("-1")){
                    String message = jsonBody.getString("message");
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message);
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.");
                }


            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.");
            }

        }
    }

    /**
     *  Execute Password Validation AsyncTask
     */
    private class validatePassword extends AsyncTask<String,String,Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.validatePassword(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");

                if (status.matches("1")){
                    startUserInteractionAndStopSpinner();
                    saveCurrentUserEmail();
                    Intent accountActivity = new Intent(getApplicationContext(), AccountActivity.class);
                    startActivity(accountActivity);
                    finish();
                } else if (status.matches("-1")){
                    String message = jsonBody.getString("message");
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message);
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.");
                }


            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.");
            }

        }
    }

    /**
     *  Execute Forgot password AsyncTask
     */
    private class forgotPassword extends AsyncTask<String,Void,Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.validateEmail(params[0]);
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
                    alertUserWithMessage("Invalid account.");
                } else if (status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    new getUserFirstName().execute(emailTextEdit.getText().toString().toLowerCase());
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.");
                }


            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.");
            }

        }
    }

    /**
     *  Execute Get user first name AsyncTask
     */
    private class getUserFirstName extends AsyncTask<String,Void,Void>{
        String jsonString;
        JSONObject jsonBody = null;
        String firstName;
        String tempPassw;
        String hashedPass;
        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.getUserFirstName(params[0]);
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
                    firstName = message;
                    tempPassw = tempPassword();
                    try{
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("email", emailTextEdit.getText().toString().toLowerCase());
                        params.put("firstName", firstName);
                        params.put("tempPassword", tempPassw);
                        params.put("hashedPass", sha1(tempPassw));
                        new changePassword().execute(params);

                    }catch (NoSuchAlgorithmException e){
                        startUserInteractionAndStopSpinner();
                        alertUserWithMessage("Unexpected error occurred, please try again later.");
                    }


                } else if (status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message);
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.");
                }


            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.");
            }
        }
    }

    /**
     *  Execute Change password AsuncTask
     */
    private class changePassword extends AsyncTask<Map<String, String>,Void,Void>{
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected Void doInBackground(Map<String, String>... params) {
            jsonString = GappzaAPI.changePassword(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");
                String message = jsonBody.getString("message");
                if (status.matches("1")){
                    if (isFingerPrint){
                        saveFingerPrintValue();
                    }
                    fingerPrintConf();
                    alertUserWithMessage(message);
                    startUserInteractionAndStopSpinner();

                } else if (status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage(message);
                } else {
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Unexpected error occurred, please try again later.");
                }


            } catch (JSONException e) {
                alertUserWithMessage("Unexpected error occurred, please try again later.");
            }

        }
    }

    //region API call helpers
    //region Spinner ProgressBar Actions
    /**
     *  Stop user interactions and Start Spinner animating
     */
    private void stopUserInteractionAndStartSpinner(){
        EditText[] anArray = {
                emailTextEdit,
                passwordTextEdit
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
     *  Generate temporary password
     * @return generated password
     */
    private String tempPassword(){
        Date myDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(myDate);
        int seconds = calendar.get(Calendar.SECOND);
        int nanoSeconds = calendar.get(Calendar.MILLISECOND);
        long s = calendar.getTimeInMillis();
        return ""+seconds+""+nanoSeconds+s;
    }

    /**
     *  Save current user email
     */
    public void saveCurrentUserEmail(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("currentUserEmail", emailTextEdit.getText().toString().toLowerCase());
        editor.commit();
    }

    /**
     * Alert user with a dialog message
     * @param message
     */
    private void alertUserWithMessage(String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    /**
     * Encrypt a string to sha1 format
     * @param input - string to be encrypted
     * @return encrypted string
     * @throws NoSuchAlgorithmException
     */
    private String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
    //endregion
    //endregion

}