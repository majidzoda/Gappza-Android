package com.fira.gappza;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;

/**
 * Created by firdavsiimajidzoda on 11/25/16.
 */
public class GappzaAPI {
    // Endpoint base link
    private static String baseURLString = "http://ec2-54-224-149-158.compute-1.amazonaws.com/";

    //region URL Connection with GET HTTP method
    /**
     * Convert given URL string to ByteArrayOutputStream
     * @param urlSpec - url string to be converted
     * @return ByteArrayOutputStream
     * @throws IOException
     */
    private static byte[] getUrlBytes(String urlSpec)throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw  new IOException(connection.getResponseMessage()+": with "+urlSpec);
            }

            int bytesRead = 0;
            byte [] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer))>0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    private static String getUrlString (String urlSpec) throws IOException{
        return new String((getUrlBytes(urlSpec)));
    }
    //endregion

    //region Endpoint Api Calls
    /**
     * Valides email if it exist in Data Base
     * @param email given string email to be validated
     * @return JSON string format with status and message
     */
    public static String validateEmail(String email){
        String result = null;
        String method = "checkIfAccountExistAndroid.php";
        try {
            String url = Uri.parse(baseURLString+method)
                    .buildUpon()
                    .appendQueryParameter("email", email)
                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;

        }
        catch (IOException ioe){
            result = "{\"status\":\"0\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }

        return result;
    }

    /**
     * Create an account with given customer details in Data Base
     * @param customer to be created
     * @return JSON string format with status and message
     * @throws NoSuchAlgorithmException
     */
    public static  String cteateAccount(Customer customer)throws NoSuchAlgorithmException{
        String result = null;
        String method = "createAccountAndroid.php";
        Log.d("Customer", customer.firstName+" "+customer.lastName);
        try {
            String url = Uri.parse(baseURLString+method)
                    .buildUpon()
                    .appendQueryParameter("firstName", customer.firstName)
                    .appendQueryParameter("lastName", customer.lastName)
                    .appendQueryParameter("phoneNumber", customer.phoneNumber)
                    .appendQueryParameter("email", customer.email)
                    .appendQueryParameter("password", sha1(customer.password))
                    .appendQueryParameter("date", customer.date)
                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;

        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }

        return result;
    }

    /**
     * Activates registered account in Data Base
     * @param email
     * @return JSON string format with status and message
     */
    public static  String validateActivation(String email){
        String result = null;
        String method = "isAccountActivatedAndroid.php";
        try {
            String url = Uri.parse(baseURLString+method)
                    .buildUpon()
                    .appendQueryParameter("email", email)

                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }

        return result;
    }

    /**
     * Checks for corret password in Data Base
     * @param email - associated email for it's password to be checked
     * @param password
     * @return JSON string format with status and message
     */
    public static  String validatePassword(String email, String password){
        String result = null;
        String method = "validatePasswordAndroid.php";
        try {
            String url = Uri.parse(baseURLString+method)
                    .buildUpon()
                    .appendQueryParameter("email", email)
                    .appendQueryParameter("password", password)
                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }

        return result;
    }

    /**
     * Gets user first name with given email
     * @param email - associated email for getting user's first name
     * @return JSON string format with status and message
     */
    public static  String getUserFirstName(String email){
        String result = null;
        String method = "getFirstNameAndroid.php";
        try {
            String url = Uri.parse(baseURLString+method)
                    .buildUpon()
                    .appendQueryParameter("email", email)
                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "-1";
            return result;
        }
        return result;
    }

    /**
     * Change password with given encrypted password and non ecnrypted password to inform user via email
     * @param params
     * @return JSON string format with status and message
     */
    public static  String changePassword(Map<String, String> params){
        String result = null;
        String method = "forgtoPasswordAndroid.php";
        try {
            String url = Uri.parse(baseURLString+method)
                    .buildUpon()
                    .appendQueryParameter("email", params.get("email"))
                    .appendQueryParameter("hashPassword", params.get("hashedPass"))
                    .appendQueryParameter("tempPass", params.get("tempPassword"))
                    .appendQueryParameter("firstName", params.get("firstName"))
                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }

        return result;
    }

    /**
     * Get existed transaction from Data Base for user with given email
     * @param email - associated email for getting transaction
     * @return JSON string format with status and message
     */
    public static  String getTransactions(String email){
        String result = null;
        String method = "getTransactionsAndroid.php";
        try {
            String url = Uri.parse(baseURLString+method)
                    .buildUpon()
                    .appendQueryParameter("email", email)
                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }
        return result;
    }

    /**
     * Check Payment Status for given Payment Id
     * @param emonPaymentID
     * @return JSON string format with status and message
     */
    public static  String checkStatusPayment(String emonPaymentID){
        String result = null;

        // MARK: Emon attributes
        String un = "firdavsusaxml";
        String pass = "52819380975";
        String secretKey = "WS#!@aq54&*kgj";

        String id = emonPaymentID;

        String keyBase = un+""+MD5(pass)+""+secretKey+""+id;
        String key;
        try {
            key = sha1(keyBase);
            String link = "http://emon.tj/xml2/check.aspx?un="+un+"&PaymentID="+id+"&key="+key;
            try {
                String url = Uri.parse(link)
                        .buildUpon()
                        .build().toString();
                String jsonString = getUrlString(url);
                result = jsonString;
            }
            catch (IOException ioe){
                result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
                return  result;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
        return result;
    }

    /**
     * Capture charge for Stripe with given token, amount, currency and description
     * @param params - Payment details to be captured for Stripe
     * @return JSON string format with status and message
     */
    public static  String captureCharge(Map<String, String> params){
        String result = null;
        String method = "captureChargeAndroid.php";

            String link = baseURLString+method;
            try {
                String url = Uri.parse(link)
                        .buildUpon()
                        .appendQueryParameter("stripeToken", params.get("tok"))
                        .appendQueryParameter("amount", params.get("amount"))
                        .appendQueryParameter("currency", "usd")
                        .appendQueryParameter("description", params.get("id"))
                        .build().toString();
                String jsonString = getUrlString(url);
                result = jsonString;
            }
            catch (IOException ioe){
                result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
                return result;
            }
        return result;
    }

    /**
     * Get USD->TJS Exchange rate
     * @return JSON string format with status and message
     */
    public static String getRate(){
        String result = null;

        String link = "http://www.apilayer.net/api/live?access_key=6a07a1f046e77670f78d4bf96c6497fb&currencies=TJS&format=1";
        try {
            String url = Uri.parse(link)
                    .buildUpon()
                    .appendQueryParameter("format", "json")
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return  result;
        }
        return result;
    }

    /**
     * Make Payment for Emon API
     * @param link link to be called for HTTP to Emon API
     * @return JSON string format with status and message
     */
    public static  String makePayment(String link){
        String result = null;
            try {
                String url = Uri.parse(link)
                        .buildUpon()
                        .build().toString();
                String jsonString = getUrlString(url);
                result = jsonString;
            }
            catch (IOException ioe){
                result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
                return  result;
            }
            return result;
    }

    /**
     * Store Payment with details to Data Base
     * @param params payment to be sotred
     * @return JSON string format with status and message
     */
    public static  String storePayment(Map<String, String> params){
        String result = null;
        String method = "storePaymentToDBAndroid.php";

        String link = baseURLString+method;
        try {
            String url = Uri.parse(link)
                    .buildUpon()
                    .appendQueryParameter("email", params.get("email"))
                    .appendQueryParameter("number", params.get("number"))
                    .appendQueryParameter("amount", params.get("amount"))
                    .appendQueryParameter("emonPaymentId", params.get("emonPaymentId"))
                    .appendQueryParameter("paymentId", params.get("paymentId"))
                    .appendQueryParameter("date", params.get("date"))
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }
        return result;
    }

    /**
     * Post Payment with given details, Check Status and Inform User
     * @param params
     * @return JSON string format with status and message
     */
    public static  String postPayment(Map<String, String> params){
        String result = null;
        String method = "checkPaymentStatusAndroid.php";

        String link = baseURLString+method;
        try {
            String url = Uri.parse(link)
                    .buildUpon()
                    .appendQueryParameter("firstName", params.get("firstName"))
                    .appendQueryParameter("email", params.get("email"))
                    .appendQueryParameter("number", params.get("number"))
                    .appendQueryParameter("amount", params.get("amount"))
                    .appendQueryParameter("emonPaymentId", params.get("emonPaymentId"))
                    .appendQueryParameter("paymentId", params.get("paymentId"))
                    .appendQueryParameter("capturedChargeId", params.get("capturedChargeId"))
                    .appendQueryParameter("statusCheckLink", params.get("statusCheckLink"))
                    .appendQueryParameter("date", params.get("date"))
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;

        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }
        return result;
    }

    /**
     * Delete Account with all it's transactions
     * @param email - asociated email for account to be deleted
     * @return JSON string format with status and message
     */
    public static  String deleteAccount(String email){
        String result = null;
        String method = "deleteAccountAndroid.php";

        String link = baseURLString+method;
        try {
            String url = Uri.parse(link)
                    .buildUpon()
                    .appendQueryParameter("email", email)
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }
        return result;
    }

    /**
     * Check remaining balance and inform if it it less than 200
     * @param rate to be converted to USD
     */
    public static void checkBalance(String rate){

        String balanceCheck = "http://emon.tj/xml2/balance.aspx?un=firdavsusaxml&key=b2902ca4d43835a3b8792507d08bf98e7f50ad02";

        // Sample format
        //<?xml version=\"1.0\" encoding=\"UTF-8\"?> <Answ><Status>1</Status><Balance>9999</Balance> <Comment>Ваш баланс</Comment></Answ>


        String link = balanceCheck;
        try {
            String url = Uri.parse(link)
                    .buildUpon()
                    .build().toString();
            String jsonString = getUrlString(url);
            String [] values = jsonString.split("Balance>");
            String value1 = values[1];
            String [] values1 = value1.split("</");
            String balance = values1[0];

            double balanceUsd = Double.parseDouble(rate)*Double.parseDouble(balance);

            if (balanceUsd < 200.0){
                informBalance(balanceUsd+"");
            }
        }
        catch (IOException ioe){

        }

    }

    /**
     * Change Password for user with given email
     * @param email - associated email for account's change password
     * @param password - encrypted password
     * @return
     */
    public static  String changePassword(String email, String password){
        String result = null;
        String method = "changePasswordAndroid.php";
        String pass =null;

        try {
            pass= sha1(password);
        }catch (Exception e){

        }

        String link = baseURLString+method;
        try {
            String url = Uri.parse(link)
                    .buildUpon()
                    .appendQueryParameter("email", email)
                    .appendQueryParameter("password", pass)
                    .build().toString();
            String jsonString = getUrlString(url);
            result = jsonString;
        }
        catch (IOException ioe){
            result = "{\"status\":\"-1\",\"message\":\"Unexpected error, contact info.gappza.info@gmail.com\"}";
            return result;
        }
        return result;
    }

    //region Endpoint Calls Helpers
    /**
     * Email myself to inform about less balance remained
     * @param bal
     */
    public static void informBalance(String bal){
        String method = "informBalanceAndroid.php";

        String link = baseURLString+method;
        try {
            String url = Uri.parse(link)
                    .buildUpon()
                    .appendQueryParameter("balance", bal)
                    .build().toString();
            String jsonString = getUrlString(url);
        }
        catch (IOException ioe){
        }
    }

    /**
     * Encrypt a string to MD5 format
     * @return encrypted string
     * @throws NoSuchAlgorithmException
     */
    static String MD5(String md5) {
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

    /**
     * Encrypt a string to sha1 format
     * @param input - string to be encrypted
     * @return encrypted string
     * @throws NoSuchAlgorithmException
     */
    static String sha1(String input) throws NoSuchAlgorithmException {
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
