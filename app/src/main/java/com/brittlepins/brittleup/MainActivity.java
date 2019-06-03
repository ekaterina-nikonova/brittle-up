package com.brittlepins.brittleup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class MainActivity extends AppCompatActivity {
    static DriveService mDriveService;
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
            Toast.makeText(this, "Hello, " + mAccount.getDisplayName(), Toast.LENGTH_SHORT).show();
        }
    }

    private

    void saveFile() {}
}
