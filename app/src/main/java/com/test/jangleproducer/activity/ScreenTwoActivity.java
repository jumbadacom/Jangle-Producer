package com.test.jangleproducer.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.AssetBitmapGenerator;
import com.test.jangleproducer.BitmapImageType;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.FileConverter;
import com.test.jangleproducer.MessageSubject;
import com.test.jangleproducer.NetworkConnection;
import com.test.jangleproducer.R;
import com.test.jangleproducer.RandomColorGenerator;
import com.test.jangleproducer.RandomWordGenerator;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.call.HandleJangle;
import com.test.jangleproducer.call.Upload;
import com.test.jangleproducer.call.UserToken;
import com.test.jangleproducer.call.VoteCompletions;
import com.test.jangleproducer.model.CommonDto;
import com.test.jangleproducer.util.UserUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import static com.test.jangleproducer.activity.MainActivity.JANGLE_KEY;
import static com.test.jangleproducer.activity.MainActivity.JANGLE_OWNER_KEY;
import static com.test.jangleproducer.activity.MainActivity.KEY_COMMON_DTO;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUB_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MSG_JANGLE_AND_COMPLETIONS_FILES_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_LIKED_JANGLE;
import static com.test.jangleproducer.activity.MainActivity.MSG_USER_FOLLOWING_VOTES_COMPLETED;
import static com.test.jangleproducer.activity.MainActivity.MSG_USER_OTHER_VOTES_COMPLETED;
import static com.test.jangleproducer.activity.MainActivity.MSG_VOTE_NEXT_COMPLETION;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_KEY;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_LIST_KEY;

//1- get n number user token
//2-


public class ScreenTwoActivity extends AppCompatActivity implements Handler.Callback {

    private static int mainCounter = 0;

    //generate bitmaps and files list before
    private static final int PROCESS_1 = 211;
    private static final int PROCESS_1_CONTINUE = 212;


    static class DecisionHolder {
        final static int SELECTED_PROCESS = PROCESS_1;

        final static int BASE_USER = 1;
        final static int MIN_USER = BASE_USER + 1;
        final static int MAX_USER = 75;

        final static int JANGLE_PER_USER = 1;
        final static int COMPLETION_PER_JANGLE = 7;
        final static int LIKE_COUNT = 5;

        final static @BitmapImageType
        int bitmapType = BitmapImageType.DEF_JANGLE_IMAGE;
    }

