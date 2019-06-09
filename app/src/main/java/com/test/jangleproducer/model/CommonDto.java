package com.test.jangleproducer.model;

import java.io.Serializable;
import java.util.ArrayList;

public class CommonDto implements Serializable {

    private boolean jangleHasCompletion;
    private String ownerUserUuid;
    private String jangleUuid;
    private String ownerToken;
    private ArrayList<String> usersToken;
    private int completionCountOfTheJangle;
    private int uploadCompletionCounter;


    public String getOwnerUserUuid() {
        return ownerUserUuid;
    }

    public void setOwnerUserUuid(String ownerUserUuid) {
        this.ownerUserUuid = ownerUserUuid;
    }

    public String getJangleUuid() {
        return jangleUuid;
    }

    public void setJangleUuid(String jangleUuid) {
        this.jangleUuid = jangleUuid;
    }

    public String getOwnerToken() {
        return ownerToken;
    }

    public void setOwnerToken(String ownerToken) {
        this.ownerToken = ownerToken;
    }

    public ArrayList<String> getUsersToken() {
        return usersToken;
    }

    public void setUsersToken(ArrayList<String> usersToken) {
        this.usersToken = usersToken;
    }

    public int getCompletionCountOfTheJangle() {
        return completionCountOfTheJangle;
    }

    public void setCompletionCountOfTheJangle(int completionCountOfTheJangle) {
        this.completionCountOfTheJangle = completionCountOfTheJangle;
    }

    public void setJangleHasCompletion(boolean jangleHasCompletion) {
        this.jangleHasCompletion = jangleHasCompletion;
    }

    public boolean isJangleHasCompletion() {
        return jangleHasCompletion;
    }

    public int getUploadCompletionCounter() {
        return uploadCompletionCounter;
    }

    public void setUploadCompletionCounter(int uploadCompletionCounter) {
        this.uploadCompletionCounter = uploadCompletionCounter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonDto{");
        sb.append("jangleHasCompletion=").append(jangleHasCompletion);
        sb.append(", ownerUserUuid='").append(ownerUserUuid).append('\'');
        sb.append(", jangleUuid='").append(jangleUuid).append('\'');
        sb.append(", ownerToken='").append(ownerToken).append('\'');
        sb.append(", usersTokenSize=").append(usersToken.size());
        sb.append(", completionCountOfTheJangle=").append(completionCountOfTheJangle);
        sb.append(", uploadCompletionCounter=").append(uploadCompletionCounter);
        sb.append('}');
        return sb.toString();
    }
}
