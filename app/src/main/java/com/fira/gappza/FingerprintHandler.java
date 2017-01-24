package com.fira.gappza;

/**
 * Created by firdavsiimajidzoda on 11/30/16.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class FingerprintHandler extends
        FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context appContext;
    public boolean isSucceed = false;
    public LogInActivity loginActivity;

    public FingerprintHandler(Context context) {
        appContext = context;
    }

    public void startAuth(FingerprintManager manager,
                          FingerprintManager.CryptoObject cryptoObject, LogInActivity loginActivity) {
        this.loginActivity = loginActivity;
        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        Toast.makeText(appContext,
                "Authentication help\n" + helpString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(appContext,
                "Authentication failed.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
        isSucceed = true;

        Toast.makeText(appContext,
                "Authentication succeeded.",
                Toast.LENGTH_SHORT).show();
                startIntent();
    }

    public void startIntent(){
        loginActivity.saveCurrentUserEmail();
        Intent accountActivity = new Intent(loginActivity, AccountActivity.class);
        loginActivity.startActivity(accountActivity);
        loginActivity.finish();
    }
}
