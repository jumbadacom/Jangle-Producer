package com.test.jangleproducer.model.dispatch;

public class RegisterModel {

    private String login;
    private String password;
    private String email;
    private String langKey;

    public RegisterModel(String login) {
        this.login = login;
        this.password=login;
        this.email=login+"@jangle.co";
        this.langKey="tr";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegisterModel{");
        sb.append("login='").append(login).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", langKey='").append(langKey).append('\'');
        sb.append('}');
        return sb.toString();
    }



}
