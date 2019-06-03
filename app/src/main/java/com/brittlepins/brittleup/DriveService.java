package com.brittlepins.brittleup;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveService {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDrive;

    public DriveService(Drive drive) {
        mDrive = drive;
    }

    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("image/jpeg")
                    .setName("test-image");

            File imageFile = mDrive.files().create(metadata).execute();

            if (imageFile == null) {
                throw new IOException("Null result when requesting image file creation.");
            }

            return imageFile.getId();
        });
    }

    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File().setName(name);
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
            mDrive.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }
}
