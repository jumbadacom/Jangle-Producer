package com.test.jangleproducer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileConverter {

    private AppExecutors mAppExecutors;
    private Context mContext;

    public FileConverter(AppExecutors appExecutors, Context context) {
        this.mAppExecutors = appExecutors;
        this.mContext = context;
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
