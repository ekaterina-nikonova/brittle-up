package com.brittlepins.brittleup;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveService {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDrive;

    public DriveService(Drive drive) {
        mDrive = drive;
    }

    public Task<File> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("image/jpeg")
                    .setName(String.valueOf(new Date().getTime()));

            File imageFile = mDrive.files().create(metadata).execute();

            if (imageFile == null) {
                throw new IOException("Null result when requesting image file creation.");
            }

            return imageFile;
        });
    }

    public Task<Void> saveFile(File file, byte[] content) {
        return Tasks.call(mExecutor, () -> {
            String mimeType = file.getMimeType();
            mDrive.files().update(
                    file.getId(),
                    new File(),
                    new InputStreamContent(mimeType, new ByteArrayInputStream(content))
            ).execute();
            return null;
        });
    }
}
