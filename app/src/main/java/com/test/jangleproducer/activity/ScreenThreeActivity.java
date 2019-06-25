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

import static com.test.jangleproducer.activity.MainActivity.JANGLE_KEY;
import static com.test.jangleproducer.activity.MainActivity.JANGLE_OWNER_KEY;
import static com.test.jangleproducer.activity.MainActivity.KEY_BIG_BITMAP;
import static com.test.jangleproducer.activity.MainActivity.KEY_BIG_FILE;
import static com.test.jangleproducer.activity.MainActivity.KEY_COMMON_DTO;
import static com.test.jangleproducer.activity.MainActivity.KEY_FILE_BUNDLE;
import static com.test.jangleproducer.activity.MainActivity.KEY_SMALL_BITMAP;
import static com.test.jangleproducer.activity.MainActivity.KEY_SMALL_FILE;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUB_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.MSG_BITMAP_COMPLETION_FILE_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_BITMAP_COMPLETION_IMAGE_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_BITMAP_JANGLE_FILE_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_BITMAP_JANGLE_IMAGE_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_JANGLE_AND_COMPLETIONS_FILES_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_LIKED_JANGLE;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_COMPLETION_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_COMPLETION_STARTING;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_COMPLETION_UPLOADING_CONTINUE;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_JANGLE_READY;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_JANGLE_STARTING;
import static com.test.jangleproducer.activity.MainActivity.MSG_UPLOAD_JANGLE_UPLOADING_CONTINUE;
import static com.test.jangleproducer.activity.MainActivity.MSG_USER_FOLLOWING_VOTES_COMPLETED;
import static com.test.jangleproducer.activity.MainActivity.MSG_USER_OTHER_VOTES_COMPLETED;
import static com.test.jangleproducer.activity.MainActivity.MSG_VOTE_NEXT_COMPLETION;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_KEY;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_LIST_KEY;

//1- get n number user token
//2-


public class ScreenThreeActivity extends AppCompatActivity implements Handler.Callback {

    private static int mainCounter = 0;

    //generate  bitmaps and files during network call


    static class DecisionHolder {

        final static int BASE_USER = 1;
        final static int MIN_USER = BASE_USER + 1;
        final static int MAX_USER = 50;

        final static int JANGLE_PER_USER = 1;
        final static int COMPLETION_PER_JANGLE = 7;
        final static int LIKE_COUNT = 5;

        final static boolean JANGLE_OWNER_IS_RANDOM = false;
        final static boolean JANGLE_HAS_COMPLETION = true;

        final static String JANGLE_OWNER_NAME = "testuser1000";
        final static String JANGLE_OWNER_PASS = "testuser1000";

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
        DebugLog.write();
        setContentView(R.layout.activity_screen_three);
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
        generateBitmap();

    }

    private void generateBitmap() {
        mAssetBitmapGenerator.getScaledJangleBitmapMsg(DecisionHolder.bitmapType);
    }

