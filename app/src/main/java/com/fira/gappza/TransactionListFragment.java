package com.fira.gappza;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;


/**
 * Created by firdavsiimajidzoda on 11/27/16.
 */

public class TransactionListFragment extends android.support.v4.app.Fragment  {

    //region Fields
    // Saving directory name
    private final String PREFS_NAME = "MyPrefsFile";

    private RecyclerView mTransactionRecyclerView;
    private TransactionAdapter mAdapter;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        mTransactionRecyclerView = (RecyclerView) view
                .findViewById(R.id.transaction_recycler_view);
        mTransactionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            new getTransactions().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new getTransactions().execute();
        }
        return view;
    }

    /**
     * Updates UI and refresh Fragment details
     * @param json
     */
    private void updateUI(String json) {
        TransactionLab transactionLab = TransactionLab.get(getActivity(), json);
        List<Transaction> transactions = transactionLab.getTransactions();
        mAdapter = new TransactionAdapter(transactions);
        mTransactionRecyclerView.setAdapter(mAdapter);
    }


    /**
     *  Transactoin Holder, hods subviews in Fragment and binds them
     */
    private class TransactionHolder extends RecyclerView.ViewHolder {
        private TextView mPhoneNumberTextView;
        private TextView mDateTextView;
        private TextView mAmountTextView;
        private TextView mStatusTextView;

        private Transaction mTransaction;

        public TransactionHolder(View itemView) {
            super(itemView);

            mPhoneNumberTextView = (TextView)itemView.findViewById(R.id.phoneNumberTransactionList_textView);
            mDateTextView = (TextView)itemView.findViewById(R.id.dateTransactionList_textView);
            mAmountTextView = (TextView)itemView.findViewById(R.id.amountTransactionList_textView);
            mStatusTextView = (TextView)itemView.findViewById(R.id.statusTransactionList_textView);

        }

        public void bindTranaction(Transaction transaction) {
            mTransaction = transaction;
            mPhoneNumberTextView.setText(mTransaction.getNumber());
            mDateTextView.setText(mTransaction.getDate());
            mAmountTextView.setText(mTransaction.getAmount());
            mStatusTextView.setText(mTransaction.getStatus());
        }

    }

    /**
     *  Transaction Adapter
     */
    private class TransactionAdapter extends RecyclerView.Adapter<TransactionHolder> {
        private  List<Transaction> mTransactions;

        public TransactionAdapter(List<Transaction> transactions){
            mTransactions = transactions;
        }

        @Override
        public TransactionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_transaction_item, parent, false);
            return  new TransactionHolder(view);
        }

        @Override
        public void onBindViewHolder(TransactionHolder holder, int position) {
            Transaction transaction = mTransactions.get(position);
            holder.bindTranaction(transaction);
        }

        @Override
        public int getItemCount() {
            return  mTransactions.size();
        }
    }

    /**
     * Loads current user's email
     * @return email string
     */
    private String loadCurrentUserEmail(){
        SharedPreferences settings = this.getActivity().getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString("currentUserEmail", "");
        return value;
    }

    /**
     *  Execute get transactions AsyncTask
     */
    private class getTransactions extends AsyncTask<String,Void,Void>{

        String jsonString;
        JSONObject jsonBody = null;

        @Override
        protected Void doInBackground(String... params) {
            jsonString = GappzaAPI.getTransactions(loadCurrentUserEmail());
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                jsonBody = new JSONObject(jsonString);
                String status = jsonBody.getString("status");

                if (status.matches("1")){
                    jsonString = jsonBody.getJSONArray("transactions").toString();
                    updateUI(jsonString);



                } else if (status.matches("-1")){

                } else {

                }
            } catch (JSONException e) {

            }
        }

    }

}

