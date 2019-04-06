package com.test.jangleproducer.model.dispatch;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class JangleUploadModel  {


    private RequestBody model;
    private MultipartBody.Part file;
    private MultipartBody.Part preview;
    private MultipartBody.Part thumbnail;
    private String token;

    public JangleUploadModel( RequestBody model, MultipartBody.Part file, MultipartBody.Part preview, MultipartBody.Part thumbnail,
        String token) {

        this.model = model;
        this.file = file;
        this.preview = preview;
        this.thumbnail = thumbnail;
        this.token=token;
    }


    public RequestBody getModel() {
        return model;
    }


    public MultipartBody.Part getFile() {
        return file;
    }


    public MultipartBody.Part getPreview() {
        return preview;
    }

    public MultipartBody.Part getThumbnail() {
        return thumbnail;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


