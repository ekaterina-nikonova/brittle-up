package com.brittlepins.brittleup;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveService {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDrive;
    String mUploadFolderId;
    String mRootFolderId;

    public DriveService(Drive drive) {
        mDrive = drive;
    }

    public Task<File> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList(mUploadFolderId))
                    .setMimeType("image/jpeg")
                    .setName(String.valueOf(new Date().getTime()));

            File imageFile = mDrive.files().create(metadata).execute();

            if (imageFile == null) {
                throw new IOException("Null result when requesting image file creation.");
            }

            return imageFile;
        });
    }

    public Task<Void> saveFile(String fileId, String mimeType, byte[] content) {
        return Tasks.call(mExecutor, () -> {
            mDrive.files().update(
                    fileId,
                    new File(),
                    new InputStreamContent(mimeType, new ByteArrayInputStream(content))
            ).execute();
            return null;
        });
    }

    public Task<Void> deleteFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            mDrive.files().delete(fileId).execute();
            return null;
        });
    }

    public Task<File> createFolder(String parentId, String name) {
        return Tasks.call(mExecutor, () -> {
            String query = "name='"
                    + name
                    + "' and mimeType='application/vnd.google-apps.folder' and trashed=false";
            FileList result = mDrive.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .execute();

            if (result.getFiles().isEmpty()) {
                File metadata = new File()
                        .setName(name)
                        .setMimeType("application/vnd.google-apps.folder")
                        .setParents(Collections.singletonList(parentId));
                File file = mDrive.files().create(metadata).setFields("id, name").execute();
                return file;
            } else {
                Log.i("CREATE_FOLDER", "Found files: " + result.getFiles().size());
                return result.getFiles().get(0);
            }
        });
    }

    public Task<List<File>> listAllFolders() {
        return Tasks.call(mExecutor, () -> {
            List<File> folders = new ArrayList<>();
            FileList result = mDrive.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false and parents='" + mRootFolderId + "'")
                    .setFields("files(id, name)")
                    .execute();
            folders.addAll(result.getFiles());
            return folders;
        });
    }

    public void setRootFolderId(String id) {
        mRootFolderId = id;
    }

    public void setUploadFolderId(String id) {
        mUploadFolderId = id;
    }

}
