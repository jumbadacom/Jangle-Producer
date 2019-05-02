package com.test.jangleproducer;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkConnection {

    private static final String BASE_URL = "http://192.168.1.20:8080";


    static public TestService get(boolean isLogHttp) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (isLogHttp){
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);}
        else{
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).
                connectTimeout(41L, TimeUnit.SECONDS).readTimeout(41L, TimeUnit.SECONDS).
                build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit.create(TestService.class);
    }

}
