package com.test.medicalpanel2.Model;

import com.google.firebase.firestore.FieldValue;

public class Notification {
    private String uid,title,content;
    private boolean read;
    private FieldValue serverTimestamp;

    public Notification() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public FieldValue getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(FieldValue serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
}