    @Override
    public boolean handleMessage(Message msg) {
        DebugLog.write("msg what=" + msg.what);
        switch (msg.what) {
            case MSG_BITMAP_JANGLE_IMAGE_READY: {
                DebugLog.write("MSG_BITMAP_JANGLE_IMAGE_READY: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                Bitmap bigBitmap = bundle.getParcelable(KEY_BIG_BITMAP);
                Bitmap smallBitmap = bundle.getParcelable(KEY_SMALL_BITMAP);
                mFileConverter.convertJangleBitmapToFilesMsg(bigBitmap, smallBitmap);
                break;
            }
            case MSG_BITMAP_COMPLETION_IMAGE_READY: {
                DebugLog.write("MSG_BITMAP_COMPLETION_IMAGE_READY: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                Bitmap bigBitmap = bundle.getParcelable(KEY_BIG_BITMAP);
                Bitmap smallBitmap = bundle.getParcelable(KEY_SMALL_BITMAP);
                CommonDto commonDto = (CommonDto) bundle.getSerializable(KEY_COMMON_DTO);
                mFileConverter.convertCompletionBitmapToFilesMsg(bigBitmap, smallBitmap, commonDto);
                break;
            }

            case MSG_BITMAP_JANGLE_FILE_READY: {
                DebugLog.write("MSG_BITMAP_JANGLE_FILE_READY: " + Thread.currentThread().getName());
                Bundle fileBundle = msg.getData();
                //File bigFile = (File) fileBundle.getSerializable(KEY_BIG_FILE);
                // File smallFile = (File) fileBundle.getSerializable(KEY_SMALL_FILE);
                //int docType= fileBundle.getInt(KEY_DOC_TYPE);
                if (mainUsersTokenList == null) {
                    DebugLog.write();
                    ArrayList<String> names = new ArrayList<>();
                    ArrayList<String> passwords = new ArrayList<>();
                    names.add(UserUtil.getUsername(DecisionHolder.JANGLE_OWNER_NAME)); //jangle owner
                    passwords.add(UserUtil.getPassword(DecisionHolder.JANGLE_OWNER_PASS)); //jangle owner
                    names.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER, DecisionHolder.MAX_USER));
                    passwords.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER, DecisionHolder.MAX_USER));
                    names.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER + 1000, DecisionHolder.MAX_USER + 1000));
                    passwords.addAll(UserUtil.getUsernameList("testuser", DecisionHolder.MIN_USER + 1000, DecisionHolder.MAX_USER + 1000));
                    //1 get token list
                    mUserToken.getTokenList(names, passwords, MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES, MSG_UPLOAD_JANGLE_STARTING,
                            fileBundle);
                } else {
                    DebugLog.write();
                    Message msg2 = Message.obtain();
                    msg2.setData(fileBundle); // set the bundle of the image files
                    msg2.what = MSG_UPLOAD_JANGLE_UPLOADING_CONTINUE;
                    this.handleMessage(msg2);
                }
                break;
            }
            case MSG_BITMAP_COMPLETION_FILE_READY: {
                DebugLog.write("MSG_BITMAP_COMPLETION_FILE_READY");
                Bundle fileBundle = msg.getData();
                Message msg2 = Message.obtain();
                msg2.setData(fileBundle); // set the bundle of the image files
                msg2.what = MSG_UPLOAD_COMPLETION_STARTING;
                this.handleMessage(msg2);
                break;
            }
            case MSG_UPLOAD_JANGLE_STARTING: {
                DebugLog.write("MSG_UPLOAD_JANGLE_STARTING: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES) {
                    mainUsersTokenList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                    int maxUsersAndFilesCount =
                            DecisionHolder.JANGLE_PER_USER + DecisionHolder.COMPLETION_PER_JANGLE;
                    Bundle fileBundle = bundle.getBundle(KEY_FILE_BUNDLE);
                    File bigFile = (File) fileBundle.getSerializable(KEY_BIG_FILE);
                    File smallFile = (File) fileBundle.getSerializable(KEY_SMALL_FILE);
                    ArrayList<String> randomUsers = UserUtil.getRandomDuplicateTokensFromList(DecisionHolder.JANGLE_OWNER_IS_RANDOM,
                            maxUsersAndFilesCount,
                            mainUsersTokenList);
                    mUpload.uploadJangleWithFile(DecisionHolder.JANGLE_OWNER_IS_RANDOM, DecisionHolder.JANGLE_HAS_COMPLETION,
                            DecisionHolder.COMPLETION_PER_JANGLE,
                            new File[]{bigFile, smallFile}, randomUsers);
                }
                break;
            }
            case MSG_UPLOAD_JANGLE_UPLOADING_CONTINUE: {
                DebugLog.write("MSG_UPLOAD_JANGLE_UPLOADING_CONTINUE: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                Bundle fileBundle = bundle.getBundle(KEY_FILE_BUNDLE);
                File bigFile = (File) fileBundle.getSerializable(KEY_BIG_FILE);
                File smallFile = (File) fileBundle.getSerializable(KEY_SMALL_FILE);
                int maxUsersAndFilesCount =
                        DecisionHolder.JANGLE_PER_USER + DecisionHolder.COMPLETION_PER_JANGLE;
                ArrayList<String> randomUsers = UserUtil.getRandomTokensFromList(maxUsersAndFilesCount, mainUsersTokenList);
                mUpload.uploadJangleWithFile(DecisionHolder.JANGLE_OWNER_IS_RANDOM, DecisionHolder.JANGLE_HAS_COMPLETION,
                        DecisionHolder.COMPLETION_PER_JANGLE, new File[]{bigFile,
                        smallFile}, randomUsers);
                break;

            }
            case MSG_UPLOAD_COMPLETION_STARTING: {
                DebugLog.write("MSG_UPLOAD_COMPLETION_STARTING: " + Thread.currentThread().getName());
                Bundle fileBundle = msg.getData();
                File bigFile = (File) fileBundle.getSerializable(KEY_BIG_FILE);
                File smallFile = (File) fileBundle.getSerializable(KEY_SMALL_FILE);
                CommonDto dto = (CommonDto) fileBundle.getSerializable(KEY_COMMON_DTO);
                DebugLog.write("CommonDto=" + dto.toString());
                mUpload.uploadCompletionWithFile(dto.getCompletionCountOfTheJangle(), new File[]{bigFile,
                        smallFile}, dto);
                break;
            }
            case MSG_UPLOAD_COMPLETION_UPLOADING_CONTINUE: {
                DebugLog.write("MSG_UPLOAD_COMPLETION_UPLOADING_CONTINUE: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                CommonDto dto = (CommonDto) bundle.getSerializable(KEY_COMMON_DTO);
                mAssetBitmapGenerator.getScaledCompletionBitmapMsg(DecisionHolder.bitmapType, dto);
                break;

            }
            case MSG_UPLOAD_JANGLE_READY: {
                DebugLog.write("MSG_UPLOAD_JANGLE_READY: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                CommonDto dto = (CommonDto) bundle.getSerializable(KEY_COMMON_DTO);
                if (dto.isJangleHasCompletion()) {
                    mAssetBitmapGenerator.getScaledCompletionBitmapMsg(DecisionHolder.bitmapType, dto);
                } else {
                    DebugLog.write("NONE COMPLETIONS");
                }
                break;

            }
            case MSG_UPLOAD_COMPLETION_READY: {
                DebugLog.write("MSG_UPLOAD_COMPLETION_READY: " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                CommonDto dto = (CommonDto) bundle.getSerializable(KEY_COMMON_DTO);
                DebugLog.write("JANGLE UUID= " + dto.getJangleUuid());
                DebugLog.write("JANGLE OWNER TOKEN= " + dto.getOwnerToken());
                DebugLog.write("OTHER USER COUNT= " + dto.getUsersToken().size());
                int likeCount = (dto.getUsersToken().size() <= DecisionHolder.LIKE_COUNT) ? dto.getUsersToken().size() : DecisionHolder.LIKE_COUNT;
                mHandleJangle.likeJangle(dto, likeCount, MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
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
                    mVoteCompletions.voteAllFollowingUsers(dto.getOwnerToken(), MessageSubject.ADD_JANGLE_COMPLETIONS_VOTES_LIKES);
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

                break;
            }


        }
        return false;
    }


}
