package com.test.jangleproducer.model.result;

public class UploadResponse {


    private String uuid;
    /*
    private String fileUrl;
    private String imageUrl;
    private String thumbnailUrl;
    private String fileType;
    private String contentType;
    private String title;
    private String description;
    private boolean active;
    private Object notActiveReason;
    private String documentType;
    */

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UploadResponse{");
        sb.append("uuid='").append(uuid).append('\'');

        sb.append('}');
        return sb.toString();
    }
}
