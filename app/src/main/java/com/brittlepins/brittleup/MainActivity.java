package com.brittlepins.brittleup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int CAMERA_PERMISSION_CODE = 1;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private Handler mBackgroundHandler;
    private String mCameraId;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;
    private File mFile;
    private TextureView mTextureView;
    private CaptureRequest mPreviewRequest;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private Size mPreviewSize;
    private int mState = STATE_PREVIEW;

    private CameraDevice mCameraDevice;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
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

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    break;
                }
                case STATE_WAITING_LOCK: {
                    // capture still picture or run pre-capture sequence
                }
                case STATE_WAITING_PRECAPTURE: {
                    // change state to waiting non pre-capture
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // capture still picture
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }
    };

    static DriveService mDriveService;
    GoogleSignInAccount mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = findViewById(R.id.textureView);
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
            if (mTextureView.isAvailable()) {
                openCamera(mTextureView.getWidth(), mTextureView.getHeight());
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }
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

    public void upload(View view) {
        createFile("");
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
                })
                .addOnFailureListener(ex -> Log.e(TAG, "Could not upload file", ex));
        }
    }

    void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            setUpCameraOutputs(width, height);
            configureTransform(width, height);

            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);

            try {
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MICROSECONDS)) {
                    throw new RuntimeException("Time out waiting to unlock the camera.");
                }
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Could not access camera: " + e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException("Unlocking the camera has been interrupted");
            }
        }
    }

    void configureTransform(int width, int height) {
        if (mTextureView == null || mPreviewSize == null) {
            return;
        }
    }

    void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (mCameraDevice == null) {
                                return;
                            }

                            try {
                                mCaptureSession = session;
                                mPreviewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                );
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Could not access the camera: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Could not capture the picture.", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Could not access the camera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.CAMERA },
                CAMERA_PERMISSION_CODE
        );
    }

    void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            for(String cameraId: manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                Integer direction = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (direction != null && direction == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                mImageReader = ImageReader.newInstance(600, 600, ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Could not access camera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class ImageSaver implements Runnable {
        private final String TAG = "ImageSaver";
        private final Image mImage;
        private final File mFile;

        ImageSaver(Image img, File file) {
            mImage = img;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Could not write to output stream: " + e.getMessage());
                e.printStackTrace();
            } finally {
                mImage.close();
                if(output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close output stream: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
