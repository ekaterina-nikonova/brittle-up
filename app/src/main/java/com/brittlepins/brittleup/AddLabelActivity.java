package com.brittlepins.brittleup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddLabelActivity extends AppCompatActivity {
    private final String TAG = "AddLabelActivity";
    private EditText mLabelEditText;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_label);
        mLabelEditText = findViewById(R.id.newLabelEditText);
        mSaveButton = findViewById(R.id.saveButton);

        mLabelEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    mSaveButton.setEnabled(false);
                } else {
                    mSaveButton.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSaveButton.setEnabled(!mLabelEditText.getText().toString().isEmpty());
    }

    public void save(View view) {
        MainActivity.mDriveService
                .createFolder(MainActivity.mDriveService.mRootFolderId,
                        mLabelEditText.getText().toString().toLowerCase().trim())
                .addOnSuccessListener(folder -> {
                    Toast.makeText(this, "Created label " + folder.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not create label.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Could not create folder: " + e.getMessage());
                    e.printStackTrace();
                });
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void cancel(View view) {
        finish();
    }
}
