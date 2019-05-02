package com.test.jangleproducer.model;

import java.io.Serializable;
import java.util.ArrayList;

public class CommonDto  implements Serializable {

    private String ownerUserUuid;
    private String jangleUuid;
    private String ownerToken;
    private ArrayList<String> usersToken;
    private int count;

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
