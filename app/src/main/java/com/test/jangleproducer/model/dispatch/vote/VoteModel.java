package com.test.jangleproducer.model.dispatch.vote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VoteModel {


    @SerializedName("approve")
    @Expose
    private Boolean approve;
    @SerializedName("completionUuid")
    @Expose
    private String completionUuid;
    @SerializedName("score")
    @Expose
    private Integer score;

    public VoteModel(Boolean approve, String completionUuid, Integer score) {
        this.approve = approve;
        this.completionUuid = completionUuid;
        this.score = score;
    }

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    public String getCompletionUuid() {
        return completionUuid;
    }

    public void setCompletionUuid(String completionUuid) {
        this.completionUuid = completionUuid;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
