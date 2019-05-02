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
    private static final String BG2_PREFIX = "bg2/000";
    private static final String HIDDEN_PREFIX = "hidden/000";

    private static final int AVATAR_COUNT=199;
    private static final int BG_COUNT=359;
    private static final int BG_COUNT2=164;
    private static final int DEF_IMAGE_COUNT=990;

    private Context mContext;
    private LoadBitmap mLoadBitmap;
    private Random mRandom;


    public AssetBitmapGenerator(Context context) {
        this.mContext = context;
        this.mLoadBitmap = new LoadBitmap(context);
        this.mRandom = new Random();
    }


    public Bitmap[] getScaledBitmap(@BitmapImageType int type) {
        int randomNo;
        String fileName;
        int imageCountLimit;
        switch (type){
            case BitmapImageType.AVATAR:{
                imageCountLimit=AVATAR_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = AVATAR_PREFIX + randomNo + ".jpg";
            }
            case BitmapImageType.BG:{
                imageCountLimit=BG_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = BG_PREFIX + randomNo + ".jpg";
            }
            case BitmapImageType.BG2:{
                imageCountLimit=BG_COUNT2;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = BG2_PREFIX + randomNo + ".jpg";
            }
            default:
            {
                imageCountLimit=DEF_IMAGE_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = IMAGE_PREFIX + randomNo + ".jpg";
            }
        }


        Bitmap loadBitmap = mLoadBitmap.getImageFromAssetsFile(fileName);
        Bitmap scaledBitmapBig = Bitmap.createScaledBitmap(loadBitmap, 720, 720, false);
        Bitmap scaledBitmapSmall = Bitmap.createScaledBitmap(loadBitmap, 144, 144, false);
        loadBitmap.recycle();
        return new Bitmap[]{scaledBitmapBig, scaledBitmapSmall};
    }

    public ArrayList<Bitmap[]> getScaledBitmapList(@BitmapImageType int type,int imageCount) {
        ArrayList<Bitmap[]> bitmapArrayList = new ArrayList();
        int randomNo;
        String fileName;
        int imageCountLimit;
        switch (type){
            case BitmapImageType.AVATAR:{
                imageCountLimit=AVATAR_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = AVATAR_PREFIX + randomNo + ".jpg";
            }
            case BitmapImageType.BG:{
                imageCountLimit=BG_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = BG_PREFIX + randomNo + ".jpg";
            }
            case BitmapImageType.BG2:{
                imageCountLimit=BG_COUNT2;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = BG2_PREFIX + randomNo + ".jpg";
            }
            default:            {
                imageCountLimit=DEF_IMAGE_COUNT;
                randomNo = mRandom.nextInt(imageCountLimit) + 1;
                fileName = IMAGE_PREFIX + randomNo + ".jpg";
            }
        }
        int i = 0;
        while (i < imageCount) {
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
