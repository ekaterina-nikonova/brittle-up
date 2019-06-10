package com.brittlepins.brittleup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final String TAG = "MainActivity";
    private final int CAMERA_PERMISSION_CODE = 1;
    static final int RC_SIGN_OUT = 2;

    TextureView mTextureView;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera(width, height);
            } catch (RuntimeException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    static GoogleSignInAccount mAccount;
    private CameraService mCameraService;
    static DriveService mDriveService;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private ArrayList<Folder> mFolders = new ArrayList<>();

    private Spinner mSpinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar mToolbar;
    private FloatingActionButton mUploadButton;
    static private ImageView mUploadIndicatorImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpinner = findViewById(R.id.folderSelectionSpinner);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mTextureView = findViewById(R.id.textureView);
        mToolbar = findViewById(R.id.toolbar);
        mUploadIndicatorImageView = findViewById(R.id.uploadIndicatorImageView);

        mUploadButton = findViewById(R.id.uploadButton);
        mUploadButton.hide();

        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCameraService = new CameraService(this, mTextureView);
        mCameraService.startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            if (mDriveService == null) {
                DriveSignInService driveSignIn = new DriveSignInService(this);
                driveSignIn.driveAuth(account);
            }

            mAccount = account;
            mSwipeRefreshLayout.setOnRefreshListener(() -> fetchFolders()
                    .addOnCompleteListener(aVoid -> mSwipeRefreshLayout.setRefreshing(false))
                    .addOnFailureListener(aVoid -> mSwipeRefreshLayout.setRefreshing(false))
            );

            fetchFolders();
        }

    }

    private Task<Void> fetchFolders() {
        return Tasks.call(mExecutor, () -> {
            ArrayAdapter<Folder> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    mFolders
            );
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setOnItemSelectedListener(this);

            mDriveService.setUploadFolderId(null);
            mDriveService.listAllFolders()
                .addOnSuccessListener(folders -> {
                    mFolders.clear();
                    for (File f: folders) {
                        mFolders.add(new Folder(f.getId(), f.getName()));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(error -> Log.e(TAG, "Failed to fetch folders: " + error.getMessage()));
            return null;
        });
    }

    @Override
    protected void onPause() {
        mCameraService.closeCamera();
        mCameraService.stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access to camera is required.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mDriveService.setUploadFolderId(((Folder) parent.getItemAtPosition(position)).getId());
        if (mDriveService.mUploadFolderId != null) {
            mUploadButton.show();
        } else {
            mUploadButton.hide();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "Nothing selected.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOutMenuItem:
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.question_icon)
                        .setTitle(getString(R.string.log_out_dialog_title))
                        .setMessage(getString(R.string.log_out_dialog_message) +
                                " " + mAccount.getDisplayName() +
                                "\n(" + mAccount.getEmail() + ")")
                        .setPositiveButton(getString(R.string.log_out_dialog_positive), (dialog, which) -> {
                            Intent intent = new Intent(this, SignInActivity.class);
                            intent.putExtra("request_code", RC_SIGN_OUT);
                            startActivity(intent);
                        })
                        .setNegativeButton(getString(R.string.log_out_dialog_negative), (dialog, which) -> {})
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }

    public void takePicture(View view) {
        mCameraService.takePicture();
    }

    public void addLabel(View view) {
        Intent intent = new Intent(this, AddLabelActivity.class);
        startActivity(intent);
    }

    private

    void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            mCameraService.openCamera(width, height, manager);
        }
    }

    static void createFile(byte[] content) {
        final String TAG = "MainActivity createFile";
        if (mDriveService != null) {
            Log.i(TAG, "Creating file");

            mDriveService.createFile()
                    .addOnSuccessListener(file -> saveFile(file.getId(), file.getMimeType(), content))
                    .addOnFailureListener(ex -> Log.e(TAG, "Could not create file", ex));
        }
    }

    static void saveFile(String fileId, String mimeType, byte[] content) {
        final String TAG = "MainActivity saveFile";
        if (mDriveService != null) {
            mDriveService.saveFile(fileId, mimeType, content)
                .addOnSuccessListener(aVoid -> {
                    mUploadIndicatorImageView.setImageResource(R.drawable.success_icon);
                })
                .addOnFailureListener(ex -> {
                    mUploadIndicatorImageView.setImageResource(R.drawable.error_icon);
                    mDriveService.deleteFile(fileId);
                    Log.e(TAG, "Could not upload file", ex);
                    ex.printStackTrace();
                });
        }
    }

    void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.CAMERA },
                CAMERA_PERMISSION_CODE
        );
    }
}
