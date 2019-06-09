package com.test.jangleproducer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.test.jangleproducer.activity.MainActivity;
import com.test.jangleproducer.activity.ScreenThreeActivity;
import com.test.jangleproducer.activity.ScreenTwoActivity;
import com.test.jangleproducer.model.CommonDto;

import java.util.ArrayList;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

import static com.test.jangleproducer.activity.MainActivity.KEY_BIG_BITMAP;
import static com.test.jangleproducer.activity.MainActivity.KEY_COMMON_DTO;
import static com.test.jangleproducer.activity.MainActivity.KEY_DOC_TYPE;
import static com.test.jangleproducer.activity.MainActivity.KEY_SMALL_BITMAP;

public class AssetBitmapGenerator {

    private static final String IMAGE_PREFIX = "images/000";
    private static final String AVATAR_PREFIX = "avatar1/000";
    private static final String BG_PREFIX = "bg/000";
    private static final String BG2_PREFIX = "bg2/000";
    private static final String HIDDEN_PREFIX = "hidden/000";

    private static final int AVATAR_COUNT = 199;
    private static final int BG_COUNT = 359;
    private static final int BG_COUNT2 = 164;
    private static final int DEF_IMAGE_COUNT = 990;


    private LoadBitmap mLoadBitmap;
    private Random mRandom;
    private Handler.Callback mCallback;
    private AppExecutors mAppExecutors;


    public AssetBitmapGenerator(AppCompatActivity activity, AppExecutors appExecutors) {
        this.mAppExecutors = appExecutors;
        this.mLoadBitmap = new LoadBitmap(activity);
        this.mRandom = new Random();
        if (activity instanceof MainActivity) {
            this.mCallback = (MainActivity) activity;
        } else if (activity instanceof ScreenTwoActivity) {
            this.mCallback = (ScreenTwoActivity) activity;
        } else if (activity instanceof ScreenThreeActivity) {
            this.mCallback = (ScreenThreeActivity) activity;
        } else {
            throw new IllegalArgumentException("Wrong Upload Activity");
        }
    }

    public void getScaledJangleBitmapMsg(@BitmapImageType int type) {
        mAppExecutors.diskIO().execute(() -> {
            Bitmap loadBitmap = getRandomFileWithName(type);
            Bitmap scaledBitmapBig = Bitmap.createScaledBitmap(loadBitmap, 720, 720, false);
            Bitmap scaledBitmapSmall = Bitmap.createScaledBitmap(loadBitmap, 144, 144, false);
            loadBitmap.recycle();
            mAppExecutors.mainThread().execute(() -> {
                DebugLog.write();
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_BIG_BITMAP, scaledBitmapBig);
                bundle.putParcelable(KEY_SMALL_BITMAP, scaledBitmapSmall);
                message.what = MainActivity.MSG_BITMAP_JANGLE_IMAGE_READY;
                message.setData(bundle);
                mCallback.handleMessage(message);
            });
        });
    }




    public void getScaledCompletionBitmapMsg(@BitmapImageType int type, CommonDto dto) {
        mAppExecutors.diskIO().execute(() -> {
            Bitmap loadBitmap = getRandomFileWithName(type);
            Bitmap scaledBitmapBig = Bitmap.createScaledBitmap(loadBitmap, 720, 720, false);
            Bitmap scaledBitmapSmall = Bitmap.createScaledBitmap(loadBitmap, 144, 144, false);
            loadBitmap.recycle();
            mAppExecutors.mainThread().execute(() -> {
                DebugLog.write();
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_BIG_BITMAP, scaledBitmapBig);
                bundle.putParcelable(KEY_SMALL_BITMAP, scaledBitmapSmall);
                bundle.putSerializable(KEY_COMMON_DTO, dto);
                message.what = MainActivity.MSG_BITMAP_COMPLETION_IMAGE_READY;
                message.setData(bundle);
                mCallback.handleMessage(message);
            });
        });
    }

    public Bitmap[] getScaledBitmap(@BitmapImageType int type) {
        Bitmap loadBitmap = getRandomFileWithName(type);
        Bitmap scaledBitmapBig = Bitmap.createScaledBitmap(loadBitmap, 720, 720, false);
        Bitmap scaledBitmapSmall = Bitmap.createScaledBitmap(loadBitmap, 144, 144, false);
        loadBitmap.recycle();
        return new Bitmap[]{scaledBitmapBig, scaledBitmapSmall};
    }

    public ArrayList<Bitmap[]> getScaledBitmapList(@BitmapImageType int type, int imageCount) {
        ArrayList<Bitmap[]> bitmapArrayList = new ArrayList();

        int i = 0;
        while (i < imageCount) {
            Bitmap loadBitmap = getRandomFileWithName(type);
            Bitmap scaledBitmapBig = Bitmap.createScaledBitmap(loadBitmap, 720, 720, false);
            Bitmap scaledBitmapSmall = Bitmap.createScaledBitmap(loadBitmap, 144, 144, false);
            loadBitmap.recycle();
            bitmapArrayList.add(new Bitmap[]{scaledBitmapBig, scaledBitmapSmall});
            i++;
        }
        return bitmapArrayList;
    }


    private Bitmap getRandomFileWithName(@BitmapImageType int type) {
        int randomNo;
        String fileName;
        int imageCountLimit;
        switch (type) {
            case BitmapImageType.AVATAR: {
                imageCountLimit = AVATAR_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = AVATAR_PREFIX + randomNo + ".jpg";
            }
            case BitmapImageType.BG: {
                imageCountLimit = BG_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = BG_PREFIX + randomNo + ".jpg";
            }
            case BitmapImageType.BG2: {
                imageCountLimit = BG_COUNT2;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = BG2_PREFIX + randomNo + ".jpg";
            }
            default: {
                imageCountLimit = DEF_IMAGE_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = IMAGE_PREFIX + randomNo + ".jpg";
            }
        }
        DebugLog.write(fileName);
        return mLoadBitmap.getImageFromAssetsFile(fileName);
    }


}
