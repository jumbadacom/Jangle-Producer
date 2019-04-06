package com.test.jangleproducer.model.dispatch;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadVM {

    private String title;
    private String description;
    private String detailDescription;
    private boolean publish;
    private DocType jangleType;
    private FileType fileType;
    @Expose
    @SerializedName(value = "referanceJangleUuid")
    private String referanceJangleUuid;
    private boolean expireIn24Hours;
    private Integer duration;


    //make jangle
    public UploadVM(String title, DocType jangleType){
    setTitle(title);
    setDescription(title+" description");
    setPublish(true);
    setJangleType(jangleType);
    setFileType(FileType.IMAGE);
    setExpireIn24Hours(false);
    }

    //make completion
    public UploadVM(String title, DocType jangleType, String jangleUuid, int counter){
        setTitle(title);
        setDescription(title+" desc="+counter);
        setPublish(true);
        setJangleType(jangleType);
        setFileType(FileType.IMAGE);
        setExpireIn24Hours(false);
        setReferanceJangleUuid(jangleUuid);

    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailDescription() {
        return detailDescription;
    }

    public void setDetailDescription(String detailDescription) {
        this.detailDescription = detailDescription;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public DocType getJangleType() {
        return jangleType;
    }

    public void setJangleType(DocType jangleType) {
        this.jangleType = jangleType;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getReferanceJangleUuid() {
        return referanceJangleUuid;
    }

    public void setReferanceJangleUuid(String referanceJangleUuid) {
        this.referanceJangleUuid = referanceJangleUuid;
    }

    public boolean isExpireIn24Hours() {
        return expireIn24Hours;
    }

    public void setExpireIn24Hours(boolean expireIn24Hours) {
        this.expireIn24Hours = expireIn24Hours;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UploadVm{");
        sb.append("title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", detailDescription='").append(detailDescription).append('\'');
        sb.append(", publish=").append(publish);
        sb.append(", jangleType=").append(jangleType);
        sb.append(", fileType=").append(fileType);
        sb.append(", referanceJangleUuid='").append(referanceJangleUuid).append('\'');
        sb.append(", expireIn24Hours=").append(expireIn24Hours);
        sb.append(", duration=").append(duration);
        sb.append('}');
        return sb.toString();
    }
}

