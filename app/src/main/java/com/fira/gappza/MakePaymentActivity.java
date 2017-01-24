package com.fira.gappza;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.devmarvel.creditcardentry.library.CardValidCallback;
import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MakePaymentActivity extends Activity {

    //region Fields
    private Payment payment;

    // Saving directory name
    private final String PREFS_NAME = "MyPrefsFile";

    // .00 Decimal formatter
    private DecimalFormat df2 = new DecimalFormat(".##");

    // Subviews and associates
    private NumberPicker providerPicker;

    private EditText numberEditText;
    private TextView numberErrorTextView;

    private EditText amountEditText;
    private TextView amountErrorTextView;

    private TextView appFeeTextView;

    private CheckBox termsConditions;

    private Button submitButton;

    private ProgressBar spinner;

    private CreditCardForm creditCardForm;
    private TextView cardErrorLabel;

    private Button back;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        // Create a new Payment
        payment = new Payment(loadCurrentUserEmail());

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
            long diffMinutes = diff / (60 * 1000) % 60;
            if (diffMinutes > 1){
                Log.d("method", "true");
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
     *  Add SubViews in main View
     */
    private void addSubViews(){
        // Add Back Button to Main View
        back = (Button)findViewById(R.id.backFromMakePaymentActivitytoAccountActivity_button);

        // Add Credit Card Form to Main View
        creditCardForm = (CreditCardForm)findViewById(R.id.credit_card_form);
        payment.creditCardForm = creditCardForm;

        // Add Provider picker NumberPicker & CreditCard Error TextView to Main View
        cardErrorLabel = (TextView)findViewById(R.id.card_textView_makePaymentActivity);
        providerPicker = (NumberPicker) findViewById(R.id.provier_picker);
        String[] values = new String[] {"Intercom NGN","Eastera NGN","Babilon NGN","TelecomTech NGN","Megafon","Beeline", "Tcell", "TKmobile", "Babilon-M"};
        providerPicker.setMinValue(0);
        providerPicker.setMaxValue(values.length-1);
        providerPicker.setDisplayedValues(values);

        // Add Number EditText & Number Error TextView to Main View
        numberEditText = (EditText)findViewById(R.id.phoneNumber_editText_makePaymentActivity);
        numberErrorTextView = (TextView)findViewById(R.id.phoneNumberError_textView_makePaymentActivity);

        // Add Amount EditText & Amount Error TextView to Main View
        amountEditText = (EditText)findViewById(R.id.amount_editText_makePaymentActivity);
        amountErrorTextView = (TextView)findViewById(R.id.amountError_textView_makePaymentActivity);

        // Add App fee TextLabel to Main View
        appFeeTextView = (TextView)findViewById(R.id.feeLabel_textView_makePaymentActivity);

        // Add Terms and Conditions CheckBox
        termsConditions = (CheckBox)findViewById(R.id.termsAndConditions_checkbox_makePaymentActivity);

        // Add Submit Button to Main View and Update it's state
        submitButton = (Button)findViewById(R.id.submitButton_makePaymentActivity);
        submitButton.setEnabled(false);
        updateSubmitButtonStatus();

        // Add Spinner ProgressBar to Main View
        spinner = (ProgressBar) findViewById(R.id.loading_progressBar_makePaymentActivity);
        spinner.getIndeterminateDrawable().setColorFilter(0xff80cbc4, android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    /**
     *  Configure SubViews
     */
    private void configureSubViews(){
        // Configure Back Button
        backButtonConf();

        // Configure CreditCard CardForm
        cardDetailsConf();

        // Configure ProviderPicker NumberPicker
        providerPickerConf();

        // Configure Number EditText
        numberEditTextConf();

        // Configure Amount EditText
        amountEditTextConf();

        // Configure Terms and Conditions CheckBox
        termsAndConditionsCheckBoxConf();

        // Configure Submit Button
        submitButtonConf();
    }

    //region Back Button Configuration
    /**
     *  Configure Back Button
     */
    private void backButtonConf(){
        back = (Button)findViewById(R.id.backFromMakePaymentActivitytoAccountActivity_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountActivityInent = new Intent(getApplicationContext(), AccountActivity.class);
                startActivity(accountActivityInent);
                finish();
            }
        });
    }
    //endregion

    //region Credit Card CreditCardForm Configuration
    /**
     *  Configure CreditCard CardForm
     */
    private void cardDetailsConf(){
        creditCardForm.setOnCardValidCallback(new CardValidCallback() {
            @Override
            public void cardValid(CreditCard creditCard) {
                updateSubmitButtonStatus();
            }
        });

        creditCardForm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(v);
                    updateSubmitButtonStatus();
                }
                updateSubmitButtonStatus();
                cardErrorLabel.setText(payment.validateCard());
            }
        });
    }
    //endregion

    //region Provider Picker NumberPicker Configuration
    // Configure ProviderPicker NumberPicker
    private void providerPickerConf(){
        providerPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                payment.providerID = payment.providers.get(providerPicker.getValue()+"");
                Log.d("paymentID", payment.paymentID);
                payment.providerError = payment.providersFormat.get(providerPicker.getValue()+"");
                payment.providerName = providerPicker.getValue()+"";


                if (!numberEditText.getText().toString().matches("")){
                    numberErrorTextView.setText(payment.validateNumber());
                }
            }
        });

    }
    //endregion

    //region Number EditText Configuration
    /**
     * Configure Number EditText
     */
    private void numberEditTextConf(){
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                payment.number = numberEditText.getText().toString();
                numberErrorTextView.setText(payment.validateNumber());
                updateSubmitButtonStatus();
            }
        });

        numberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }
    //endregion

    //region Amount EditText Configuration
    /**
     * Configure Amount EditText
     */
    private void amountEditTextConf(){
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                payment.amount = amountEditText.getText().toString();
                amountErrorTextView.setText(payment.validateAmmount());
                appFeeCalc();
                updateSubmitButtonStatus();
            }
        });

        amountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });
    }

    //region App fee calculation
    /**
     *  Calculate Application fee
     */
    public void appFeeCalc() {
        if (!amountEditText.getText().toString().matches("") && !amountEditText.getText().toString().matches("0")){
            if (payment.validateAmmount().matches("")){
                payment.appFee = payment.calcFee();
                appFeeTextView.setText("Card transaction and application fee: "+df2.format(Double.parseDouble(payment.appFee)+2.0));
            } else {
                payment.appFee = "";
                appFeeTextView.setText("");
            }
        }

    }
    //endregion
    //endregion

    //region EditText helpers
    /**
     *  Hide keyboard if given view has no focus
     * @param view (EditText)
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    //endregion

    //region Terms and Conditions CheckBox Configuration
    /**
     *  Configure Terms and Conditions CheckBox
     */
    private void termsAndConditionsCheckBoxConf(){
        termsConditions.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (termsConditions.isChecked()){
                    payment.isTermsAndConditionChecked = true;
                    termsConditions.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_checked, null), null, null, null);
                } else {
                    payment.isTermsAndConditionChecked = false;
                    termsConditions.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.checkbox_unchecked, null), null, null, null);
                }
                updateSubmitButtonStatus();
            }
        });
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
                if (payment.validatePayment().matches("")){
                    stopUserInteractionAndStartSpinner();
                    getToken();
                } else {
                    updateAll();
                    submitButton.setEnabled(false);
                    submitButton.setBackgroundResource(R.drawable.button_disabled);
                }

            }
        });
    }


    //region Submit Button Validation
    /**
     * Updates Submit Button validation state
     */
    public void updateSubmitButtonStatus(){
        if (payment.validatePayment().matches("")){
            submitButton.setEnabled(true);
            submitButton.setBackgroundResource(R.drawable.button_enabled);
        } else {
            submitButton.setEnabled(false);
            submitButton.setBackgroundResource(R.drawable.button_disabled);
        }
    }
    //endregion
    //endregion

    //region All Errors Configuration
    /**
     *  Updates all the Errors
     */
    private void updateAll(){
        cardErrorLabel.setText(payment.validateCard());
        numberErrorTextView.setText(payment.validateNumber());
        amountErrorTextView.setText(payment.validateAmmount());
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


    //region API Calls
    /**
     *  Get Stripe token for payment
     */
    private void getToken(){
        CreditCard cardDetails = creditCardForm.getCreditCard();
        final String publishableApiKey = "pk_live_L0TaF0wxIcMjy4eDNaLMxI8q";
        Card card = new Card(cardDetails.getCardNumber(),
                Integer.valueOf(cardDetails.getExpMonth()),
                Integer.valueOf(cardDetails.getExpYear()),
                cardDetails.getSecurityCode());

        Stripe stripe = new Stripe();
        stripe.createToken(card, publishableApiKey, new TokenCallback() {
            public void onSuccess(Token token) {
                stopUserInteractionAndStartSpinner();
                new captureCharge().execute(token.getId());
            }

            public void onError(Exception error) {
                startUserInteractionAndStopSpinner();
                alertUserWithMessage("Your card is not going through, try different card.", false);
            }
        });
    }

    /**
     *  Execute Capture charge AsyncTask
     */
    private class captureCharge extends AsyncTask<String,Void,String> {
        String jsonString;
        JSONObject jsonBody = null;
        @Override
        protected String doInBackground(String... params) {
            Map<String, String> parameters = new HashMap<String, String>();
            String finalAmount = Double.parseDouble(payment.amount)+2.0+Double.parseDouble(payment.appFee)+"";

            Date myDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(myDate);
            int seconds = calendar.get(Calendar.SECOND);
            int nanoSeconds = calendar.get(Calendar.MILLISECOND);
            long s = calendar.getTimeInMillis();
            String id = ""+seconds+""+nanoSeconds+s;
            parameters.put("tok", params[0]);
            parameters.put("amount", finalAmount);
            parameters.put("id", id);

            payment.paymentID = id;

            jsonString = GappzaAPI.captureCharge(parameters);
            return null;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");
                if (status.matches("1")){
                    String captureID = jsonBody.getString("capturedID");
                    payment.capturedChargedID =captureID;
                    new prepareForPayment().execute();

                } else if (status.matches("-1")){
                    startUserInteractionAndStopSpinner();
                    alertUserWithMessage("Your card did not go through, use different card", false);
                } else {
                    startUserInteractionAndStopSpinner();
                    Log.e("GappzaERROR", "MakePaymentActivity: captureCharge()");
                    alertUserWithMessage("Unexpected error occurred, please try again later.", true);
                }


            } catch (JSONException e) {
                startUserInteractionAndStopSpinner();
                Log.e("GappzaERROR", "MakePaymentActivity: captureCharge()");
                alertUserWithMessage("Unexpected error occurred, please try again later.", true);
            }

        }
    }

    /**
     * Execute Prepare Payment AsyncTask
     */
    private class prepareForPayment extends AsyncTask<Void, Void, Void>{
        String jsonString;
        JSONObject jsonBody = null;

        @Override
        protected Void doInBackground(Void... params) {
            jsonString = GappzaAPI.getRate();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            try {
                jsonBody = new JSONObject(jsonString);
                String quotes = jsonBody.getJSONObject("quotes").toString();
                JSONObject USDTJS = new JSONObject(quotes);
                double rate = Double.parseDouble(USDTJS.getString("USDTJS"));
                payment.exchRate = Double.parseDouble(df2.format(rate));
                new makePayment().execute();


            } catch (JSONException e) {
                startUserInteractionAndStopSpinner();
                Log.e("GappzaERROR", "MakePaymentActivity: prepareForPayment()");
                alertUserWithMessage("Unexpected error occurred, please try again later.", true);
            }
        }
    }

    /**
     * Execute Make Payment AsyncTask
     */
    private class makePayment extends AsyncTask<Void, Void, Void>{
        String jsonString;


        @Override
        protected Void doInBackground(Void... params) {
            double calc = Double.parseDouble(payment.amount)*payment.exchRate;
            String psm = calc+"";
            String opid = payment.providerID;
            String nm = payment.number;
            String txt = payment.paymentID;


            String keyBase = payment.un+opid+txt+psm+nm+ MD5(payment.pass)+payment.secretKey;
            String key = null;
            try {
                key = sha1(keyBase);

            } catch (NoSuchAlgorithmException e) {
                startUserInteractionAndStopSpinner();
                alertUserWithMessage("Unexpected error occurred, please try again later.", true);
            }

            String link = "http://emon.tj/xml2/pay.aspx?un="+payment.un+"&psm="+psm+"&opid="+opid+"&nm="+nm+"&txd="+txt+"&reg=&key="+key;


            jsonString = GappzaAPI.makePayment(link);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){

            String [] values = jsonString.split("Status>");
            String [] status1 = values[1].split("</");
            String status = status1[0];
            String [] valuesID = jsonString.split("PaymentID>");
            String [] valuesID1 = valuesID[1].split("</");
            String emonPaymentID = valuesID1[0];
            payment.emonPaymentId =emonPaymentID;
            new checkBalance().execute();

            if (status.matches("1")){
                new storePayment().execute();
            } else if (status.matches("2")){
                new storePayment().execute();
            } else if (status.matches("3")){
                new storePayment().execute();
            } else {
                new storePayment().execute();
            }
        }
    }

    /**
     * Execute Check Balance AsyncTask
     */
    private class checkBalance extends  AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            GappzaAPI.checkBalance(payment.exchRate+"");
            return null;
        }
    }

    /**
     *  Execute Store Payment AsyncTask
     */
    private class storePayment extends  AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            Map<String, String> param = new HashMap<String, String>();
            param.put("email", loadCurrentUserEmail());
            param.put("number", payment.number);
            param.put("amount", Integer.parseInt(payment.amount)+"");
            param.put("emonPaymentId", payment.emonPaymentId);
            param.put("paymentId", payment.paymentID);
            param.put("date", payment.date);

            GappzaAPI.storePayment(param);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                 new postPayment().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            }
            else {
                new postPayment().execute();
            }
            alertUserWithMessageAndGotoAccountActivity("Your payment has been recieved. We will email you a reciepe for this transaction");
        }
    }


    /**
     *  Execute Post Payment AsyncTask
     */
    private class postPayment extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Map<String, String> param = new HashMap<String, String>();
            String un = "firdavsusaxml";
            String pass = "52819380975";
            String secretKey = "WS#!@aq54&*kgj";

            String id = payment.emonPaymentId;
            String key = null;
            String keyBase = un+""+MD5(pass)+""+secretKey+""+id;
            try {
                key = sha1(keyBase);
            } catch (NoSuchAlgorithmException e) {
                startUserInteractionAndStopSpinner();
                alertUserWithMessage("Unexpected error occurred, please try again later.", true);
            }
            String link = "http://emon.tj/xml2/check.aspx?un="+un+"&PaymentID="+id+"&key="+key;
            String finalAmount = Double.parseDouble(payment.amount)+2.0+Double.parseDouble(payment.appFee)+"";

            param.put("firstName", loadCurrentUserName());
            param.put("email", loadCurrentUserEmail());
            param.put("number", payment.number);
            param.put("amount", finalAmount);
            param.put("emonPaymentId", payment.emonPaymentId);
            param.put("paymentId", payment.paymentID);
            param.put("capturedChargeId", payment.capturedChargedID);
            param.put("statusCheckLink", link);
            param.put("date", payment.date);

            GappzaAPI.postPayment(param);

            return null;
        }
    }

    //region API Call Helpers
    /**
     * Loads current user's email
     * @return email string
     */
    private String loadCurrentUserEmail(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString("currentUserEmail", "");
        return value;
    }

    /**
     * Load current user's name
     * @return email string
     */
    private String loadCurrentUserName(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString("userFirstName", "");
        return value;
    }

    //region Spinner ProgressBar Actions
    /**
     *  Stop user interactions and Start Spinner animating
     */
    private void stopUserInteractionAndStartSpinner(){
        EditText[] anArray = {
                numberEditText,
                amountEditText
        };

        for (EditText object: anArray){
            if (object.hasFocus()){
                hideKeyboard(object);
            }
        }

        if (creditCardForm.hasFocus()){
            hideKeyboard(creditCardForm);
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

    /**
     * Alert user with a dialog message and Intent to AccountActivity.java
     * @param message to be viewed to user
     */
    private void alertUserWithMessageAndGotoAccountActivity(String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                            backToAccountActivity();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    /**
     * Intent to AccountActivity.java
     */
    private void backToAccountActivity(){
        Intent accountActivityIntent = new Intent();
        accountActivityIntent.setClass(getApplicationContext(), AccountActivity.class);
        startActivity(accountActivityIntent);
        finish();
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

    /**
     * Encyrpt string to MD5 format
     * @param md5 - string to be encrypted
     * @return encrypted string
     */
    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
    //endregion
    //endregion
}
