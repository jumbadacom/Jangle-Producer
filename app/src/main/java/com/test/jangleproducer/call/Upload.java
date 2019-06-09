package com.test.jangleproducer.call;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.test.jangleproducer.AppExecutors;
import com.test.jangleproducer.Constants;
import com.test.jangleproducer.DebugLog;
import com.test.jangleproducer.MessageSubject;
import com.test.jangleproducer.RandomWordGenerator;
import com.test.jangleproducer.TestService;
import com.test.jangleproducer.activity.MainActivity;
import com.test.jangleproducer.activity.ScreenThreeActivity;
import com.test.jangleproducer.activity.ScreenTwoActivity;
import com.test.jangleproducer.model.CommonDto;
import com.test.jangleproducer.model.dispatch.DocType;
import com.test.jangleproducer.model.dispatch.UploadVM;
import com.test.jangleproducer.model.result.UploadResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.test.jangleproducer.activity.MainActivity.JANGLE_KEY;
import static com.test.jangleproducer.activity.MainActivity.JANGLE_OWNER_KEY;
import static com.test.jangleproducer.activity.MainActivity.KEY_COMMON_DTO;
import static com.test.jangleproducer.activity.MainActivity.MESSAGE_SUBJECT_KEY;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_LIST_KEY;
import static okhttp3.MediaType.parse;

public class Upload {

    private Handler.Callback callback;
    private AppExecutors mAppExecutor;
    private Gson mGson;
    private TestService mService;
    private RandomWordGenerator mRandomWordGenerator;
    private Random mRandom;

    public Upload(TestService testService, AppExecutors appExecutors, Gson gson, RandomWordGenerator randomWordGenerator,
                  Random random,
                  AppCompatActivity activity) {
        mAppExecutor = appExecutors;
        mGson = gson;
        mService = testService;
        mRandomWordGenerator = randomWordGenerator;

        mRandom = random;
        if (activity instanceof MainActivity) {
            this.callback = (MainActivity) activity;
        } else if (activity instanceof ScreenTwoActivity) {
            this.callback = (ScreenTwoActivity) activity;
        } else if (activity instanceof ScreenThreeActivity) {
            this.callback = (ScreenThreeActivity) activity;
        } else {
            throw new IllegalArgumentException("Wrong Upload Activity");
        }
    }

