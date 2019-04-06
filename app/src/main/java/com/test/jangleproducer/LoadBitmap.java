package com.test.jangleproducer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class LoadBitmap {

    private AssetManager mAssetManager;

    public LoadBitmap(Context context) {
        this.mAssetManager = context.getResources().getAssets();
    }


    public Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        try {
            InputStream is = mAssetManager.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


}
