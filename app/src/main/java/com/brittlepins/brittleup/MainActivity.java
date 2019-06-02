package com.brittlepins.brittleup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class MainActivity extends AppCompatActivity {
    GoogleSignInAccount mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAccount = getIntent().getParcelableExtra("Google account");

        if (mAccount == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            if (mAccount != null) {
                Log.i("Get account from intent", mAccount.getEmail());
            } else {
                Log.i("Get account from intent", "no account");
            }
        }
    }
}
