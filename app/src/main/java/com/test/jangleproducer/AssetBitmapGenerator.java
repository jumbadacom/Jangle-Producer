package com.test.jangleproducer;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AssetBitmapGenerator {

    private static final String IMAGE_PREFIX = "images/000";
    private static final String AVATAR_PREFIX = "avatar1/000";
    private static final String BG_PREFIX = "bg/000";
    private static final String HIDEN_PREFIX = "hidden/000";

    private Context mContext;
    private LoadBitmap mLoadBitmap;
    private Random mRandom;


    public AssetBitmapGenerator(Context context) {
        this.mContext = context;
        this.mLoadBitmap = new LoadBitmap(context);
        this.mRandom = new Random();
    }


    public Bitmap[] getScaledBitmap() {
        int randomNo = mRandom.nextInt(1) + 1;
        String fileName = HIDEN_PREFIX + randomNo + ".jpg";
        Bitmap loadBitmap = mLoadBitmap.getImageFromAssetsFile(fileName);
        Bitmap scaledBitmapBig = Bitmap.createScaledBitmap(loadBitmap, 720, 720, false);
        Bitmap scaledBitmapSmall = Bitmap.createScaledBitmap(loadBitmap, 144, 144, false);
        loadBitmap.recycle();
        return new Bitmap[]{scaledBitmapBig, scaledBitmapSmall};
    }

    public ArrayList<Bitmap[]> getScaledBitmapList(int fileCount) {
        ArrayList<Bitmap[]> bitmapArrayList = new ArrayList();
        int i = 0;
        while (i < fileCount) {
            int randomNo = mRandom.nextInt(360) + 1;
            String fileName = BG_PREFIX + randomNo + ".jpg";
            Bitmap loadBitmap = mLoadBitmap.getImageFromAssetsFile(fileName);
            Bitmap scaledBitmapBig = Bitmap.createScaledBitmap(loadBitmap, 720, 720, false);
            Bitmap scaledBitmapSmall = Bitmap.createScaledBitmap(loadBitmap, 144, 144, false);
            loadBitmap.recycle();
            bitmapArrayList.add(new Bitmap[]{scaledBitmapBig, scaledBitmapSmall});
            i++;
        }
        return bitmapArrayList;
    }
}
