package com.test.jangleproducer.call;

import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.Constants;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.model.dispatch.AuthModel;
import com.test.jangleproducer.model.dispatch.profile.HideAccountModel;
import com.test.jangleproducer.model.dispatch.profile.UpdateNameModel;
import com.test.jangleproducer.model.result.AuthResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class UpdateUserProfile {



    private TestService mTestService;
    private AppExecutors mAppExecutors;

    public UpdateUserProfile(TestService testService, AppExecutors appExecutors) {
        mTestService=testService;
        mAppExecutors=appExecutors;

    }


    //2- hide account
    public void hideSelectedAccounts(final int startingUserSuffix) {
        mAppExecutors.networkIO().execute(() -> {
            Call<AuthResponse> call = mTestService.authenticate(new AuthModel("testuser" + startingUserSuffix, "testuser" + startingUserSuffix));
            try {
                Response<AuthResponse> response = call.execute();
                if (response.body() != null && response.body().getToken() != null && startingUserSuffix < 400) {
                    Map<String, String> authMap = new HashMap<>();
                    authMap.put(Constants.AUTHORIZATION, Constants.BEARER + response.body().getToken());
                    Call<Void> call2 = mTestService.hideAccount(new HideAccountModel(true), authMap);
                    Response<Void> response2 = call2.execute();
                    DebugLog.write(response2.code());
                    if (response2.code() == 200) {
                        DebugLog.write();
                        hideSelectedAccounts(startingUserSuffix + 1);
                    }
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }
        });
    }

    public void updateProfileName(int userSuf, String userBaseInfo) {
        if (userSuf < 1200) {
            mAppExecutors.networkIO().execute(() -> {
                Call<AuthResponse> call = mTestService.authenticate(new AuthModel(userBaseInfo + userSuf, userBaseInfo + userSuf));
                try {
                    Response<AuthResponse> response = call.execute();
                    if (response.isSuccessful()) {
                        Map<String, String> authMap = new HashMap<>();
                        authMap.put(Constants.AUTHORIZATION, Constants.BEARER + response.body().getToken());
                        String login = userBaseInfo + userSuf;
                        String email = login + "@jangle.co";
                        String name = "name " + login;
                        String oldPass = login;
                        String lang="tr";
                        UpdateNameModel model = new UpdateNameModel(email, login, name, oldPass,lang);
                        Call<Void> call2 = mTestService.updateProfileName(model, authMap);
                        Response<Void> response2 = call2.execute();
                        if (response2.isSuccessful()) {
                            updateProfileName(userSuf + 1, userBaseInfo);
                        }
                    }

                } catch (IOException e) {
                    DebugLog.write();
                }
            });
        }
    }
}
