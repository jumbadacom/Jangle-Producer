package com.test.jangleproducer.model.result.del;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeleteUserResponse {

    @SerializedName("login")
    @Expose
    private String login;
    @SerializedName("authorities")
    @Expose
    private List<String> authorities = null;
    @SerializedName("uuid")
    @Expose
    private String uuid;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
