package com.example.fertilisense;

import android.net.Uri;

public class Message {
    public static final String SENT_BY_ME = "me";
    public static final String SENT_BY_BOT = "bot";

    private String message;
    private String sentBy;
    private String senderId;  // New field for sender ID
    private String senderName;  // New field for sender name
    private Uri userImage;

    // Constructor for messages with sender ID and name
    public Message(String message, String sentBy, String senderId, String senderName) {
        this.message = message;
        this.sentBy = sentBy;
        this.senderId = senderId;
        this.senderName = senderName;
    }

    public Message (String message, String sentBy, String senderId, String senderName, Uri userImage){
        this.message = message;
        this.sentBy = sentBy;
        this.senderId = senderId;
        this.senderName = senderName;
        this.userImage = userImage;
    }

    // Constructor for messages without sender ID and name
    public Message(String message, String sentBy) {
        this(message, sentBy, null, null);
    }

    // Getters and setters for the fields
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public Uri getUserImage() {
        return userImage; // Added getter for user image
    }
}
