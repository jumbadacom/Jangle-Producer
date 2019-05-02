package com.test.jangleproducer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.test.jangleproducer.activity.MainActivity;
import com.test.jangleproducer.activity.ScreenTwoActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import static com.test.jangleproducer.activity.MainActivity.KEY_BIG_BITMAP;
import static com.test.jangleproducer.activity.MainActivity.KEY_BIG_FILE;
import static com.test.jangleproducer.activity.MainActivity.KEY_DOC_TYPE;
import static com.test.jangleproducer.activity.MainActivity.KEY_SMALL_BITMAP;
import static com.test.jangleproducer.activity.MainActivity.KEY_SMALL_FILE;
import static com.test.jangleproducer.activity.MainActivity.MSG_BITMAP_IMAGE_READY;
import static com.test.jangleproducer.activity.MainActivity.USER_TOKEN_LIST_KEY;

public class FileConverter  {

    private AppExecutors mAppExecutors;
    private Context mContext;
    private Handler.Callback mCallback;

    public FileConverter(AppExecutors appExecutors, AppCompatActivity activity) {
        this.mAppExecutors = appExecutors;
        this.mContext = activity;
        if (activity instanceof MainActivity) {
            this.mCallback = (MainActivity) activity;
        } else if (activity instanceof ScreenTwoActivity) {
            this.mCallback = (ScreenTwoActivity) activity;
        } else {
            throw new IllegalArgumentException("Wrong Upload Activity");
        }

    }

    public void convertBitmapToFilesMsg(final Bitmap bitmapBig, final Bitmap bitmapSmall,@DocType int docType,ArrayList<String> userList) {
        mAppExecutors.diskIO().execute(() -> {
            clearCache();
            File[] files = new File[2];
            File fBig = new File(mContext.getCacheDir(), "big.jpg");
            File fSmall = new File(mContext.getCacheDir(), "small.jpg");
            Bitmap bBig = bitmapBig;
            Bitmap bSmall = bitmapSmall;
            ByteArrayOutputStream bosBig = new ByteArrayOutputStream();
            ByteArrayOutputStream bosSmall = new ByteArrayOutputStream();
            bBig.compress(CompressFormat.JPEG, 100, bosBig);
            bSmall.compress(CompressFormat.JPEG, 100, bosSmall);
            byte[] bitmapDataBig = bosBig.toByteArray();
            byte[] bitmapDataSmall = bosSmall.toByteArray();
            try {
                FileOutputStream fosBig = new FileOutputStream(fBig);
                FileOutputStream fosSmall = new FileOutputStream(fSmall);
                fosBig.write(bitmapDataBig);
                files[0] = fBig;
                fosBig.flush();
                fosBig.close();
                fosSmall.write(bitmapDataSmall);
                files[1] = fSmall;
                fosSmall.flush();
                fosSmall.close();
                mAppExecutors.mainThread().execute(() -> {
                    DebugLog.write();
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(KEY_BIG_FILE, files[0]);
                    bundle.putSerializable(KEY_SMALL_FILE, files[1]);
                    bundle.putStringArrayList(USER_TOKEN_LIST_KEY,userList);
                    bundle.putInt(KEY_DOC_TYPE,docType);
                    message.what = MainActivity.MSG_BITMAP_FILE_READY;
                    message.setData(bundle);
                    mCallback.handleMessage(message);
                });

            } catch (Exception e) {
                DebugLog.write(e.getMessage());
            }

        });


    }


    public File[] convertBitmapToFiles(final Bitmap bitmapBig, final Bitmap bitmapSmall) {
        clearCache();
        File[] files = new File[2];
        File fBig = new File(mContext.getCacheDir(), "big.jpg");
        File fSmall = new File(mContext.getCacheDir(), "small.jpg");

        Bitmap bBig = bitmapBig;
        Bitmap bSmall = bitmapSmall;
        ByteArrayOutputStream bosBig = new ByteArrayOutputStream();
        ByteArrayOutputStream bosSmall = new ByteArrayOutputStream();
        bBig.compress(CompressFormat.JPEG, 100, bosBig);
        bSmall.compress(CompressFormat.JPEG, 100, bosSmall);
        byte[] bitmapDataBig = bosBig.toByteArray();
        byte[] bitmapDataSmall = bosSmall.toByteArray();
        try {
            FileOutputStream fosBig = new FileOutputStream(fBig);
            FileOutputStream fosSmall = new FileOutputStream(fSmall);
            fosBig.write(bitmapDataBig);
            files[0] = fBig;
            fosBig.flush();
            fosBig.close();
            fosSmall.write(bitmapDataSmall);
            files[1] = fSmall;
            fosSmall.flush();
            fosSmall.close();
        } catch (Exception e) {
            DebugLog.write(e.getMessage());
        }


        return files;

    }

    public ArrayList<File[]> convertBitmapListToFileList(final ArrayList<Bitmap[]> bitmapList) {
        clearCache();
        int bitmapCount = bitmapList.size();
        ArrayList<File[]> fileList = new ArrayList<>(bitmapCount);
        int counter = 0;
        for (Bitmap[] bitmapArray : bitmapList) {
            File[] files = new File[2];
            File fBig = new File(mContext.getCacheDir(), counter + "_big.jpg");
            File fSmall = new File(mContext.getCacheDir(), counter + "_small.jpg");
            Bitmap bBig = bitmapArray[0];
            Bitmap bSmall = bitmapArray[1];
            ByteArrayOutputStream bosBig = new ByteArrayOutputStream();
            ByteArrayOutputStream bosSmall = new ByteArrayOutputStream();
            bBig.compress(CompressFormat.JPEG, 100, bosBig);
            bSmall.compress(CompressFormat.JPEG, 100, bosSmall);
            byte[] bitmapDataBig = bosBig.toByteArray();
            byte[] bitmapDataSmall = bosSmall.toByteArray();
            try {
                FileOutputStream fosBig = new FileOutputStream(fBig);
                FileOutputStream fosSmall = new FileOutputStream(fSmall);
                fosBig.write(bitmapDataBig);
                files[0] = fBig;
                fosBig.flush();
                fosBig.close();
                fosSmall.write(bitmapDataSmall);
                files[1] = fSmall;
                fosSmall.flush();
                fosSmall.close();
            } catch (Exception e) {
                DebugLog.write(e.getMessage());
            }
            fileList.add(files);
            counter++;
        }
        return fileList;

    }

    private void clearCache() {
        File cacheDir = mContext.getCacheDir();

        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files)
                file.delete();
        }
    }


}
