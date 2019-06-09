package com.test.jangleproducer.activity;

import static com.test.jangleproducer.Constants.USER_LIMIT;
import static com.test.jangleproducer.Constants.USERNAME_SUFFIX;
import static okhttp3.MediaType.parse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.AssetBitmapGenerator;
import com.test.jangleproducer.BitmapImageType;
import com.test.jangleproducer.Constants;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.FileConverter;
import com.test.jangleproducer.MessageSubject;
import com.test.jangleproducer.NetworkConnection;
import com.test.jangleproducer.R;
import com.test.jangleproducer.RandomBitmapGenerator;
import com.test.jangleproducer.RandomColorGenerator;
import com.test.jangleproducer.RandomWordGenerator;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.Uuid;
import com.test.jangleproducer.call.HandleJangle;
import com.test.jangleproducer.call.InterUsersFollow;
import com.test.jangleproducer.call.UpdateUserProfile;
import com.test.jangleproducer.call.UserToken;
import com.test.jangleproducer.call.VoteCompletions;
import com.test.jangleproducer.model.dispatch.DocType;
import com.test.jangleproducer.model.dispatch.RegisterModel;
import com.test.jangleproducer.model.dispatch.UploadVM;
import com.test.jangleproducer.model.result.AuthResponse;
import com.test.jangleproducer.model.result.UUID;
import com.test.jangleproducer.model.result.UploadResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    //bitmap
    public static final int MSG_BITMAP_JANGLE_IMAGE_READY =7000;
    public static final int MSG_BITMAP_COMPLETION_IMAGE_READY =7001;
    public static final int MSG_BITMAP_JANGLE_FILE_READY =7100;
    public static final int MSG_BITMAP_COMPLETION_FILE_READY =7101;

    //files
    public static final int MSG_JANGLE_AND_COMPLETIONS_FILES_READY = 1110;
    private static final int MSG_JANGLE_FILES_READY = 1111;
    private static final int MSG_COMPLETION_FILES_READY = 1112;
    public static final int MSG_UPLOAD_IMAGE_FILES_READY = 1114;
    public static final int MSG_LAST_JANGLE_COMPLETION_FILES_READY = 1115;

    //votes
    public static final int MSG_USER_OTHER_VOTES_COMPLETED = 1301;
    public static final int MSG_USER_FOLLOWING_VOTES_COMPLETED = 1302;
    public static final int MSG_VOTE_NEXT_COMPLETION = 1304;

    //handle jangle
    public static final int MSG_DEL_JANGLE_COMPLETED = 2601;
    public static final int MSG_DEL_NEXT_JANGLE = 2602;
    public static final int MSG_LAST_JANGLE = 2603;
    public static final int MSG_LIKED_JANGLE = 2604;
    public static final int MSG_UPLOAD_JANGLE_STARTING = 2605;
    public static final int MSG_UPLOAD_COMPLETION_STARTING = 2606;
    public static final int MSG_UPLOAD_JANGLE_UPLOADING_CONTINUE = 2607;
    public static final int MSG_UPLOAD_COMPLETION_UPLOADING_CONTINUE = 2608;
    public static final int MSG_UPLOAD_JANGLE_READY = 2609;
    public static final int MSG_UPLOAD_COMPLETION_READY = 2610;

    //tokens
    public static final int MSG_TOKEN_LIST_READY = 1200;
    public static final int MSG_TOKEN_READY = 1201;
    public static final int MSG_DOUBLE_TOKEN_READY = 1202;

    //bundle key
    public static final String MESSAGE_SUBJECT_KEY = "com.test.jangle_producer_message_subject";
    public static final String MESSAGE_SUB_SUBJECT_KEY = "com.test.jangle_producer_sub_message_subject";
    public static final String KEY_COMMON_DTO = "com.test.jangle_producer_common_dto";
    public static final String USER_TOKEN_LIST_KEY = "com.test.jangle_producer_token_list";
    public static final String USER_TOKEN_KEY = "com.test.jangle_producer_token";
    public static final String USER_TOKEN_KEY2 = "com.test.jangle_producer_token2";
    public static final String JANGLE_KEY = "com.test.jangle_producer_jangle";
    public static final String JANGLE_OWNER_KEY = "com.test.jangle_producer_jangle_owner";
    public static final String COMPLETION_COUNT_KEY = "com.test.jangle_producer_completion_count";
    public static final String FILE_LIST_KEY = "com.test.jangle_producer_file_list";
    public static final String KEY_BIG_BITMAP = "com.test.jangle_producer_big_bitmap";
    public static final String KEY_SMALL_BITMAP = "com.test.jangle_producer_small_bitmap";
    public static final String KEY_BIG_FILE = "com.test.jangle_producer_big_file";
    public static final String KEY_SMALL_FILE = "com.test.jangle_producer_small_file";
    public static final String KEY_DOC_TYPE = "com.test.jangle_producer_doc_type";
    public static final String KEY_HAS_COMPLETIONS ="com.test.jangle_producer_jangle_has_completions";
    public static final String KEY_FILE_BUNDLE= "com.test.jangle_producer_jangle_file_bundle";


    private TestService mService;
    private AppExecutors mAppExecutors;
    private FileConverter mFileConverter;
    private AssetBitmapGenerator mAssetBitmapGenerator;
    private RandomBitmapGenerator mRandomBitmapGenerator;
    private RandomColorGenerator mRandomColorGenerator;
    private RandomWordGenerator mRandomWordGenerator;
    private Gson mGson;
    private Random mRandom;

    private UserToken mUserToken;
    private VoteCompletions mVoteCompletions;
    private HandleJangle mHandleJangle;

    private File[] mJangleFile;
    private File[] mJangleFile2;
    private File[] mJangleFile3;
    private String mLastJangleUuid;
    private int mCompletionCounter = 1;
    private int mUserCounter = 1;
    private int mUsernameSuf = USERNAME_SUFFIX;
    private boolean islogHttp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mService = NetworkConnection.get(islogHttp);
        mAppExecutors = new AppExecutors();
        mAssetBitmapGenerator = new AssetBitmapGenerator(this,mAppExecutors);
        mFileConverter = new FileConverter(mAppExecutors, this);
        mRandomColorGenerator = new RandomColorGenerator();
        mRandomWordGenerator = new RandomWordGenerator();
        mRandomBitmapGenerator = new RandomBitmapGenerator(this, mRandomColorGenerator, mRandomWordGenerator);
        mGson = new GsonBuilder().setLenient().create();
        mRandom = new Random();
        mUserToken = new UserToken(mService, mAppExecutors, this);
        mVoteCompletions = new VoteCompletions(mService, mAppExecutors, this);
        mHandleJangle = new HandleJangle(mService, mAppExecutors, this);
    }

    //region CLICK EVENT

    //1- create
    public void addUsers(View view) {
        DebugLog.write();
        createUser(1000);
    }

    //2- hide account
    public void hideAccount(View view) {
        DebugLog.write();
        UpdateUserProfile updateUserProfile = new UpdateUserProfile(mService, mAppExecutors);
        updateUserProfile.hideSelectedAccounts(301);
    }

    //3- send follow request
    public void addFollowRequest(View view) {
        DebugLog.write();
        InterUsersFollow interUsersFollow = new InterUsersFollow(mAppExecutors, mService);
       // interUsersFollow.runInterUserFollow();
      interUsersFollow.sendFollowRequestFromUser(102,70, Uuid.TESTUSER100);
    }


    //4- add a jangle
    public void addJangle(View view) {
        DebugLog.write();
        addJangleForUsers();
    }


    //5- add completions
    public void addCompletion(View view) {
        sendCompletion();
    }

    public void uploadJangleAndComp(View view) {
        addJangleAndCompletions();
    }

    //6-vote all others
    public void voteAllOthers(View view) {
        DebugLog.write();
        // votingOtherUser();
        votingFollowingUser();
    }

    public void updateProfileName(View view) {
        DebugLog.write("updateProfileName : " + Thread.currentThread().getName());
        UpdateUserProfile updateUserProfile = new UpdateUserProfile(mService, mAppExecutors);
        updateUserProfile.updateProfileName(1, "testuser");
    }


    //-update username
    public void uploadProfileImage(View view) {
        DebugLog.write("uploadProfileImage : " + Thread.currentThread().getName());
        uploadUserProfileImage();

    }

    public void deleteJangle(View view) {
        DebugLog.write();
        deleteJangleOfUser();
    }

    public void sendCompLastJangle(View view) {
        sendCompletionsToLastJangle(55);
    }

    public void sendCompLastJangleGivenUsers(View view) {
        String[] userArray = {"testuser1", "testuser4", "testuser1001", "testuser1002", "testuser2", "testuser3", "testuser1003", "testuser1004"};
        sendCompletionGivenJangleWithUser(new ArrayList<String>(Arrays.asList(userArray)), 5);
    }
    //endregion


    //region CALLBACK MESSAGES
    @Override
    public boolean handleMessage(Message msg) {
        DebugLog.write("msg what=" + msg.what);
        switch (msg.what) {
            //region FILES
            case MSG_JANGLE_FILES_READY: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                Bundle bundle = msg.getData();
                String token = bundle.getString(USER_TOKEN_KEY);
                UploadVM model = new UploadVM(mRandomWordGenerator.getWord(), DocType.JANGLE);
                uploadImageJangle(DocType.JANGLE, mJangleFile[0], mJangleFile[1], model, token);
                break;
            }
            case MSG_COMPLETION_FILES_READY: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                if (mLastJangleUuid != null) {
                    DebugLog.write();
                    UploadVM model = new UploadVM(mRandomWordGenerator.getWord(), DocType.COMPLETION, mLastJangleUuid, mCompletionCounter);
                    uploadImageJangle(DocType.COMPLETION, mJangleFile[0], mJangleFile[1], model, "");
                }
                break;
            }
            case MSG_UPLOAD_IMAGE_FILES_READY: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                DebugLog.write("MSG_UPLOAD_IMAGE_FILES_READY  : " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                String token = bundle.getString(USER_TOKEN_KEY);
                uploadImageProfile(mJangleFile[0], mJangleFile[1], token);
                break;
            }
            case MSG_JANGLE_AND_COMPLETIONS_FILES_READY: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                Bundle bundle = msg.getData();
                //add subject
                /*
                String token1 = bundle.getString(USER_TOKEN_KEY);
                String token2 = bundle.getString(USER_TOKEN_KEY2);
                uploadJangleAndCompletions(mJangleFile[0], mJangleFile[1], mJangleFile2[0], mJangleFile2[1], mJangleFile3[0],
                        mJangleFile3[1], token1, token2);*/
                break;
            }
            case MSG_LAST_JANGLE: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                Bundle bundle = msg.getData();
                int completionCount = bundle.getInt(COMPLETION_COUNT_KEY);
                String jangleUuid = bundle.getString(JANGLE_KEY);
                ArrayList<String> tokenList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.UPLOAD_COMPLETIONS_TO_LAST_JANGLE) {
                    DebugLog.write();
                    ArrayList<Bitmap[]> bitmapList = mAssetBitmapGenerator.getScaledBitmapList(BitmapImageType.DEF_JANGLE_IMAGE, completionCount);
                    try {
                        Runnable run = new SaveBitmapCompletionFileRunnable(mFileConverter, bitmapList, this, tokenList, jangleUuid);
                        mAppExecutors.diskIO().execute(run);
                    } catch (Exception e) {
                        DebugLog.write();
                    }
                }
                break;
            }
            case MSG_LAST_JANGLE_COMPLETION_FILES_READY: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                Bundle bundle = msg.getData();
                String jangleUuid = bundle.getString(JANGLE_KEY);
                ArrayList<String> tokenList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                ArrayList<File[]> fileList = (ArrayList<File[]>) bundle.getSerializable(FILE_LIST_KEY);
                uploadCompletionsToLastJangle(1, jangleUuid, tokenList, fileList);
                break;
            }
            //endregion
            //region VOTES
            case MSG_VOTE_NEXT_COMPLETION: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                Bundle bundle = msg.getData();
                String token = bundle.getString(USER_TOKEN_KEY);
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                DebugLog.write(subject);
                if (subject == MessageSubject.VOTE_COMPLETION_OTHERS) {
                    mVoteCompletions.voteAllOtherUsers(token);
                } else if (subject == MessageSubject.VOTE_COMPLETION_FOLLOWINGS) {
                    mVoteCompletions.voteAllFollowingUsers(token);
                }
                break;
            }
            case MSG_USER_OTHER_VOTES_COMPLETED: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                if (mUsernameSuf < USER_LIMIT) {
                    mUserToken.getToken(++mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.VOTE_COMPLETION_OTHERS);
                }
                break;
            }
            case MSG_USER_FOLLOWING_VOTES_COMPLETED: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                if (mUsernameSuf < USER_LIMIT) {

                    mUserToken.getToken(++mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.VOTE_COMPLETION_FOLLOWINGS);
                }
                break;
            }
            //endregion
            //region DEL JANGLE
            case MSG_DEL_NEXT_JANGLE: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                Bundle bundle = msg.getData();
                String token = bundle.getString(USER_TOKEN_KEY);
                mHandleJangle.deleteJangle(token);
                break;
            }
            case MSG_DEL_JANGLE_COMPLETED: {
                DebugLog.write("username-suf=" + mUsernameSuf);
                if (mUserCounter < USER_LIMIT) {
                    mUserCounter++;
                    mUserToken.getToken(++mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.DELETE_JANGLE);
                }
                break;
            }
            //endregion

            //region TOKEN
            case MSG_TOKEN_LIST_READY: {
                DebugLog.write();
                Bundle bundle = msg.getData();

                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                if (subject == MessageSubject.UPLOAD_COMPLETIONS_TO_LAST_JANGLE) {
                    DebugLog.write();
                    int completionCount = bundle.getInt(COMPLETION_COUNT_KEY);
                    ArrayList<String> tokenList = bundle.getStringArrayList(USER_TOKEN_LIST_KEY);
                    mHandleJangle.getLastJangle(tokenList, completionCount, subject);
                } else if (subject == MessageSubject.UPLOAD_COMPLETIONS_TO_LAST_JANGLE_WITH_USERS) {
                    String jangleUuid = bundle.getString(JANGLE_KEY);

                }
                break;
            }
            case MSG_DOUBLE_TOKEN_READY: {
                DebugLog.write();
                Bundle bundle = msg.getData();
                String token1 = bundle.getString(USER_TOKEN_KEY);
                String token2 = bundle.getString(USER_TOKEN_KEY2);
                try {
                    Bitmap[] bitmaps1 = mAssetBitmapGenerator.getScaledBitmap(BitmapImageType.DEF_JANGLE_IMAGE);
                    Bitmap[] bitmaps2 = mAssetBitmapGenerator.getScaledBitmap(BitmapImageType.DEF_JANGLE_IMAGE);
                    Bitmap[] bitmaps3 = mAssetBitmapGenerator.getScaledBitmap(BitmapImageType.DEF_JANGLE_IMAGE);
                    Runnable runnableBitmap = new SaveB3itmapToFileRunnable(mFileConverter, bitmaps1, bitmaps2, bitmaps3, this::handleMessage, token1,
                            token2);
                    mAppExecutors.diskIO().execute(runnableBitmap);
                } catch (Exception e) {
                    DebugLog.write(e.getMessage());
                }
                break;
            }
            case MSG_TOKEN_READY: {
                DebugLog.write("MSG TOKEN READY  : " + Thread.currentThread().getName());
                Bundle bundle = msg.getData();
                String token = bundle.getString(USER_TOKEN_KEY);
                MessageSubject subject = (MessageSubject) bundle.getSerializable(MESSAGE_SUBJECT_KEY);
                switch (subject) {
                    case UPLOAD_IMAGE: {
                        try {
                            Bitmap[] bitmaps = mAssetBitmapGenerator.getScaledBitmap(BitmapImageType.AVATAR);
                            Runnable runUploadImage = new SaveBitmapToFileRunnable(mFileConverter, bitmaps, this::handleMessage,
                                    DocType.UPLOAD_IMAGE, token);
                            mAppExecutors.diskIO().execute(runUploadImage);
                        } catch (Exception e) {
                            DebugLog.write(e.getMessage());
                        }
                        break;
                    }
                    case UPLOAD_JANGLE: {
                        try {
                            // Bitmap[] bitmaps = mRandomBitmapGenerator.genResourcedBitmap();
                            Bitmap[] bitmaps = mAssetBitmapGenerator.getScaledBitmap(BitmapImageType.DEF_JANGLE_IMAGE);
                            Runnable runnableBitmap = new SaveBitmapToFileRunnable(mFileConverter, bitmaps, this::handleMessage, DocType.JANGLE,
                                    token);
                            mAppExecutors.diskIO().execute(runnableBitmap);
                        } catch (Exception e) {
                            DebugLog.write(e.getMessage());
                        }
                        break;
                    }
                    case VOTE_COMPLETION_OTHERS: {
                        DebugLog.write();
                        mVoteCompletions.voteAllOtherUsers(token);
                        break;
                    }
                    case VOTE_COMPLETION_FOLLOWINGS: {
                        DebugLog.write();
                        mVoteCompletions.voteAllFollowingUsers(token);
                        break;
                    }
                    case DELETE_JANGLE: {
                        DebugLog.write();
                        mHandleJangle.deleteJangle(token);
                        break;
                    }
                }
            }
            //endregion TOKEN
        }
        return false;
    }
    //endregion

    //region METHODS
    void createUser(final int i) {
        mAppExecutors.networkIO().execute(() -> {
            Call<AuthResponse> call = mService.register(new RegisterModel("testuser" + i));
            try {
                Response<AuthResponse> response = call.execute();
                if (response.body() != null && response.body().getToken() != null && i < 1101) {
                    createUser(i + 1);
                }
            } catch (IOException e) {
                DebugLog.write(e.getMessage());
            }
        });
    }

    private void addJangleForUsers() {
        DebugLog.write();
        mUserToken.getToken(mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.UPLOAD_JANGLE);
    }

    private void uploadUserProfileImage() {
        DebugLog.write();
        mUserToken.getToken(mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.UPLOAD_IMAGE);
    }

    private void addJangleAndCompletions() {
        DebugLog.write();
        mUserToken.getSequenceUserToken(mUsernameSuf++, Constants.PROFILE_BASE_NAME);
    }

    private void votingOtherUser() {
        DebugLog.write();
        mUserToken.getToken(mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.VOTE_COMPLETION_OTHERS);
    }

    private void votingFollowingUser() {
        DebugLog.write();
        mUserToken.getToken(mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.VOTE_COMPLETION_FOLLOWINGS);
    }

    public void sendCompletionsToLastJangle(int completionCount) {
        DebugLog.write();
        mUserToken.getTokenList(mUsernameSuf, Constants.PROFILE_BASE_NAME, USER_LIMIT, completionCount,
                MessageSubject.UPLOAD_COMPLETIONS_TO_LAST_JANGLE);
    }

    public void sendCompletionGivenJangleWithUser(ArrayList<String> completenioners, int completionCount) {
        DebugLog.write();
        mUserToken.getTokenList(completenioners, completionCount,
                MessageSubject.UPLOAD_COMPLETIONS_TO_LAST_JANGLE_WITH_USERS);
    }

    private void sendCompletion() {/*
        try {
            Bitmap[] bitmaps = mRandomBitmapGenerator.genResourcedBitmap();
            //  new SaveBitmapToFileRunnable(mFileConverter, bitmaps, this::handleMessage, DocType.COMPLETION).run();
        } catch (Exception e) {
            DebugLog.write(e.getMessage());
        }*/
    }

    private void deleteJangleOfUser() {
        mUserToken.getToken(mUsernameSuf, Constants.PROFILE_BASE_NAME, MessageSubject.DELETE_JANGLE);
    }
    //endregion


    //region UPLOAD
    public void uploadImageProfile(File image, File thumbnail, String token) {
        MultipartBody.Part fileImage = MultipartBody.Part
                .createFormData("file", "pro_big.jpg", RequestBody.create(parse("multipart/form-data"), image));
        MultipartBody.Part fileThumbnail = MultipartBody.Part
                .createFormData("thumbnail", "pro_sml.jpg", RequestBody.create(parse("multipart/form-data"), thumbnail));

        Map<String, String> authMap = new HashMap<>();
        authMap.put(Constants.AUTHORIZATION, Constants.BEARER + token);
        mAppExecutors.networkIO().execute(() -> {
            DebugLog.write("uploadImageProfile networkIO : " + Thread.currentThread().getName());
            Call<Void> call2 = mService.uploadImage(fileImage, fileThumbnail, authMap);
            try {
                Response<Void> response = call2.execute();
                if (response.isSuccessful()) {
                    if (mUsernameSuf < USER_LIMIT) {
                        uploadUserProfileImage();

                        mUsernameSuf++;
                    }
                }
            } catch (IOException e) {
                DebugLog.write();
            }
        });
    }

    private void uploadImageJangle(DocType type, File image, File thumbnail, UploadVM model, String token) {

        DebugLog.write("counter=" + mUserCounter);
        RequestBody modelBody = RequestBody.create(parse("application/json"), mGson.toJson(model));
        MultipartBody.Part fileImage = MultipartBody.Part
                .createFormData("file", "bg.jpg", RequestBody.create(parse("multipart/form-data"), image));
        MultipartBody.Part imageUrl = MultipartBody.Part
                .createFormData("preview", "bg.jpg", RequestBody.create(parse("multipart/form-data"), image));
        MultipartBody.Part fileThumbnail = MultipartBody.Part
                .createFormData("thumbnail", "sml.jpg", RequestBody.create(parse("multipart/form-data"), thumbnail));
        Map<String, String> authMap = new HashMap<>();
        authMap.put(Constants.AUTHORIZATION, Constants.BEARER + token);
        mAppExecutors.networkIO().execute(() -> {
            Call<UploadResponse> call2 = mService.uploadJangle(modelBody, fileImage, imageUrl, fileThumbnail, authMap);
            try {
                Response<UploadResponse> response2 = call2.execute();
                DebugLog.write(response2.code());
                if (type == DocType.JANGLE) {
                    DebugLog.write();
                    mLastJangleUuid = response2.body().getUuid();
                    if (mUsernameSuf < USER_LIMIT) {
                        addJangleForUsers();
                      //  mUsernameSuf++;
                    } else {

                    }

                } else if (type == DocType.COMPLETION) {
                    DebugLog.write();
                    mCompletionCounter++;
                    if (mCompletionCounter <= Constants.COMPLETION_COUNT) {
                        sendCompletion();
                    } else {
                        mCompletionCounter = 1;
                    }
                }
            } catch (Exception e) {
                DebugLog.write(e.getMessage());
            }
        });
    }

    private void uploadCompletionsToLastJangle(int counter, String jangleUuid, ArrayList<String> tokenList,
                                               ArrayList<File[]> fileList) {

        if (fileList != null && fileList.size() > 0) {
            DebugLog.write();
            File[] files = fileList.remove(fileList.size() - 1);
            fileList.trimToSize();
            UploadVM uploadModel = new UploadVM(mRandomWordGenerator.getWord(), DocType.COMPLETION, jangleUuid, counter);
            RequestBody modelBody = RequestBody.create(parse("application/json"), mGson.toJson(uploadModel));
            MultipartBody.Part fileImage = MultipartBody.Part
                    .createFormData("file", counter + "_bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
            MultipartBody.Part imageUrl = MultipartBody.Part
                    .createFormData("preview", counter + "_bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
            MultipartBody.Part fileThumbnail = MultipartBody.Part
                    .createFormData("thumbnail", counter + "_sml.jpg", RequestBody.create(parse("multipart/form-data"), files[1]));
            int tokenListIndex = tokenList.size() - 1;
            int userIndex = mRandom.nextInt(tokenListIndex);
            String compToken = tokenList.get(userIndex);
            Map<String, String> authMapComp = new HashMap<>();
            authMapComp.put(Constants.AUTHORIZATION, Constants.BEARER + compToken);
            mAppExecutors.networkIO().execute(() -> {
                Call<UploadResponse> call2 = mService.uploadJangle(modelBody, fileImage, imageUrl, fileThumbnail,
                        authMapComp);
                try {
                    Response<UploadResponse> response = call2.execute();
                    if (response.code() == 200) {
                        uploadCompletionsToLastJangle(counter + 1, jangleUuid, tokenList, fileList);
                    }
                } catch (Exception e) {
                    DebugLog.write();
                }
            });
        } else {
            DebugLog.write();

            if (mUsernameSuf < USER_LIMIT) {
                ++mUsernameSuf;
                sendCompletionsToLastJangle(15);
            }
        }
    }

    private void uploadJangleAndCompletions(File image1, File thumbnail1, File image2, File thumbnail2, File image3,
                                            File thumbnail3, String janglerToken,
                                            String compToken) {

        DebugLog.write("counter=" + mUserCounter);
        UploadVM model1 = new UploadVM(mRandomWordGenerator.getWord(), DocType.JANGLE);
        RequestBody modelBodyJangle = RequestBody.create(parse("application/json"), mGson.toJson(model1));
        MultipartBody.Part fileImageJangle = MultipartBody.Part
                .createFormData("file", "bg.jpg", RequestBody.create(parse("multipart/form-data"), image1));
        MultipartBody.Part imageUrlJangle = MultipartBody.Part
                .createFormData("preview", "bg.jpg", RequestBody.create(parse("multipart/form-data"), image1));
        MultipartBody.Part fileThumbnailJangle = MultipartBody.Part
                .createFormData("thumbnail", "sml.jpg", RequestBody.create(parse("multipart/form-data"), thumbnail1));


        MultipartBody.Part fileImageComp2 = MultipartBody.Part
                .createFormData("file", "bg2.jpg", RequestBody.create(parse("multipart/form-data"), image2));
        MultipartBody.Part imageUrlComp2 = MultipartBody.Part
                .createFormData("preview", "bg2.jpg", RequestBody.create(parse("multipart/form-data"), image2));
        MultipartBody.Part fileThumbnailComp2 = MultipartBody.Part
                .createFormData("thumbnail", "sml2.jpg", RequestBody.create(parse("multipart/form-data"), thumbnail2));


        MultipartBody.Part fileImageComp3 = MultipartBody.Part
                .createFormData("file", "bg3.jpg", RequestBody.create(parse("multipart/form-data"), image3));
        MultipartBody.Part imageUrlComp3 = MultipartBody.Part
                .createFormData("preview", "bg3.jpg", RequestBody.create(parse("multipart/form-data"), image3));
        MultipartBody.Part fileThumbnailComp3 = MultipartBody.Part
                .createFormData("thumbnail", "sml3.jpg", RequestBody.create(parse("multipart/form-data"), thumbnail3));


        Map<String, String> authMap = new HashMap<>();
        authMap.put(Constants.AUTHORIZATION, Constants.BEARER + janglerToken);
        mAppExecutors.networkIO().execute(() -> {
            Call<UploadResponse> call1 = mService.uploadJangle(modelBodyJangle, fileImageJangle, imageUrlJangle, fileThumbnailJangle, authMap);
            try {
                Response<UploadResponse> response1 = call1.execute();
                DebugLog.write(response1.code());
                if (response1.isSuccessful()) {
                    DebugLog.write();
                    mLastJangleUuid = response1.body().getUuid();
                    Map<String, String> authMapComp = new HashMap<>();
                    authMapComp.put(Constants.AUTHORIZATION, Constants.BEARER + compToken);
                    UploadVM model2 = new UploadVM(mRandomWordGenerator.getWord(), DocType.COMPLETION, mLastJangleUuid, 1);
                    RequestBody modelBodyComp2 = RequestBody.create(parse("application/json"), mGson.toJson(model2));
                    Call<UploadResponse> call2 = mService.uploadJangle(modelBodyComp2, fileImageComp2, imageUrlComp2, fileThumbnailComp2,
                            authMapComp);
                    Response<UploadResponse> response2 = call2.execute();
                    if (response2.isSuccessful()) {
                        UploadVM model3 = new UploadVM(mRandomWordGenerator.getWord(), DocType.COMPLETION, mLastJangleUuid, 2);
                        RequestBody modelBodyComp3 = RequestBody.create(parse("application/json"), mGson.toJson(model3));
                        Call<UploadResponse> call3 = mService.uploadJangle(modelBodyComp3, fileImageComp3, imageUrlComp3, fileThumbnailComp3,
                                authMapComp);
                        Response<UploadResponse> response3 = call3.execute();
                        if (response3.isSuccessful()) {

                            if (mUserCounter < USER_LIMIT) {
                                addJangleAndCompletions();
                                mUserCounter++;
                            }
                        }
                    }

                }

            } catch (Exception e) {
                DebugLog.write(e.getMessage());
            }
        });
    }

    //endregion


    //region RUNNABLE

    class LooperThread extends Thread {
        public Handler mHandler;

        public void run() {
            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here
                    // this will run in non-ui/background thread
                }
            };

            Looper.loop();
        }
    }

    private class MyHandlerThread extends HandlerThread {

        Handler handler;

        public MyHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    // process incoming messages here
                    // this will run in non-ui/background thread
                }
            };
        }
    }

    private class SaveBitmapToFileRunnable implements Runnable {

        private FileConverter mFileConverter;
        private Handler.Callback callback;
        private Bitmap[] bitmaps;
        private DocType mDocType;
        private String mToken;


        SaveBitmapToFileRunnable(FileConverter converter, Bitmap[] bitmaps, Handler.Callback callback, DocType type, String token) {
            this.mFileConverter = converter;
            this.bitmaps = bitmaps;
            this.callback = callback;
            this.mDocType = type;
            this.mToken = token;

        }

        @Override
        public void run() {
            DebugLog.write("SaveBitmapToFileRunnable  : " + Thread.currentThread().getName());
            mJangleFile = mFileConverter.convertBitmapToFiles(bitmaps[0], bitmaps[1]);
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(USER_TOKEN_KEY, mToken);
            if (mDocType == DocType.JANGLE) {
                msg.what = MSG_JANGLE_FILES_READY;
            } else if (mDocType == DocType.COMPLETION) {
                msg.what = MSG_COMPLETION_FILES_READY;
            } else if (mDocType == DocType.UPLOAD_IMAGE) {
                msg.what = MSG_UPLOAD_IMAGE_FILES_READY;

            }
            msg.setData(bundle);
            callback.handleMessage(msg);
        }
    }

    private class SaveBitmapCompletionFileRunnable implements Runnable {

        private FileConverter mFileConverter;
        private Handler.Callback callback;
        private ArrayList<Bitmap[]> mBitmapList;
        private ArrayList<String> mUserTokenList;
        private String mJangleUuid;


        SaveBitmapCompletionFileRunnable(FileConverter converter, ArrayList<Bitmap[]> bitmapList, Handler.Callback callback,
                                         ArrayList<String> userTokenList, String jangleUuid) {
            this.mFileConverter = converter;
            this.mBitmapList = bitmapList;
            this.callback = callback;
            this.mUserTokenList = userTokenList;
            this.mJangleUuid = jangleUuid;

        }

        @Override
        public void run() {
            DebugLog.write();
            ArrayList<File[]> fileList = mFileConverter.convertBitmapListToFileList(mBitmapList);
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            msg.what = MSG_LAST_JANGLE_COMPLETION_FILES_READY;
            bundle.putString(JANGLE_KEY, mJangleUuid);
            bundle.putStringArrayList(USER_TOKEN_LIST_KEY, mUserTokenList);
            bundle.putSerializable(FILE_LIST_KEY, fileList);
            msg.setData(bundle);
            callback.handleMessage(msg);
        }
    }

    private class SaveB3itmapToFileRunnable implements Runnable {

        private FileConverter mFileConverter;
        private Handler.Callback callback;
        private Bitmap[] bitmaps1;
        private Bitmap[] bitmaps2;
        private Bitmap[] bitmaps3;
        private String mToken1;
        private String mToken2;

        SaveB3itmapToFileRunnable(FileConverter converter, Bitmap[] bitmaps1, Bitmap[] bitmaps2, Bitmap[] bitmaps3, Handler.Callback callback,
                                  String token1, String token2) {
            this.mFileConverter = converter;
            this.bitmaps1 = bitmaps1;
            this.bitmaps2 = bitmaps2;
            this.bitmaps3 = bitmaps3;
            this.callback = callback;
            this.mToken1 = token1;
            this.mToken2 = token2;
        }

        @Override
        public void run() {
            DebugLog.write();
            mJangleFile = mFileConverter.convertBitmapToFiles(bitmaps1[0], bitmaps1[1]);
            mJangleFile2 = mFileConverter.convertBitmapToFiles(bitmaps2[0], bitmaps2[1]);
            mJangleFile3 = mFileConverter.convertBitmapToFiles(bitmaps3[0], bitmaps3[1]);

            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(USER_TOKEN_KEY, mToken1);
            bundle.putString(USER_TOKEN_KEY2, mToken2);
            msg.what = MSG_JANGLE_AND_COMPLETIONS_FILES_READY;
            msg.setData(bundle);
            callback.handleMessage(msg);
        }
    }
    //endregion

    //navigation
    public void navigateScreenTwo(View view) {
        DebugLog.write();
        Intent intent = new Intent(MainActivity.this, ScreenTwoActivity.class);
        startActivity(intent);

    }

    public void navigateScreenThree(View view) {
        DebugLog.write();
        Intent intent = new Intent(MainActivity.this, ScreenThreeActivity.class);
        startActivity(intent);

    }

}



