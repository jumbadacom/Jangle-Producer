package com.test.jangleproducer.call;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.Constants;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.activity.MainActivity;
import com.test.jangleproducer.MessageSubject;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.activity.ScreenTwoActivity;
import com.test.jangleproducer.model.CommonDto;
import com.test.jangleproducer.model.dispatch.UuidModel;
import com.test.jangleproducer.model.result.del.Datum;
import com.test.jangleproducer.model.result.del.JangleInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Response;

import static com.test.jangleproducer.activity.MainActivity.COMPLETION_COUNT_KEY;
import static com.test.jangleproducer.activity.MainActivity.JANGLE_KEY;
import static com.test.jangleproducer.activity.MainActivity.KEY_COMMON_DTO;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MSG_DEL_JANGLE_COMPLETED;
import static com.test.jangleproducer.activity.MainActivity.MSG_DEL_NEXT_JANGLE;
import static com.test.jangleproducer.activity.MainActivity.MSG_LAST_JANGLE;
import static com.test.jangleproducer.activity.MainActivity.MSG_LIKED_JANGLE;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_KEY;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_LIST_KEY;

public class HandleJangle {


    private TestService mTestService;
    private AppExecutors mAppExecutors;
    private Handler.Callback callback;

    public HandleJangle(TestService testService, AppExecutors appExecutors, AppCompatActivity activity) {
        this.mTestService = testService;
        this.mAppExecutors = appExecutors;
        if (activity instanceof MainActivity) {
            this.callback = (MainActivity) activity;
        } else if (activity instanceof ScreenTwoActivity) {
            this.callback = (ScreenTwoActivity) activity;
        } else {
            throw new IllegalArgumentException("Wrong HandleJangle Activity");
        }

    }

    public void deleteJangle(String userToken) {
        mAppExecutors.networkIO().execute(() -> {
            try {
                Map<String, String> authMap = new HashMap<>();
                authMap.put(Constants.AUTHORIZATION, Constants.BEARER + userToken);
                Call<JangleInfo> call2 = mTestService.getUserLastJangle(authMap);
                Response<JangleInfo> jangleInfoResponse = call2.execute();
                if (jangleInfoResponse.isSuccessful() && jangleInfoResponse.body() != null && jangleInfoResponse.body().getData() != null && jangleInfoResponse.body().getData().size() > 0) {
                    Datum datum = jangleInfoResponse.body().getData().get(0);
                    String uuid = datum.getUuid();
                    Call<Void> call3 = mTestService.delJangle(authMap, uuid);
                    Response<Void> response3 = call3.execute();
                    if (response3.code() == 200) {
                        Message msg = Message.obtain();
                        msg.what = MSG_DEL_NEXT_JANGLE;
                        Bundle bundle = new Bundle();
                        bundle.putString(USER_TOKEN_KEY, userToken);
                        callback.handleMessage(msg);
                    }

                } else {
                    Message msg = Message.obtain();
                    msg.what = MSG_DEL_JANGLE_COMPLETED;
                    callback.handleMessage(msg);
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }

        });
    }

    public void getLastJangle(ArrayList<String> userTokenList, int completionCount, MessageSubject subject) {

        mAppExecutors.networkIO().execute(() -> {
            try {
                Map<String, String> authMap = new HashMap<>();
                authMap.put(Constants.AUTHORIZATION, Constants.BEARER + userTokenList.remove(0));
                Call<JangleInfo> call = mTestService.getUserLastJangle(authMap);
                Response<JangleInfo> jangleInfoResponse = call.execute();
                if (jangleInfoResponse.isSuccessful() && jangleInfoResponse.body() != null && jangleInfoResponse.body().getData() != null && jangleInfoResponse.body().getData().size() > 0) {
                    userTokenList.trimToSize();
                    Datum datum = jangleInfoResponse.body().getData().get(0);
                    String uuid = datum.getUuid();
                    Message msg = Message.obtain();
                    msg.what = MSG_LAST_JANGLE;
                    Bundle bundle = new Bundle();
                    bundle.putInt(COMPLETION_COUNT_KEY, completionCount);
                    bundle.putString(JANGLE_KEY, uuid);
                    bundle.putStringArrayList(USER_TOKEN_LIST_KEY, userTokenList);
                    bundle.putSerializable(MESSAGE_SUBJECT_KEY, subject);
                    msg.setData(bundle);
                    callback.handleMessage(msg);
                }
            } catch (Exception e) {
                DebugLog.write(e.getMessage());
            }
        });
    }


    public void likeJangle(CommonDto commonDto, int likeCount, MessageSubject subject) {

        if (likeCount > 0) {
            int userIndex = likeCount - 1;
            String likeUserToken = commonDto.getUsersToken().get(userIndex);
            Map<String, String> authMap = new HashMap<>();
            authMap.put(Constants.AUTHORIZATION, Constants.BEARER + likeUserToken);
            mAppExecutors.networkIO().execute(() -> {
                DebugLog.write("LIKED USER TOKEN= "+authMap.get(Constants.AUTHORIZATION));
                DebugLog.write("jangle UUID= "+commonDto.getJangleUuid());
                Call<Void> call = mTestService.likeJangle(new UuidModel(commonDto.getJangleUuid()), authMap);
                try {
                    Response<Void> response = call.execute();
                    if (response.code() == 200) {
                        likeJangle(commonDto, likeCount - 1, subject);
                    }
                    else{
                        DebugLog.write(response.code()+" "+response.errorBody());
                    }

                } catch (IOException r) {
                }
            });
        } else {
            DebugLog.write();
            Message msg = Message.obtain();
            msg.what = MSG_LIKED_JANGLE;
            Bundle bundle = new Bundle();
            bundle.putSerializable(MESSAGE_SUBJECT_KEY, subject);
            bundle.putSerializable(KEY_COMMON_DTO, commonDto);
            msg.setData(bundle);
            callback.handleMessage(msg);
        }

    }

}
