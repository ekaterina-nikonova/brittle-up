package com.brittlepins.brittleup;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FolderTest {
    Folder mFolder;

    @Before
    public void setUp() {
        mFolder = new Folder("folder-id", "folder-name");
    }

    @Test
    public void constructorShouldCreateFolder() {
        assertEquals("folder-id", mFolder.getId());
        assertEquals("folder-name", mFolder.getName());
    }

    @Test
    public void toStringShouldReturnName() {
        assertEquals("folder-name", mFolder.toString());
    }
}
