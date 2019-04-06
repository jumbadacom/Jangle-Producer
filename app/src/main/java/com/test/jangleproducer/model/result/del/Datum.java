package com.test.jangleproducer.model.result.del;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.test.jangleproducer.model.result.vote.Completion;

import java.util.List;

public class Datum {

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
