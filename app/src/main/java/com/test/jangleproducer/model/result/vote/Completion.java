package com.test.jangleproducer.model.result.vote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Completion {



    @SerializedName("uuid")
    @Expose
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
