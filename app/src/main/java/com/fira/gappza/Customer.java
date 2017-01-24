package com.fira.gappza;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by firdavsiimajidzoda on 11/25/16.
 */
public class Customer {

    //region Fields
    String firstName = null;
    String lastName = null;
    String phoneNumber = null;
    String email = null;
    String password = null;
    String confirmPassword = null;
    String date = null;
    boolean isTermsAndConditionChecked = false;
    //endregion

    // Constructor
    Customer(){
        firstName = "";
        lastName = "";
        phoneNumber = "";
        email = "";
        password = "";
        confirmPassword = "";

        Date myDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date = dateFormat.format(myDate);

        isTermsAndConditionChecked = false;
    }

    //region Validate First name
    /**
     * Validate First name
     * @return true or false
     */
    public String validateFirstName(){
        if (firstName == "") {
            return "Your first name can not be empty.";
        } else {
            if (containsOnlyLetters(firstName) == false){
                return "Only letters are allowed";
            }
            else if (firstName.length() < 2) {
                return "Your first name can not be less than 2 characters.";
            } else if (firstName.length() > 15) {
                return "Your first name can not longer.";
            }
            return "";
        }
    }
    //endregion

    //region Validate Last name
    /**
     * Validate Last name
     * @return true or false
     */
    public String validateLastName(){
        if (lastName == "") {
            return "Your last name can not be empty.";
        } else {
            if (containsOnlyLetters(lastName) == false){
                return "Only letters are allowed";
            } else if (lastName.length() < 2) {
                return "Your last name can not be less than 2 characters.";
            } else if (lastName.length() > 15) {
                return "Your last name can not longer.";
            } else {
                return "";
            }
        }
    }
    //endregion

    //region Contain only string check for First name and Last name
    /**
     * Validate if string only contains letters
     * @param name - input string
     * @return true or false
     */
    public boolean containsOnlyLetters(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }
    //endregion

    //region Validate Phone number
    /**
     * Validate Phone number
     * @return true or false
     */
    public String validatePhoneNumber() {
        if (phoneNumber == "") {
            return "Your phone number can not be empty.";
        } else {
            if ((doStringContainsNumber(phoneNumber)) == true) {
                if (phoneNumber.length() < 10 || phoneNumber.length() > 10) {
                    return "Your phone number has to be 10 digit number.";
                } else {
                    return "";
                }
            } else {
                return "Only numbers allowed.";
            }
        }
    }

    //region Onlu numbers string check for Phone number
    /**
     * Check string if it contains only numbers
     * @param text - input string
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
    //endregion
    //endregion

    //region Validate Email
    /**
     * Validate Email
     * @return true or false
     */
    public String  validateEmail() {
        if (email == "") {
            return "Your email can not be empty.";
        } else {
            if (isEmailValid(email) == false) {
                return "Your email has to mach format: abc@abc.abc";
            } else {
                return "";
            }
        }
    }

    //region Email format validation check for Email
    /**
     * Check email string if it is valid email or not
     * @param email - input string
     * @return true or false
     */
    public boolean isEmailValid(String email) {
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
    //endregion

    //region Validate Password
    /**
     * Validate Password
     * @return true or false
     */
    public String validatePassword() {
        if (password == "") {
            return "Your password can not be empty.";
        } else {
            if (password.length() < 8 || password.length() > 14) {
                return "Your password must be between 8 to 14 characters.";
            } else {
                return "";
            }
        }
    }
    //endregion

    //region Validate Confirm Password
    /**
     * Validate Confirm Password
     * @return
     */
    public String validateConfirmPassword() {
        if (confirmPassword.equals(password)) {
            return "";
        } else {
            return "Your password do not match.";
        }
    }
    //endregion

    //region Validate Terms And Conditions
    /**
     * Validate Terms And Conditions
     * @return true or false
     */
    public String validateTermsAndConditions(){
        if (isTermsAndConditionChecked) {
        return "";
        } else {
            return "No";
        }
    }
    //endregion

    //region Validate Customer
    /**
     * Validate Customer
     * @return "" or Error String
     */
    public String validateCustomer() {
        return ""+validateFirstName()+validateLastName()+validatePhoneNumber()+validateEmail()+validatePassword()+validateConfirmPassword()+validateTermsAndConditions();
    }
    //endregion

}
