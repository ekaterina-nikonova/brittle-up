package com.brittlepins.brittleup;

import com.google.api.services.drive.Drive;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DriveServiceTest {
    DriveService mDriveService;
    Drive mDrive = mock(Drive.class);

    @Before
    public void setUp() throws Exception {
        mDriveService = new DriveService(mDrive);
    }

    @Test
    public void listAllFolders() {}
}
