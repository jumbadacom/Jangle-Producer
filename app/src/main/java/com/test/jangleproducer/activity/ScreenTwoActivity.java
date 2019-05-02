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
import com.test.jangleproducer.DocType;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

import static com.test.jangleproducer.activity.MainActivity.JANGLE_KEY;
import static com.test.jangleproducer.activity.MainActivity.JANGLE_OWNER_KEY;
import static com.test.jangleproducer.activity.MainActivity.KEY_BIG_BITMAP;
import static com.test.jangleproducer.activity.MainActivity.KEY_BIG_FILE;
import static com.test.jangleproducer.activity.MainActivity.KEY_COMMON_DTO;
import static com.test.jangleproducer.activity.MainActivity.KEY_DOC_TYPE;
import static com.test.jangleproducer.activity.MainActivity.KEY_HAS_COMPLETIONS;
import static com.test.jangleproducer.activity.MainActivity.KEY_SMALL_BITMAP;
import static com.test.jangleproducer.activity.MainActivity.KEY_SMALL_FILE;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUB_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MSG_BITMAP_FILE_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_BITMAP_IMAGE_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_JANGLE_AND_COMPLETIONS_FILES_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_LIKED_JANGLE;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_COMPLETION_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_JANGLE_READY;
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

    //generate  bitmaps and files during network call
    private static final int PROCESS_2 = 221;
    private static final int PROCESS_2_CONTINUE = 222;

    static class DecisionHolder {
        final static int SELECTED_PROCESS = PROCESS_2;

        final static int BASE_USER = 1;
        final static int MIN_USER = BASE_USER + 1;
        final static int MAX_USER = 5;

        final static int JANGLE_PER_USER = 1;
        final static int COMPLETION_PER_JANGLE = 5;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_two);
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
            mUserToken.getTokenList(names, passwords,MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES, DecisionHolder.SELECTED_PROCESS);
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

            case PROCESS_2: {
                DebugLog.write("PROCESS_2: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES) {
                    mainUsersTokenList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                    int janglerAndCompletionerUserCount = DecisionHolder.JANGLE_PER_USER + DecisionHolder.COMPLETION_PER_JANGLE;
                    DebugLog.write(mainUsersTokenList.size());
                    ArrayList<String> randomUsers = UserUtil.getRandomTokensFromList(janglerAndCompletionerUserCount, mainUsersTokenList);
                    mAssetBitmapGenerator.getScaledBitmapMsg(DecisionHolder.bitmapType, DocType.JANGLE,randomUsers);
                }
                break;
            }
            case PROCESS_2_CONTINUE: {
                DebugLog.write("PROCESS_2_CONTINUE: " + Thread.currentThread().getName());
                int janglerAndCompletionerUserCount = DecisionHolder.JANGLE_PER_USER + DecisionHolder.COMPLETION_PER_JANGLE;
                ArrayList<String> randomUsers = UserUtil.getRandomTokensFromList(janglerAndCompletionerUserCount, mainUsersTokenList);
                mAssetBitmapGenerator.getScaledBitmapMsg(DecisionHolder.bitmapType, DocType.JANGLE,randomUsers);
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

                    ++mainCounter;
                    if (mainCounter < 50) {
                        mVoteCompletions.voteAllFollowingUsers(dto.getOwnerToken(), MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
                    } else if (mainCounter < 75) {
                        addJangleComplVoteLikes();
                    } else {
                        DebugLog.write("!! STOP !!");
                    }


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
            case MSG_BITMAP_IMAGE_READY: {
                DebugLog.write("MSG_BITMAP_IMAGE_READY: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                Bitmap bigBitmap = bundle.getParcelable(KEY_BIG_BITMAP);
                Bitmap smallBitmap = bundle.getParcelable(KEY_SMALL_BITMAP);
                int docType = bundle.getInt(KEY_DOC_TYPE);
                ArrayList<String> randomUserList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                mFileConverter.convertBitmapToFilesMsg(bigBitmap, smallBitmap, docType,randomUserList);
                break;
            }
            case MSG_BITMAP_FILE_READY: {
                DebugLog.write("MSG_USER_OTHER_VOTES_COMPLETED: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                File bigFile = (File) bundle.getSerializable(KEY_BIG_FILE);
                File smallFile = (File) bundle.getSerializable(KEY_SMALL_FILE);
                ArrayList<String> randomUserList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                int docType = bundle.getInt(KEY_DOC_TYPE);
                switch (docType) {
                    case DocType.JANGLE: {
                        mUpload.uploadJangleWithFile(true, DecisionHolder.COMPLETION_PER_JANGLE,new File[]{bigFile, smallFile}, randomUserList);
                        break;
                    }
                    case DocType.COMPLETION: {

                        break;
                    }
                }

                break;
            }
            case MSG_UPLOAD_JANGLE_READY: {
                DebugLog.write("MSG_UPLOAD_JANGLE_READY: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                if (bundle.getBoolean(KEY_HAS_COMPLETIONS)) {
                    String jangleUuid = bundle.getString(JANGLE_KEY);
                    String jangleOwnerToken = bundle.getString(JANGLE_OWNER_KEY);
                    ArrayList<String> completionList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                    mAppExecutors.diskIO().execute(() -> {
                        Bitmap[] bitmaps = mAssetBitmapGenerator.getScaledBitmap(BitmapImageType.DEF_JANGLE_IMAGE);
                        File[] files = mFileConverter.convertBitmapToFiles(bitmaps[0], bitmaps[1]);
                        mUpload.uploadCompletionWithFile(msg.arg1 + 1, msg.arg2 - 1, files, jangleOwnerToken, jangleUuid, completionList,
                                MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);

                    });

                } else {
                    DebugLog.write("NONE COMPLETIONS");

                }
                break;
            }
            case MSG_UPLOAD_COMPLETION_READY: {

                break;
            }


        }
        return false;
    }


}
