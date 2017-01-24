package com.fira.gappza;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TermsAndConditionsActivity extends AppCompatActivity {

    //region Fields
    Button back = null;
    Bundle bundle = null;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        // Get bundle to save Customer with given details
        getBundle();

        // Configure Back Button
        backButtonConf();
    }

    /**
     *  Get Bundle from SignUpActivity.java to save Customer with given details
     */
    private void getBundle(){
        bundle = this.getIntent().getExtras();
    }

    /**
     *  Configure Back Button
     */
    private void backButtonConf(){
        back = (Button)findViewById(R.id.backFromTermsAndConditionsActivityToSignUpActivity_button);
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent signUpActivityInent = new Intent();
                Bundle b = bundle;
                signUpActivityInent.putExtras(b);
                signUpActivityInent.setClass(getApplicationContext(), SignUpActivity.class);
                startActivity(signUpActivityInent);
                finish();
            }
        });
    }
}
