package com.fira.gappza;

import com.devmarvel.creditcardentry.library.CreditCardForm;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by firdavsiimajidzoda on 11/29/16.
 */

public class Payment {

    //region Fields
    // .00 Decimal Formatter
    private  DecimalFormat df2 = new DecimalFormat(".##");

    Map<String, String> providers = new HashMap<String, String>();
    Map<String, String> providersPrefix = new HashMap<String, String>();
    Map<String, String> providersFormat = new HashMap<String, String>();

    // Emon attributes
    String un = "firdavsusaxml";
    String pass = "52819380975";
    String secretKey = "WS#!@aq54&*kgj";
    String emonPaymentId;


    // Gapza attributes
    String userEmail;
    String number;
    String providerID;
    String providerError;
    String amount;
    boolean isTermsAndConditionChecked = false;
    double exchRate;
    int numberLength;
    String paymentID;         //For Gapza Side
    String description = "Payment";
    String date;
    String providerName;
    String fullAmount;

    String cardNumber;
    String expMonth;
    String expYear;
    String CVC;


    // Stripe attributes
    String stripeToken;
    String paymentTextField;
    String capturedChargedID;
    String appFee;
    CreditCardForm creditCardForm;
    //endregion

    // Constructor
    Payment(String email){
        cardNumber = "";
        expMonth = "";
        expYear = "";
        CVC = "";

        providers.put("0", "42");
        providers.put("1", "43");
        providers.put("2", "44");
        providers.put("3", "48");
        providers.put("4", "90");
        providers.put("5", "91");
        providers.put("6", "93");
        providers.put("7", "95");
        providers.put("8", "98");

        providersPrefix.put("0", "4");
        providersPrefix.put("1", "4");
        providersPrefix.put("2", "4");
        providersPrefix.put("3", "4");
        providersPrefix.put("4", "9");
        providersPrefix.put("5", "9");
        providersPrefix.put("6", "9");
        providersPrefix.put("7", "9");
        providersPrefix.put("8", "9");


        providersFormat.put("0", "Sample: 4X XXX XXXX");
        providersFormat.put("1", "Sample: 4X XXX XXXX");
        providersFormat.put("2", "Sample: 4X XXX XXXX");
        providersFormat.put("3", "Sample: 4X XXX XXXX");
        providersFormat.put("4", "Sample: 9X XXX XXXX");
        providersFormat.put("5", "Sample: 9X XXX XXXX");
        providersFormat.put("6", "Sample: 9X XXX XXXX");
        providersFormat.put("7", "Sample: 9X XXX XXXX");
        providersFormat.put("8", "Sample: 9X XXX XXXX");

        userEmail = email;
        number = "";
        providerID = providers.get("Intercom NGN");
        providerError = providersFormat.get("Intercom NGN");
        providerName = "0";
        amount = "";
        exchRate = 7.8;
        numberLength = 9;
        paymentID = "";

        Date myDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date = dateFormat.format(myDate);
    }

    //region Validations
    /**
     *  Validate Phone number
     * @return true or false
     */
    public String validateNumber(){
        if (number.matches("")) {
            return "Your phone number cannot be empty.";
        } else {
            if (doStringContainsNumber(number) == true) {
                if (number.length() < numberLength ||  number.length() > numberLength){
                    return providersFormat.get(providerName);
                } else if (number.startsWith(providersPrefix.get(providerName))){
                    return "";
                } else {
                    return providersFormat.get(providerName);
                }
            } else {
                return "Only numbers allowed.";
            }
        }
    }

    /**
     * Validate amount limit
     * @return true or false
     */
    public String  validateAmmount(){
        if (amount.matches("")) {
            return "Amount can not be empty.";
        } else {
            if (doStringContainsNumber(amount) == true) {
                if (Double.parseDouble(amount) > 20.0 || Double.parseDouble(amount) < 5.0 ) {
                    return "Amount should be between 5 - 20.";
                } else {
                    return "";
                }
            } else {
                return "Only numbers allowed.";
            }
        }
    }

    /**
     * Validate Terms And Conditions CheckBox, if user agreed or not
     * @return true or false
     */
    public String validateTermsButton(){
        if (isTermsAndConditionChecked){
            return "";
        } else {
            return "Agree to terms and conditions";
        }
    }

    /**
     * Validate Credit card details
     * @return true or false
     */
    public String validateCard(){
        if (creditCardForm.isCreditCardValid()){
            return "";
        } else {
            return "Invalid card";
        }
    }

    /**
     *  Validate payment, inclutind credit card, number, amount, terms
     * @return
     */
    public String validatePayment() {
        return ""+validateNumber()+validateAmmount()+validateTermsButton()+validateCard();
    }

    /**
     * Check if string only contains number
     * @param text
     * @return true or false
     */
    private boolean doStringContainsNumber(String text) {
        try {
            Long.parseLong(text);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     *  Calculate Application fee
     * @return calculated app fee
     */
    public String calcFee(){
        double amountEntered = Double.parseDouble(amount);
        double gapzaFee = 2.0;
        double stripeFee = (amountEntered+gapzaFee)*3.0/100+0.31;
        double finalFee = stripeFee*100;
        finalFee = Math.ceil(finalFee);
        finalFee = finalFee/100;
        String stripeFeeFormatted = df2.format(finalFee);
        return stripeFeeFormatted;
    }
    //endregion


}
