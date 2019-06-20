package com.brittlepins.brittleup;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DriveServiceTest {
    DriveService mDriveService;
    Drive mDrive = mock(Drive.class, RETURNS_DEEP_STUBS);
    Drive mDriveFailing = mock(Drive.class, RETURNS_DEEP_STUBS);

    @Captor
    ArgumentCaptor<String> argumentCaptor;

    @Before
    public void setUp() throws Exception {
        mDriveService = spy(new DriveService(mDrive));
        when(mDrive.files().list().setQ(anyString()).setFields(anyString()).execute())
                .thenReturn(mockFileList());
        when(mDrive.files().create(any()).execute()).thenReturn(mockFile_1());
        when(mDrive.files().update(anyString(), any(), any()).execute()).thenReturn(null);
        when(mDrive.files().delete(anyString()).execute()).then(answer -> null);

        when(mDriveFailing.files().list().setQ(anyString()).setFields(anyString()).execute())
                .thenThrow(IOException.class);
        when(mDriveFailing.files().create(any()).execute()).thenReturn(null);
    }

    @Test
    public void methodsShouldBeCalledInCallbacks() throws IOException {
        mDriveService.createFile();
        verify(mDriveService, times(1)).create();

        mDriveService.listAllFolders();
        verify(mDriveService, times(1)).list();

        mDriveService.createFolder("parent-id", "folder-name");
        verify(mDriveService).findOrCreate(argumentCaptor.capture(), argumentCaptor.capture());
        List<String> args = argumentCaptor.getAllValues();
        assertEquals("parent-id", args.get(0));
        assertEquals("folder-name", args.get(1));
    }

    @Test
    public void listShouldReturnFolders() throws IOException {
        List<File> files = mDriveService.list();
        assertEquals(2, files.size());
        assertEquals("test-id-1", files.get(0).getId());
        assertEquals("test-name-1", files.get(0).getName());
    }

    @Test
    public void createShouldReturnFile() throws IOException {
        File file = mDriveService.create();
        assertEquals("test-id-1", file.getId());
        assertEquals("test-name-1", file.getName());
    }

    @Test(expected = IOException.class)
    public void createShouldThrowExceptionWhenResultNull() throws IOException {
        DriveService failingService = new DriveService(mDriveFailing);
        failingService.create();
    }

    @Test
    public void saveFileShouldSucceed() {
        byte[] content = new byte[0];
        mDriveService.saveFile("test-file", "test-mime", content);
    }

    @Test
    public void deleteFileShouldSucceed() {
        mDriveService.deleteFile("test-file");
    }

    @Test
    public void setRootFolderIdShouldWork() {
        String id = "test-root-folder-id";
        mDriveService.setRootFolderId(id);
        assertEquals(mDriveService.mRootFolderId, id);
    }

    @Test
    public void setUploadFolderIdShouldWork() {
        String id = "test-upload-folder-id";
        mDriveService.setUploadFolderId(id);
        assertEquals(mDriveService.mUploadFolderId, id);
    }

    @Test
    public void findOrCreate_ShouldCreateAFile_IfDoesNotExist() throws IOException {
        when(mDrive.files().list().setQ(anyString()).setFields(anyString()).execute())
                .thenReturn(new FileList().setFiles(new ArrayList<>()));
        when(mDrive.files().create(any()).setFields(anyString()).execute())
                .thenReturn(new File().setName("new-file-name").setId("new-file-id"));

        File newFolder = mDriveService.findOrCreate("parent-id", "folder-name");
        assertEquals(newFolder.getName(), "new-file-name");
        assertEquals(newFolder.getId(), "new-file-id");
    }

    @Test
    public void findOrCreate_ShouldFindAFile_IfExists() throws IOException {
        when(mDrive.files().list().setQ(anyString()).setFields(anyString()).execute())
                .thenReturn(mockFileList());

        File foundFolder = mDriveService.findOrCreate("parent-id", "folder-name");
        assertEquals(foundFolder.getName(), "test-name-1");
        assertEquals(foundFolder.getId(), "test-id-1");
    }

    private

    FileList mockFileList() {
        ArrayList<File> files = new ArrayList<>();
        files.add(mockFile_1());
        files.add(mockFile_2());
        FileList fileList = new FileList();
        fileList.setFiles(files);
        return fileList;
    }

    File mockFile_1() {
        return new File().setId("test-id-1").setName("test-name-1");
    }

    File mockFile_2() {
        return new File().setId("test-id-2").setName("test-name-2");
    }
}
