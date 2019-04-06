package com.test.jangleproducer.call;

import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.Constants;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.Uuid;
import com.test.jangleproducer.model.dispatch.AuthModel;
import com.test.jangleproducer.model.dispatch.UserModel;
import com.test.jangleproducer.model.result.AuthResponse;
import com.test.jangleproducer.model.result.UUID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class InterUsersFollow {


    private TestService mTestService;
    private AppExecutors mAppExecutors;
    private List<String> mTokenList = new ArrayList<>();
    private List<String> mUUIDList = new ArrayList<>();
    private int mTokenUserIndex = 1;

    public InterUsersFollow(AppExecutors appExecutors, TestService testService) {
        this.mAppExecutors = appExecutors;
        this.mTestService = testService;
    }

    public void sendFollowRequestToUser(final int userSuffix) {
        mAppExecutors.networkIO().execute(() -> {
            Call<AuthResponse> call = mTestService.authenticate(new AuthModel("testuser" + userSuffix, "testuser" + userSuffix));
            try {
                Response<AuthResponse> response = call.execute();
                if (response.body() != null && response.body().getToken() != null && userSuffix < 301) {
                    Map<String, String> authMap = new HashMap<>();
                    authMap.put(Constants.AUTHORIZATION, Constants.BEARER + response.body().getToken());
                    Call<Void> call2 = mTestService.sendFollowRequest(new UserModel(Uuid.TESTUSER32), authMap);
                    Response<Void> response2 = call2.execute();
                    DebugLog.write(response2.code());
                    if (response2.code() == 201) {
                        DebugLog.write();
                        sendFollowRequestToUser(userSuffix + 1);
                    }
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }
        });
    }


    public void runInterUserFollow() {

        getTokenList(301);
    }

    private void getTokenList(final int userId) {
        if (userId < 338) {
            mAppExecutors.networkIO().execute(() -> {
                Call<AuthResponse> call = mTestService.authenticate(new AuthModel("testuser" + userId, "testuser" + userId));
                try {
                    Response<AuthResponse> response = call.execute();
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        mTokenList.add(response.body().getToken());
                        getTokenList(userId + 1);
                    }
                } catch (IOException e) {
                    DebugLog.write(e.getMessage());
                }
            });
        } else {
            getUUIDList(1);
        }
    }

    private void getUUIDList(final int start) {
        if (start < 38) {
            mAppExecutors.networkIO().execute(() -> {
                Map<String, String> authMap = new HashMap<>();
                authMap.put(Constants.AUTHORIZATION, Constants.BEARER + mTokenList.get(start - 1));
                Call<UUID> call = mTestService.getUUIDs(authMap);
                try {
                    Response<UUID> response = call.execute();
                    if (response.isSuccessful() && response.body() != null) {
                        mUUIDList.add(response.body().getUuid());
                        getUUIDList(start + 1);
                    }

                } catch (IOException e) {
                    DebugLog.write(e.getMessage());
                }
            });
        } else {
            userFollowOthers(0);
        }
    }

    private void userFollowOthers(int uuidUserIndex) {

        if (mTokenUserIndex < mTokenList.size() && uuidUserIndex < mUUIDList.size()) {
            DebugLog.write();
            if (mTokenUserIndex == uuidUserIndex) {
                DebugLog.write();
                userFollowOthers(++uuidUserIndex);
            } else {
                DebugLog.write();
                String token = mTokenList.get(mTokenUserIndex);
                Map<String, String> authMap = new HashMap<>();
                authMap.put(Constants.AUTHORIZATION, Constants.BEARER + token);
                Call<Void> call = mTestService.sendFollowRequest(new UserModel(mUUIDList.get(uuidUserIndex)), authMap);
                try {
                    Response<Void> response = call.execute();
                    DebugLog.write(response.code());
                    if (response.code() == 201) {
                        userFollowOthers(++uuidUserIndex);
                    }
                } catch (Exception e) {
                    DebugLog.write();
                }
            }
        } else {
            DebugLog.write();
            mTokenUserIndex++;
            userFollowOthers(0);
        }

    }


}
