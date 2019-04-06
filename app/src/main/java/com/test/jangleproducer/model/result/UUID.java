package com.test.jangleproducer.model.result;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UUID {
    @Expose
    @SerializedName("uuid")
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
