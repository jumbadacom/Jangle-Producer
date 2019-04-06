package com.test.jangleproducer.call;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.Constants;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.MainActivity;
import com.test.jangleproducer.MessageSubject;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.model.dispatch.vote.VoteModel;
import com.test.jangleproducer.model.result.vote.Datum;
import com.test.jangleproducer.model.result.vote.Voting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;


import static com.test.jangleproducer.MainActivity.MSG_USER_FOLLOWING_VOTES_COMPLETED;
import static com.test.jangleproducer.MainActivity.MSG_USER_OTHER_VOTES_COMPLETED;
import static com.test.jangleproducer.MainActivity.MSG_VOTE_NEXT_COMPLETION;
import static com.test.jangleproducer.MainActivity.MESSAGE_SUBJECT_KEY;
import static com.test.jangleproducer.MainActivity.USER_TOKEN_KEY;

public class VoteCompletions {


    private TestService mTestService;
    private AppExecutors mAppExecutors;
    private Handler.Callback callback;

    public VoteCompletions(TestService testService, AppExecutors appExecutors, MainActivity activity) {
        this.mTestService = testService;
        this.mAppExecutors = appExecutors;
        this.callback = activity;

    }

    //6-vote all others
    public void voteAllOtherUsers(String userToken) {
        mAppExecutors.networkIO().execute(() -> {
            try {
                Map<String, String> authMap = new HashMap<>();
                authMap.put(Constants.AUTHORIZATION, Constants.BEARER + userToken);
                Call<Voting> call2 = mTestService.getVoteAllOthers(authMap);
                Response<Voting> votingResponse = call2.execute();
                if (votingResponse.isSuccessful() && votingResponse.body() != null && votingResponse.body().getData() != null && votingResponse.body().getData().size() > 0) {
                    Datum datum = votingResponse.body().getData().get(0);
                    String uuid = datum.getCompletions().get(0).getUuid();
                    Call<Void> call3 = mTestService.voteCompletion(new VoteModel(true, uuid, 1 + new Random().nextInt(5)), authMap);
                    Response<Void> voteSendResponse = call3.execute();
                    if (voteSendResponse.code()==200) {
                        Message msg = Message.obtain();
                        msg.what = MSG_VOTE_NEXT_COMPLETION;
                        Bundle bundle = new Bundle();
                        bundle.putString(USER_TOKEN_KEY, userToken);
                        bundle.putSerializable(MESSAGE_SUBJECT_KEY,MessageSubject.VOTE_COMPLETION_OTHERS);
                        msg.setData(bundle);
                        callback.handleMessage(msg);
                    }

                } else {
                    Message msg = Message.obtain();
                    msg.what = MSG_USER_OTHER_VOTES_COMPLETED;
                    callback.handleMessage(msg);
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }

        });
    }

    //6-vote all followings
    public void voteAllFollowingUsers(String userToken) {
        mAppExecutors.networkIO().execute(() -> {
            try {
                Map<String, String> authMap = new HashMap<>();
                authMap.put(Constants.AUTHORIZATION, Constants.BEARER + userToken);
                Call<Voting> call2 = mTestService.getVoteAllFollowing(authMap);
                Response<Voting> votingResponse = call2.execute();
                if (votingResponse.isSuccessful() && votingResponse.body() != null && votingResponse.body().getData() != null && votingResponse.body().getData().size() > 0) {
                    Datum datum = votingResponse.body().getData().get(0);
                    String uuid = datum.getCompletions().get(0).getUuid();
                    Call<Void> call3 = mTestService.voteCompletion(new VoteModel(true, uuid, 1 + new Random().nextInt(5)), authMap);
                    Response<Void> voteSendResponse = call3.execute();
                    if (voteSendResponse.code()==200) {
                        Message msg = Message.obtain();
                        msg.what = MSG_VOTE_NEXT_COMPLETION;
                        Bundle bundle = new Bundle();
                        bundle.putString(USER_TOKEN_KEY, userToken);
                        bundle.putSerializable(MESSAGE_SUBJECT_KEY,MessageSubject.VOTE_COMPLETION_FOLLOWINGS);
                        msg.setData(bundle);
                        callback.handleMessage(msg);
                    }

                } else {
                    Message msg = Message.obtain();
                    msg.what = MSG_USER_FOLLOWING_VOTES_COMPLETED;
                    callback.handleMessage(msg);
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }

        });
    }


}