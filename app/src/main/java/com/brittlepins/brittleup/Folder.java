package com.brittlepins.brittleup;

import androidx.annotation.NonNull;

public class Folder {
    private String mId;
    private String mName;

    public Folder(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public String getName() { return mName; }

    @NonNull
    @Override
    public String toString() {
        return mName;
    }
}
