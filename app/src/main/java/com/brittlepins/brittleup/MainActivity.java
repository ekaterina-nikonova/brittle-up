package com.brittlepins.brittleup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private EditText mEditText;
    private Button mUploadButton;

    static DriveService mDriveService;
    GoogleSignInAccount mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.editText);
        mUploadButton = findViewById(R.id.button);
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

    public void upload(View view) {
        String text = mEditText.getText().toString();
        createFile(text);
    }

    private

    void createFile(String text) {
        if (mDriveService != null) {
            Log.i(TAG, "Creating file");

            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String filename = formatter.format(date);

            mDriveService.createFile()
                    .addOnSuccessListener(fileId -> saveFile(fileId, filename, text))
                    .addOnFailureListener(ex -> Log.e(TAG, "Could not create file", ex));
        }
    }

    void saveFile(String id, String name, String content) {
        if (mDriveService != null) {
            Log.i(TAG, "Uploading file");
            mDriveService.saveFile(id, name, content)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                    mEditText.setText("");
                })
                .addOnFailureListener(ex -> Log.e(TAG, "Could not upload file", ex));
        }
    }


}
