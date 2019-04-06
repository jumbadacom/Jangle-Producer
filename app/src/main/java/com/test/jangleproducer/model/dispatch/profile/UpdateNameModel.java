package com.test.jangleproducer.model.dispatch.profile;

public class UpdateNameModel {

    private String email;
    private String login;
    private String name;
    private String oldPassword;
    private String langKey;

    public UpdateNameModel(String email, String login, String name, String oldPassword,String langKey) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.oldPassword = oldPassword;
        this.langKey=langKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }
}
