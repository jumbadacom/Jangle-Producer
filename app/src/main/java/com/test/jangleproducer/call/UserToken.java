package com.test.jangleproducer.call;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.activity.MainActivity;
import com.test.jangleproducer.MessageSubject;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.activity.ScreenTwoActivity;
import com.test.jangleproducer.model.dispatch.AuthModel;
import com.test.jangleproducer.model.result.AuthResponse;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Response;

import static com.test.jangleproducer.activity.MainActivity.COMPLETION_COUNT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MSG_DOUBLE_TOKEN_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_TOKEN_READY;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_KEY;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_KEY2;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_LIST_KEY;

public class UserToken {


    private Handler.Callback callback;
    private ArrayList<String> mTokenList = new ArrayList<>();
    private TestService mTestService;
    private AppExecutors mAppExecutors;


    public UserToken(TestService testService, AppExecutors appExecutors, AppCompatActivity activity) {
        this.mAppExecutors = appExecutors;
        this.mTestService = testService;
        if (activity instanceof MainActivity){
            this.callback =(MainActivity) activity;
        }else if(activity instanceof ScreenTwoActivity){
            this.callback = (ScreenTwoActivity)activity;
        }else{
            throw new IllegalArgumentException("Wrong UserToken Activity");
        }

    }

    public void getToken(int userId, final String userBaseName, final MessageSubject messageSubject) {


        mAppExecutors.networkIO().execute(() -> {
            Call<AuthResponse> call = mTestService.authenticate(new AuthModel(userBaseName + userId, userBaseName + userId));
            try {
                Response<AuthResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    DebugLog.write("isSuccessful networkIO: " + Thread.currentThread().getName());
                    mAppExecutors.mainThread().execute(() -> {
                        //todo add looper this line
                        DebugLog.write("? diskIO : " + Thread.currentThread().getName());
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString(USER_TOKEN_KEY, response.body().getToken());
                        bundle.putSerializable(MESSAGE_SUBJECT_KEY, messageSubject);
                        msg.setData(bundle);
                        msg.what = MSG_TOKEN_READY;
                        callback.handleMessage(msg);
                    });

                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }
        });


    }

    public void getSequenceUserToken(int userId, final String userBaseName) {


        mAppExecutors.networkIO().execute(() -> {
            DebugLog.write(userBaseName + userId);
            Call<AuthResponse> call = mTestService.authenticate(new AuthModel(userBaseName + userId, userBaseName + userId));
            try {
                Response<AuthResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    String token1 = response.body().getToken();
                    Call<AuthResponse> call2 = mTestService.authenticate(new AuthModel(userBaseName + (1 + userId), userBaseName + (1 + userId)));
                    Response<AuthResponse> response2 = call2.execute();
                    if (response2.isSuccessful() && response2.body() != null && response2.body().getToken() != null) {
                        String token2 = response2.body().getToken();
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString(USER_TOKEN_KEY, token1);
                        bundle.putString(USER_TOKEN_KEY2, token2);
                        msg.setData(bundle);
                        msg.what = MSG_DOUBLE_TOKEN_READY;
                        callback.handleMessage(msg);
                    }

                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }
        });


    }

    public void getTokenList(int userId, final String userBaseName, final int limit, int completionCount, final MessageSubject messageSubject) {

        if (userId < limit) {
            mAppExecutors.networkIO().execute(() -> {
                Call<AuthResponse> call = mTestService.authenticate(new AuthModel(userBaseName + userId, userBaseName + userId));
                try {
                    Response<AuthResponse> response = call.execute();
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        mTokenList.add(response.body().getToken());
                        getTokenList(userId + 1, userBaseName, limit, completionCount, messageSubject);
                    }
                } catch (IOException e) {
                    DebugLog.write(e.getMessage());
                }
            });
        } else {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putInt(COMPLETION_COUNT_KEY, completionCount);
            bundle.putStringArrayList(USER_TOKEN_LIST_KEY, mTokenList);
            bundle.putSerializable(MESSAGE_SUBJECT_KEY, messageSubject);
            msg.what = MainActivity.MSG_TOKEN_LIST_READY;
            msg.setData(bundle);
            callback.handleMessage(msg);
        }
    }


    public void getTokenList(final ArrayList<String> userNames,  int completionCount, final MessageSubject messageSubject) {

        if (userNames.size() > 0) {
            mAppExecutors.networkIO().execute(() -> {
                Call<AuthResponse> call = mTestService.authenticate(new AuthModel(userNames.get(0), userNames.get(0)));
                try {
                    Response<AuthResponse> response = call.execute();
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        userNames.remove(0);
                        userNames.trimToSize();
                        mTokenList.add(response.body().getToken());
                           getTokenList( userNames,   completionCount, messageSubject);
                    }
                } catch (IOException e) {
                    DebugLog.write(e.getMessage());
                }
            });
        } else {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putInt(COMPLETION_COUNT_KEY, completionCount);
            bundle.putStringArrayList(USER_TOKEN_LIST_KEY, mTokenList);
            bundle.putSerializable(MESSAGE_SUBJECT_KEY, messageSubject);
            msg.what = MainActivity.MSG_TOKEN_LIST_READY;
            msg.setData(bundle);
            callback.handleMessage(msg);
        }
    }
    public void getTokenList(final ArrayList<String> userNames, final MessageSubject messageSubject, int what) {

        if (userNames.size() > 0) {
            mAppExecutors.networkIO().execute(() -> {
                Call<AuthResponse> call = mTestService.authenticate(new AuthModel(userNames.get(0), userNames.get(0)));
                try {
                    DebugLog.write("isSuccessful networkIO: " + Thread.currentThread().getName());
                    Response<AuthResponse> response = call.execute();
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        userNames.remove(0);
                        userNames.trimToSize();
                        mTokenList.add(response.body().getToken());
                        getTokenList( userNames,    messageSubject,what);
                    }
                } catch (IOException e) {
                    DebugLog.write(e.getMessage());
                }
            });
        } else {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(USER_TOKEN_LIST_KEY, mTokenList);
            bundle.putSerializable(MESSAGE_SUBJECT_KEY, messageSubject);
            msg.what = what;
            msg.setData(bundle);
            callback.handleMessage(msg);
        }
    }

}
