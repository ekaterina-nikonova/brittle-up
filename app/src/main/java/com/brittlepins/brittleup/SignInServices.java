package com.brittlepins.brittleup;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class SignInServices {
    private final String TAG = "SignInServices";
    private Context ctx;

    SignInServices(Context ctx) {
        this.ctx = ctx;
    }

    Context getCtx() {
        return ctx;
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