    private TestService mService;
    private AppExecutors mAppExecutors;
    private FileConverter mFileConverter;
    private AssetBitmapGenerator mAssetBitmapGenerator;
    private RandomColorGenerator mRandomColorGenerator;
    private RandomWordGenerator mRandomWordGenerator;
    private Gson mGson;
    private Random mRandom;
    private UserToken mUserToken;
    private VoteCompletions mVoteCompletions;
    private HandleJangle mHandleJangle;
    private Upload mUpload;
    private ArrayList<String> mainUsersTokenList;
    private AppCompatTextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_two);
        mTextView = (AppCompatTextView) findViewById(R.id.txtAddJangleCompletionsVotesLikes);
        mService = NetworkConnection.get(false);
        mAppExecutors = new AppExecutors();
        mAssetBitmapGenerator = new AssetBitmapGenerator(this, mAppExecutors);
        mFileConverter = new FileConverter(mAppExecutors, this);
        mRandomColorGenerator = new RandomColorGenerator();
        mRandomWordGenerator = new RandomWordGenerator();
        mGson = new GsonBuilder().setLenient().create();
        mRandom = new Random();
        mUserToken = new UserToken(mService, mAppExecutors, this);
        mVoteCompletions = new VoteCompletions(mService, mAppExecutors, this);
        mHandleJangle = new HandleJangle(mService, mAppExecutors, this);
        mUpload = new Upload(mService, mAppExecutors, mGson, mRandomWordGenerator, mRandom, this);
    }

    public void addJangleAndCompletionsAndVoteAndLikes(View view) {
        DebugLog.write("addJangleAndCompletionsAndVoteAndLikes: " + Thread.currentThread().getName());
        addJangleComplVoteLikes();


    }


    private void addJangleComplVoteLikes() {
        if (mainUsersTokenList == null) {
            DebugLog.write();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> passwords = new ArrayList<>();
            names.add(UserUtil.getUsername("testuser1"));
            passwords.add(UserUtil.getPassword("testuser1"));
            names.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER, DecisionHolder.MAX_USER));
            passwords.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER, DecisionHolder.MAX_USER));
            names.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER + 1000, DecisionHolder.MAX_USER + 1000));
            passwords.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER + 1000, DecisionHolder.MAX_USER + 1000));

            //1 get token list
            mUserToken.getTokenList(names, passwords, MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES, DecisionHolder.SELECTED_PROCESS);
        } else {
            DebugLog.write();
            Message msg = Message.obtain();
            msg.what = DecisionHolder.SELECTED_PROCESS + 1;
            this.handleMessage(msg);
        }

    }

    @Override
    public boolean handleMessage(Message msg) {
        DebugLog.write("msg what=" + msg.what);
        switch (msg.what) {

            case PROCESS_1: {
                DebugLog.write("PROCESS_1: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES) {
                    mainUsersTokenList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                    int maxUsersAndFilesCount = DecisionHolder.JANGLE_PER_USER + DecisionHolder.COMPLETION_PER_JANGLE;
                    DebugLog.write(mainUsersTokenList.size());
                    ArrayList<String> randomUsers = UserUtil.getRandomTokensFromList(maxUsersAndFilesCount, mainUsersTokenList);
                    mAppExecutors.diskIO().execute(() -> {
                        DebugLog.write("PROCESS_1 diskIO: " + Thread.currentThread().getName());
                        ArrayList<Bitmap[]> bitmapList = mAssetBitmapGenerator.getScaledBitmapList(BitmapImageType.DEF_JANGLE_IMAGE,
                                maxUsersAndFilesCount);
                        ArrayList<File[]> fileList = mFileConverter.convertBitmapListToFileList(bitmapList);
                        DebugLog.write(fileList.size());
                        mUpload.uploadJangleAndCompletions(fileList, randomUsers,
                                MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                    });
                }
                break;
            }
            case PROCESS_1_CONTINUE: {
                DebugLog.write();
                int maxUsersAndFilesCount = DecisionHolder.JANGLE_PER_USER + DecisionHolder.COMPLETION_PER_JANGLE;
                DebugLog.write(mainUsersTokenList.size());
                ArrayList<String> randomUsers = UserUtil.getRandomTokensFromList(maxUsersAndFilesCount, mainUsersTokenList);
                mAppExecutors.diskIO().execute(() -> {
                    DebugLog.write("PROCESS_1_CONTINUE diskIO: " + Thread.currentThread().getName());
                    ArrayList<Bitmap[]> bitmapList = mAssetBitmapGenerator.getScaledBitmapList(BitmapImageType.DEF_JANGLE_IMAGE,
                            maxUsersAndFilesCount);
                    ArrayList<File[]> fileList = mFileConverter.convertBitmapListToFileList(bitmapList);
                    DebugLog.write(fileList.size());
                    mUpload.uploadJangleAndCompletions(fileList, randomUsers,
                            MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                });
                break;
            }

            case MSG_JANGLE_AND_COMPLETIONS_FILES_READY: {
                DebugLog.write("COMPLETIONS_FILES_READY: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES) {
                    String jangleUuid = bundle.getString(JANGLE_KEY);
                    String jangleOwnerToken = bundle.getString(JANGLE_OWNER_KEY);
                    ArrayList<String> otherTokens = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                    DebugLog.write("JANGLE UUID= " + jangleUuid);
                    DebugLog.write("JANGLE OWNER TOKEN= " + jangleOwnerToken);
                    DebugLog.write("OTHER USER COUNT= " + otherTokens.size());
                    int likeCount = (otherTokens.size() <= DecisionHolder.LIKE_COUNT) ? otherTokens.size() : DecisionHolder.LIKE_COUNT;
                    CommonDto dto = new CommonDto();
                    dto.setJangleUuid(jangleUuid);
                    dto.setOwnerToken(jangleOwnerToken);
                    dto.setUsersToken(otherTokens);
                    mHandleJangle.likeJangle(dto, likeCount, MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                }
                break;
            }
            case MSG_LIKED_JANGLE: {
                DebugLog.write("MSG_LIKED_JANGLE: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES) {
                    CommonDto dto = (CommonDto) bundle.getSerializable(KEY_COMMON_DTO);

                    boolean isCont = false;
                    String fnsh = "";
                    ++mainCounter;
                    if (mainCounter < 120) {
                        isCont = true;
                        mVoteCompletions.voteAllFollowingUsers(dto.getOwnerToken(), MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                    } else if (mainCounter < 175) {
                        addJangleComplVoteLikes();
                    } else {
                        DebugLog.write("!! STOP !!");
                        fnsh = "Completed";
                    }
                    String info = "jid= " + dto.getJangleUuid() + " counter= " + mainCounter + " " + String.valueOf(isCont) + " " + fnsh;
                    mAppExecutors.mainThread().execute(() -> {
                        mTextView.setText(info);
                    });
                }
                break;
            }
            case MSG_VOTE_NEXT_COMPLETION: {
                DebugLog.write("MSG_VOTE_NEXT_COMPLETION: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES) {
                    String token = bundle.getString(USER_TOKEN_KEY);
                    MessageSubject subSubject = (MessageSubject) bundle.getSerializable(MESSAGE_SUB_SUBJECT_KEY);
                    DebugLog.write(subSubject);
                    if (subSubject == MessageSubject.VOTE_COMPLETION_FOLLOWINGS) {
                        mVoteCompletions.voteAllFollowingUsers(token, MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                    } else if (subSubject == MessageSubject.VOTE_COMPLETION_OTHERS) {
                        mVoteCompletions.voteAllOtherUsers(token, MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                    }
                }
                break;
            }
            case MSG_USER_FOLLOWING_VOTES_COMPLETED: {
                DebugLog.write("MSG_USER_FOLLOWING_VOTES_COMPLETED: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES) {
                    String token = bundle.getString(USER_TOKEN_KEY);
                    mVoteCompletions.voteAllOtherUsers(token, MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                }
                break;
            }
            case MSG_USER_OTHER_VOTES_COMPLETED: {
                DebugLog.write("MSG_USER_OTHER_VOTES_COMPLETED: " + Thread.currentThread().getName());
                addJangleComplVoteLikes();
                break;
            }


        }
        return false;
    }


}
