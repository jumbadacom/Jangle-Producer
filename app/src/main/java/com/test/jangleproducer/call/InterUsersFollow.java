package com.test.jangleproducer.call;

import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.Constants;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.Uuid;
import com.test.jangleproducer.model.dispatch.AuthModel;
import com.test.jangleproducer.model.dispatch.UuidModel;
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
    private ArrayList<String> mTokenList = new ArrayList<>();
    private ArrayList<String> mUUIDList = new ArrayList<>();
    private static final int THE_USER_FOLLOW_OTHERS = 1;
    private static final int OTHERS_FOLLOW_THE_USER = 2;
//    private int mTokenUserIndex = 100;

    public InterUsersFollow(AppExecutors appExecutors, TestService testService) {
        this.mAppExecutors = appExecutors;
        this.mTestService = testService;
    }

    /*
    public void sendFollowRequestFromUser(final int fromUser, final int reqCount, final String toTargetUserUUID) {
        mAppExecutors.networkIO().execute(() -> {
            Call<AuthResponse> call = mTestService.authenticate(new AuthModel("testuser" + fromUser, "testuser" + fromUser));
            try {
                Response<AuthResponse> response = call.execute();
                if (response.body() != null && response.body().getToken() != null && fromUser < (reqCount + fromUser)) {
                    Map<String, String> authMap = new HashMap<>();
                    authMap.put(Constants.AUTHORIZATION, Constants.BEARER + response.body().getToken());
                    Call<Void> call2 = mTestService.sendFollowRequest(new UuidModel(toTargetUserUUID), authMap);
                    Response<Void> response2 = call2.execute();
                    DebugLog.write(response2.code());
                    if (response2.code() == 201) {
                        DebugLog.write();
                        sendFollowRequestFromUser(fromUser + 1, reqCount, toTargetUserUUID);
                    }
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }
        });
    }


    public void runInterUserFollow() {
        DebugLog.write("runInterUserFollow : " + Thread.currentThread().getName());
        getTokenList(1);
    }*/


    public  void followTheUser(String username, int lowerRange, int maxRange) {
        getToken(username, lowerRange, maxRange, OTHERS_FOLLOW_THE_USER);
    }

    public void userFollowOthers(String username, int lowerRange, int maxRange) {
        DebugLog.write("userFollowOthers : " + Thread.currentThread().getName());
        getToken(username, lowerRange, maxRange, THE_USER_FOLLOW_OTHERS);
    }

    private void getToken(String username, int lowerRange, int maxRange, int opt) {
        mAppExecutors.networkIO().execute(() -> {
            DebugLog.write("getToken : " + Thread.currentThread().getName());
            Call<AuthResponse> call = mTestService.authenticate(new AuthModel(username, username));
            try {
                Response<AuthResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    String token = response.body().getToken();
                    getTokenList(token, lowerRange, maxRange, opt);
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }
        });
    }

    private void getTokenList(String mainUserToken, final int lowerRange, final int maxRange, final int opt) {
        DebugLog.write("getTokenList : " + Thread.currentThread().getName());

        if (lowerRange <= maxRange) {
            mAppExecutors.networkIO().execute(() -> {
                Call<AuthResponse> call = mTestService.authenticate(new AuthModel("testuser" + lowerRange, "testuser" + lowerRange));
                try {
                    DebugLog.write("getTokenList networkIO() : " + Thread.currentThread().getName());
                    Response<AuthResponse> response = call.execute();
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        mTokenList.add(response.body().getToken());
                        getTokenList(mainUserToken, lowerRange + 1, maxRange, opt);
                    }
                } catch (IOException e) {
                    DebugLog.write(e.getMessage());
                }
            });
        } else {
            if (opt == THE_USER_FOLLOW_OTHERS) {
                getUUIDList(mainUserToken, mTokenList);
            } else if (opt == OTHERS_FOLLOW_THE_USER) {
                getUUID(mainUserToken, mTokenList);
            }

        }
    }

    private void getUUIDList(String mainUserToken, ArrayList<String> userTokens) {
        DebugLog.write("getUUIDList : " + Thread.currentThread().getName());
        while (userTokens.size() > 0) {
            String otherUserToken = userTokens.get(0);
            DebugLog.write("getUUIDList networkIO : " + Thread.currentThread().getName());
            Map<String, String> authMap = new HashMap<>();
            authMap.put(Constants.AUTHORIZATION, Constants.BEARER + otherUserToken);
            Call<UUID> call = mTestService.getUUIDs(authMap);
            try {
                Response<UUID> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    mUUIDList.add(response.body().getUuid());
                    userTokens.remove(0);
                    userTokens.trimToSize();
                    getUUIDList(mainUserToken, userTokens);
                }
            } catch (IOException io) {
                DebugLog.write(io.getMessage());
            }
        }
        userFollowOthers(mainUserToken, mUUIDList);
    }

    private void getUUID(String userToken, ArrayList<String> otherUsersTokens) {
        DebugLog.write("getUUID networkIO : " + Thread.currentThread().getName());
        Map<String, String> authMap = new HashMap<>();
        authMap.put(Constants.AUTHORIZATION, Constants.BEARER + userToken);
        Call<UUID> call = mTestService.getUUIDs(authMap);
        try {
            Response<UUID> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                String uuid = response.body().getUuid();
                otherUsersFollowTheUser(uuid, otherUsersTokens);
            }
        } catch (Exception e) {
        }
    }

    private void userFollowOthers(String mainUserToken, ArrayList<String> uuidList) {
        while (uuidList.size() > 0) {
            String uuid = uuidList.get(0);
            DebugLog.write("getUUIDList networkIO : " + Thread.currentThread().getName());
            Map<String, String> authMap = new HashMap<>();
            authMap.put(Constants.AUTHORIZATION, Constants.BEARER + mainUserToken);
            Call<Void> call = mTestService.sendFollowRequest(new UuidModel(uuid), authMap);
            try {
                Response<Void> response = call.execute();
                DebugLog.write(response.code());
                if (response.code() == 201) {
                    uuidList.remove(0);
                    uuidList.trimToSize();
                    userFollowOthers(mainUserToken, uuidList);
                }
            } catch (Exception e) {
                DebugLog.write();
            }
        }
        mTokenList.clear();
        mUUIDList.clear();
    }

    private void otherUsersFollowTheUser(String userUuid, ArrayList<String> otherUsersTokens) {
        while (otherUsersTokens.size() > 0) {
            String token = otherUsersTokens.get(0);
            DebugLog.write("getUUIDList networkIO : " + Thread.currentThread().getName());
            Map<String, String> authMap = new HashMap<>();
            authMap.put(Constants.AUTHORIZATION, Constants.BEARER + token);
            Call<Void> call = mTestService.sendFollowRequest(new UuidModel(userUuid), authMap);
            try {
                Response<Void> response = call.execute();
                DebugLog.write(response.code());
                if (response.code() == 201) {
                    otherUsersTokens.remove(0);
                    otherUsersTokens.trimToSize();
                    otherUsersFollowTheUser(userUuid, otherUsersTokens);
                }
            } catch (Exception e) {

            }
        }
        mTokenList.clear();
        mUUIDList.clear();

    }



    /*
    private void getTokenList(final int startUserId) {
        DebugLog.write("getTokenList : " + Thread.currentThread().getName());
        if (startUserId < 99) {
            mAppExecutors.networkIO().execute(() -> {
                Call<AuthResponse> call = mTestService.authenticate(new AuthModel("testuser" + startUserId, "testuser" + startUserId));
                try {
                    DebugLog.write("getTokenList networkIO() : " + Thread.currentThread().getName());
                    Response<AuthResponse> response = call.execute();
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        mTokenList.add(response.body().getToken());
                        getTokenList(startUserId + 1);
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
        DebugLog.write("getUUIDList : " + Thread.currentThread().getName());
        if (start < 99) {
            mAppExecutors.networkIO().execute(() -> {
                DebugLog.write("getUUIDList networkIO : " + Thread.currentThread().getName());
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
        DebugLog.write("userFollowOthers : " + Thread.currentThread().getName());
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
                Call<Void> call = mTestService.sendFollowRequest(new UuidModel(mUUIDList.get(uuidUserIndex)), authMap);
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

    }*/


}
