package com.brittlepins.brittleup;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Before
    public void setUp() throws Exception {
        mDriveService = new DriveService(mDrive);
        when(mDrive.files().list().setQ(anyString()).setFields(anyString()).execute())
                .thenReturn(mockFileList());
        when(mDriveFailing.files().list().setQ(anyString()).setFields(anyString()).execute())
                .thenThrow(IOException.class);
    }

    @Test
    public void listAllFoldersShouldRunList() throws IOException {
        DriveService spyService = spy(new DriveService(mDrive));
        spyService.listAllFolders();
        verify(spyService, times(1)).list();
    }

    @Test
    public void listShouldReturnFolders() throws IOException {
        List<File> files = mDriveService.list();
        assertEquals(1, files.size());
        assertEquals("test-id", files.get(0).getId());
        assertEquals("test-name", files.get(0).getName());
    }


    private

    FileList mockFileList() {
        File file = new File();
        file.setId("test-id");
        file.setName("test-name");
        ArrayList<File> files = new ArrayList<>();
        files.add(file);
        FileList fileList = new FileList();
        fileList.setFiles(files);
        return fileList;
    }
}
