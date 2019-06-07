package com.brittlepins.brittleup;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class DriveSignInService {
    private final String TAG = "DriveSignInService";
    private Context ctx;

    DriveSignInService(Context ctx) {
        this.ctx = ctx;
    }

    void signInToDrive(Intent resultIntent) {
        GoogleSignIn.getSignedInAccountFromIntent(resultIntent)
                .addOnSuccessListener(account -> driveAuth(account))
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Failed to sign in to Google Drive", exception));
    }

    void driveAuth(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                ctx, Collections.singleton(DriveScopes.DRIVE_FILE));

        credential.setSelectedAccount(account.getAccount());

        Drive drive = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential
        ).setApplicationName("Brittle Up").build();

        MainActivity.mDriveService = new DriveService(drive);
        MainActivity.mDriveService.createFolder("root", "Brittle Up")
                .addOnSuccessListener(folder -> MainActivity.mDriveService.setRootFolderId(folder.getId()))
                .addOnFailureListener(error -> Log.e(TAG, "Could not create folder: " + error.getMessage()));
    }
}
