package com.fira.gappza;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by firdavsiimajidzoda on 11/27/16.
 */

public class TransactionLab {

    //region Fields
    private static TransactionLab sTransactionLab;
    private ArrayList<Transaction> mTransaction;
    //endregion

    /**
     * Create TransactionLab
     * @param context
     * @param jsonBody
     * @return transactionLab
     */
    public static TransactionLab get(Context context, String jsonBody) {
         sTransactionLab = new TransactionLab(context, jsonBody);

        return sTransactionLab;
    }

    /**
     * Configure TransactionLab
     * @param context
     * @param jsonBody
     */
    private TransactionLab(Context context, String jsonBody) {
        mTransaction = new ArrayList<>();


        try {
            JSONArray jsonArray = new JSONArray(jsonBody);

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject transaction = jsonArray.getJSONObject(i);
                String number = transaction.getString("phoneNumber");
                String amount = transaction.getString("amount");
                String date = transaction.getString("date");
                String status = transaction.getString("status");

                Transaction trans = new Transaction(number, amount, date, status);
                mTransaction.add(trans);
            }

        } catch (JSONException e) {

        }
    }

    /**
     * @return Transactions
     */
    public List<Transaction> getTransactions() {
        return mTransaction;
    }

}