    public void uploadJangleWithFile(boolean isRandomUser, boolean hasCompletion, int completionCount, File[] files, ArrayList<String> tokenList) {
        //get random user to upload a jangle
        String jangleOwnerToken;
        if (isRandomUser) {
            int tokenListIndex = tokenList.size() - 1;
            int userIndex = mRandom.nextInt(tokenListIndex);
            jangleOwnerToken = tokenList.remove(userIndex);
            tokenList.trimToSize();
        } else {
            jangleOwnerToken = tokenList.remove(0);
            tokenList.trimToSize();
        }
        //get jangle file
        //prepare jangle model
        UploadVM model = new UploadVM(mRandomWordGenerator.getWord(), DocType.JANGLE);
        RequestBody modelBody = RequestBody.create(parse("application/json"), mGson.toJson(model));
        MultipartBody.Part fileImage = MultipartBody.Part
                .createFormData("file", "bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
        MultipartBody.Part imageUrl = MultipartBody.Part
                .createFormData("preview", "bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
        MultipartBody.Part fileThumbnail = MultipartBody.Part
                .createFormData("thumbnail", "sml.jpg", RequestBody.create(parse("multipart/form-data"), files[1]));
        mAppExecutor.networkIO().execute(() -> {
            Map<String, String> authMap = new HashMap<>();
            authMap.put(Constants.AUTHORIZATION, Constants.BEARER + jangleOwnerToken);
            Call<UploadResponse> callJangle = mService.uploadJangle(modelBody, fileImage, imageUrl, fileThumbnail, authMap);
            try {
                Response<UploadResponse> response = callJangle.execute();
                if (response.code() == 200) {
                    mAppExecutor.mainThread().execute(() -> {
                        DebugLog.write();
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        CommonDto dto = new CommonDto();
                        dto.setJangleUuid(response.body().getUuid());
                        dto.setOwnerToken(jangleOwnerToken);
                        dto.setUsersToken(tokenList);
                        if (hasCompletion) {
                            dto.setCompletionCountOfTheJangle(completionCount);
                            dto.setJangleHasCompletion(hasCompletion);
                            dto.setUploadCompletionCounter(1);
                        }
                        bundle.putSerializable(KEY_COMMON_DTO, dto);
                        message.what = MainActivity.MSG_UPLOAD_JANGLE_READY;
                        message.setData(bundle);
                        callback.handleMessage(message);
                    });

                }
            } catch (Exception e) {
                DebugLog.write();
            }
        });

    }

    public void uploadCompletionWithFile(int completionCount, File[] files, CommonDto dto) {
        if (completionCount > 0) {
            int tokenListIndex = dto.getUsersToken().size() - 1;
            int userIndex = mRandom.nextInt(tokenListIndex);
            String compOwnerToken = dto.getUsersToken().get(userIndex);
            //prepare jangle model
            UploadVM uploadModel = new UploadVM(mRandomWordGenerator.getWord(), DocType.COMPLETION, dto.getJangleUuid(),
                    dto.getUploadCompletionCounter());
            RequestBody modelBody = RequestBody.create(parse("application/json"), mGson.toJson(uploadModel));
            MultipartBody.Part fileImage = MultipartBody.Part
                    .createFormData("file", dto.getUploadCompletionCounter() + "_bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
            MultipartBody.Part imageUrl = MultipartBody.Part
                    .createFormData("preview", dto.getUploadCompletionCounter() + "_bg.jpg", RequestBody.create(parse("multipart/form-data"),
                            files[0]));
            MultipartBody.Part fileThumbnail = MultipartBody.Part
                    .createFormData("thumbnail", dto.getUploadCompletionCounter() + "_sml.jpg", RequestBody.create(parse("multipart/form-data"),
                            files[1]));
            mAppExecutor.diskIO().execute(() -> {
                DebugLog.write("UploadCompletionWithFile networkIO " + Thread.currentThread().getName());
                // DebugLog.write("CompOwnerToken=" + compOwnerToken);
                Map<String, String> authMapComp = new HashMap<>();
                authMapComp.put(Constants.AUTHORIZATION, Constants.BEARER + compOwnerToken);
                Call<UploadResponse> call2 = mService.uploadJangle(modelBody, fileImage, imageUrl, fileThumbnail,
                        authMapComp);
                try {
                    Response<UploadResponse> response = call2.execute();
                    if (response.code() == 200) {
                        DebugLog.write();
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        dto.setUploadCompletionCounter(dto.getUploadCompletionCounter() + 1);
                        dto.setCompletionCountOfTheJangle(dto.getCompletionCountOfTheJangle() - 1);
                        DebugLog.write("COMMON DTO= " + dto.toString());
                        bundle.putSerializable(KEY_COMMON_DTO, dto);
                        message.what = MainActivity.MSG_UPLOAD_COMPLETION_UPLOADING_CONTINUE;
                        message.setData(bundle);
                        callback.handleMessage(message);
                    }

                } catch (Exception e) {
                    DebugLog.write();
                }
            });

        } else {
            //completions are completed
            mAppExecutor.mainThread().execute(() -> {
                DebugLog.write();
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable(KEY_COMMON_DTO, dto);
                message.what = MainActivity.MSG_UPLOAD_COMPLETION_READY;
                message.setData(bundle);
                callback.handleMessage(message);
            });
        }

    }

    public void uploadCompletionMixSequenceWithFile(int completionCount, File[] files, CommonDto dto) {
        if (completionCount > 0) {
            int tokenListIndex = dto.getUsersToken().size() - 1;
            int userIndex = mRandom.nextInt(tokenListIndex);
            String compOwnerToken = dto.getUsersToken().get(userIndex);
            //prepare jangle model
            UploadVM uploadModel = new UploadVM(mRandomWordGenerator.getWord(), DocType.COMPLETION, dto.getJangleUuid(),
                    dto.getUploadCompletionCounter());
            RequestBody modelBody = RequestBody.create(parse("application/json"), mGson.toJson(uploadModel));
            MultipartBody.Part fileImage = MultipartBody.Part
                    .createFormData("file", dto.getUploadCompletionCounter() + "_bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
            MultipartBody.Part imageUrl = MultipartBody.Part
                    .createFormData("preview", dto.getUploadCompletionCounter() + "_bg.jpg", RequestBody.create(parse("multipart/form-data"),
                            files[0]));
            MultipartBody.Part fileThumbnail = MultipartBody.Part
                    .createFormData("thumbnail", dto.getUploadCompletionCounter() + "_sml.jpg", RequestBody.create(parse("multipart/form-data"),
                            files[1]));
            mAppExecutor.networkIO().execute(() -> {
                DebugLog.write("UploadCompletionWithFile networkIO " + Thread.currentThread().getName());
                // DebugLog.write("CompOwnerToken=" + compOwnerToken);
                Map<String, String> authMapComp = new HashMap<>();
                authMapComp.put(Constants.AUTHORIZATION, Constants.BEARER + compOwnerToken);
                Call<UploadResponse> call2 = mService.uploadJangle(modelBody, fileImage, imageUrl, fileThumbnail,
                        authMapComp);
                try {
                    Response<UploadResponse> response = call2.execute();
                    if (response.code() == 200) {
                        DebugLog.write();
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        dto.setUploadCompletionCounter(dto.getUploadCompletionCounter() + 1);
                        dto.setCompletionCountOfTheJangle(dto.getCompletionCountOfTheJangle() - 1);
                        DebugLog.write("COMMON DTO= " + dto.toString());
                        bundle.putSerializable(KEY_COMMON_DTO, dto);
                        message.what = MainActivity.MSG_UPLOAD_COMPLETION_UPLOADING_CONTINUE;
                        message.setData(bundle);
                        callback.handleMessage(message);
                    }

                } catch (Exception e) {
                    DebugLog.write();
                }
            });

        } else {
            //completions are completed
            mAppExecutor.mainThread().execute(() -> {
                DebugLog.write();
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable(KEY_COMMON_DTO, dto);
                message.what = MainActivity.MSG_UPLOAD_COMPLETION_READY;
                message.setData(bundle);
                callback.handleMessage(message);
            });
        }

    }


    public void uploadJangleAndCompletions(ArrayList<File[]> fileList, ArrayList<String> tokenList, MessageSubject subject) {
        int counter = 0;
        //get random user to upload a jangle
        int tokenListIndex = tokenList.size() - 1;
        int userIndex = mRandom.nextInt(tokenListIndex);
        String jangleOwnerToken = tokenList.remove(userIndex);
        tokenList.trimToSize();
        //get jangle file
        File[] jangleFiles = fileList.remove(fileList.size() - 1);
        fileList.trimToSize();
        //prepare jangle model
        UploadVM model = new UploadVM(mRandomWordGenerator.getWord(), DocType.JANGLE);
        RequestBody modelBody = RequestBody.create(parse("application/json"), mGson.toJson(model));
        MultipartBody.Part fileImage = MultipartBody.Part
                .createFormData("file", "bg.jpg", RequestBody.create(parse("multipart/form-data"), jangleFiles[0]));
        MultipartBody.Part imageUrl = MultipartBody.Part
                .createFormData("preview", "bg.jpg", RequestBody.create(parse("multipart/form-data"), jangleFiles[0]));
        MultipartBody.Part fileThumbnail = MultipartBody.Part
                .createFormData("thumbnail", "sml.jpg", RequestBody.create(parse("multipart/form-data"), jangleFiles[1]));
        Map<String, String> authMap = new HashMap<>();
        authMap.put(Constants.AUTHORIZATION, Constants.BEARER + jangleOwnerToken);
        Call<UploadResponse> callJangle = mService.uploadJangle(modelBody, fileImage, imageUrl, fileThumbnail, authMap);
        mAppExecutor.networkIO().execute(() -> {
            try {
                Response<UploadResponse> response = callJangle.execute();
                if (response.code() == 200) {
                    uploadCompletions(counter + 1, jangleOwnerToken, response.body().getUuid(), fileList, tokenList, subject);
                }
            } catch (Exception e) {
                DebugLog.write();
            }
        });
    }


    private void uploadCompletions(int counter, String jangleOwnerToken, String jangleUuid, ArrayList<File[]> fileList,
                                   ArrayList<String> compTokenList,
                                   MessageSubject subject) {

        if (fileList != null && fileList.size() > 0) {
            DebugLog.write("COMPLETION FILE COUNT= " + fileList.size());
            File[] files = fileList.remove(fileList.size() - 1);
            fileList.trimToSize();
            DebugLog.write("file name= " + files[1].getName());
            UploadVM uploadModel = new UploadVM(mRandomWordGenerator.getWord(), DocType.COMPLETION, jangleUuid, counter);
            RequestBody modelBody = RequestBody.create(parse("application/json"), mGson.toJson(uploadModel));
            MultipartBody.Part fileImage = MultipartBody.Part
                    .createFormData("file", counter + "_bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
            MultipartBody.Part imageUrl = MultipartBody.Part
                    .createFormData("preview", counter + "_bg.jpg", RequestBody.create(parse("multipart/form-data"), files[0]));
            MultipartBody.Part fileThumbnail = MultipartBody.Part
                    .createFormData("thumbnail", counter + "_sml.jpg", RequestBody.create(parse("multipart/form-data"), files[1]));
            int tokenListIndex = compTokenList.size() - 1;
            int userIndex = mRandom.nextInt(tokenListIndex);
            String compToken = compTokenList.get(userIndex);
            Map<String, String> authMapComp = new HashMap<>();
            authMapComp.put(Constants.AUTHORIZATION, Constants.BEARER + compToken);
            mAppExecutor.networkIO().execute(() -> {
                Call<UploadResponse> call2 = mService.uploadJangle(modelBody, fileImage, imageUrl, fileThumbnail,
                        authMapComp);
                try {
                    Response<UploadResponse> response = call2.execute();
                    if (response.code() == 200) {
                        uploadCompletions(counter + 1, jangleOwnerToken, jangleUuid, fileList, compTokenList, subject);
                    }
                } catch (Exception e) {
                    DebugLog.write();
                }
            });

        } else {//completions are completed

            DebugLog.write();
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(JANGLE_KEY, jangleUuid);
            bundle.putString(JANGLE_OWNER_KEY, jangleOwnerToken);
            bundle.putStringArrayList(USER_TOKEN_LIST_KEY, compTokenList);
            bundle.putSerializable(MESSAGE_SUBJECT_KEY, subject);
            message.what = MainActivity.MSG_JANGLE_AND_COMPLETIONS_FILES_READY;
            message.setData(bundle);
            callback.handleMessage(message);


        }
    }


}
