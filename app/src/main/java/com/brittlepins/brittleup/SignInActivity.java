package com.brittlepins.brittleup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class SignInActivity extends AppCompatActivity {
    private final String TAG = "SignInActivity";
    private final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK && resultIntent != null) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(resultIntent);
                signInToDrive(resultIntent);
                returnToMainActivity(task.getResult());
            }
        }
    }

    public void signInWithGoogle(View view) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private

    void signInToDrive(Intent resultIntent) {
        GoogleSignIn.getSignedInAccountFromIntent(resultIntent)
                .addOnSuccessListener(account -> {
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                            this, Collections.singleton(DriveScopes.DRIVE_FILE));

                    credential.setSelectedAccount(account.getAccount());

                    Drive drive = new Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential
                    ).setApplicationName("Brittle Up").build();

                    MainActivity.mDriveService = new DriveService(drive);
                    MainActivity.mDriveService.createFolder("root", "Brittle Up")
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Created folder"))
                            .addOnFailureListener(error -> Log.e(TAG, "Could not create folder: " + error.getMessage()));
                })
        .addOnFailureListener(exception ->
                Log.e(TAG, "Failed to sign in to Google Drive", exception));
    }

    void returnToMainActivity(GoogleSignInAccount account) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Google account", account);
        startActivity(intent);
    }
}
