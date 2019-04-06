package com.test.jangleproducer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;

public class RandomBitmapGenerator {


    private Context mContext;
    private RandomColorGenerator mRandomColorGenerator;
    private RandomWordGenerator mRandomWordGenerator;

    public RandomBitmapGenerator(Context context, RandomColorGenerator mRandomColorGenerator, RandomWordGenerator randomWordGenerator) {

        this.mContext = context;
        this.mRandomColorGenerator = mRandomColorGenerator;
        this.mRandomWordGenerator = randomWordGenerator;
    }


    public Bitmap[] genColoredBitmap() {

        Bitmap[] bitmaps = new Bitmap[2];
        // Here you create the bound of your shape
        Rect rectSmall = new Rect(0, 0, 120, 120);
        Rect rectBig = new Rect(0, 0, 720, 720);

        // You then create a Bitmap and get a canvas to draw into it
        Bitmap imageSmall = Bitmap.createBitmap(rectSmall.width(), rectSmall.height(), Config.ARGB_8888);
        Bitmap imageBig = Bitmap.createBitmap(rectBig.width(), rectBig.height(), Config.ARGB_8888);
        Canvas canvasSmall = new Canvas(imageSmall);
        Canvas canvasBig = new Canvas(imageBig);

        //You can get an int value representing an argb color by doing so. Put 1 as alpha by default
        int color = Color
            .rgb(mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor());

        //Paint holds information about how to draw shapes
        Paint paint = new Paint();
        paint.setColor(color);

        // Then draw your shape
        canvasSmall.drawRect(rectSmall, paint);
        canvasBig.drawRect(rectBig, paint);

        bitmaps[0] = imageBig;
        bitmaps[1] = imageSmall;
        return bitmaps;
    }

    public Bitmap[] genResourcedBitmap() {

        Resources resources = mContext.getResources();

        Bitmap[] bitmaps = new Bitmap[2];
        // You then create a Bitmap and get a canvas to draw into it
        Bitmap imageBig = BitmapFactory.decodeResource(resources, R.mipmap.big);
        Bitmap imageSmall = BitmapFactory.decodeResource(resources, R.mipmap.small);
        // Here you create the bound of your shape
        int heightBig = imageBig.getHeight();
        int widthBig = imageBig.getWidth();
        int heightSmall = imageSmall.getHeight();
        int widthSmall = imageSmall.getWidth();
        Rect rectBigUp = new Rect(0, 0, widthBig, heightBig / 2);
        Rect rectSmallUp = new Rect(0, 0, widthSmall, heightSmall / 2);
        Bitmap mutableBigBitmap = imageBig.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap mutableSmallBitmap = imageSmall.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvasBig = new Canvas(mutableBigBitmap);
        Canvas canvasSmall = new Canvas(mutableSmallBitmap);
        //You can get an int value representing an argb color by doing so. Put 1 as alpha by default
        int color1 = Color
            .rgb(mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRgbColor(), mRandomColorGenerator.getRandomColor());

        //Paint holds information about how to draw shapes
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color1, Mode.SRC_ATOP));

        // Then draw your shape
        canvasBig.drawRect(rectBigUp, paint);
        canvasSmall.drawRect(rectSmallUp, paint);

        //bottom
        Rect rectBigDown = new Rect(0, heightBig / 2, widthBig, heightBig);
        Rect rectSmallDown = new Rect(0, heightSmall / 2, widthSmall, heightSmall);
        int color2 = Color
            .rgb(mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRgbColor());
        paint.setColorFilter(new PorterDuffColorFilter(color2, PorterDuff.Mode.SRC_ATOP));
        canvasBig.drawRect(rectBigDown, paint);
        canvasSmall.drawRect(rectSmallDown, paint);

        int randomNumber = mRandomColorGenerator.getOptNumber(16);
        DebugLog.write("option=" + randomNumber);

        if (randomNumber <= 1) {
            RectF rectBigCenter = new RectF((widthBig / 4), (heightBig / 4), (widthBig / 4 * 3), (heightBig / 4 * 3));
            RectF rectSmallCenter = new RectF((widthSmall / 4), (heightSmall / 4), (widthSmall / 4 * 3), (heightSmall / 4 * 3));
            int color3 = Color
                .rgb(mRandomColorGenerator.getRgbColor(), mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor());
            paint.setColorFilter(new PorterDuffColorFilter(color3, Mode.SRC_ATOP));
            int rnd = mRandomColorGenerator.getRgbColor();
            canvasBig.drawArc(rectBigCenter, 360, rnd, false, paint);
            canvasSmall.drawArc(rectSmallCenter, 360, rnd, false, paint);
        } else if (randomNumber <= 4) {
            RectF rectBigCenter = new RectF((widthBig / 4), (heightBig / 4), (widthBig / 4 * 3), (heightBig / 4 * 3));
            RectF rectSmallCenter = new RectF((widthSmall / 4), (heightSmall / 4), (widthSmall / 4 * 3), (heightSmall / 4 * 3));
            int color3 = Color
                .rgb(mRandomColorGenerator.getRgbColor(), mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor());
            paint.setColorFilter(new PorterDuffColorFilter(color3, Mode.SRC_ATOP));
            canvasBig.drawRect(rectBigCenter, paint);
            canvasSmall.drawRect(rectSmallCenter, paint);
        } else if (randomNumber <= 7) {
            RectF rectBigCenter = new RectF((widthBig / 3), (heightBig / 4), (widthBig / 4 * 3), (heightBig / 3 * 2));
            RectF rectSmallCenter = new RectF((widthSmall / 3), (heightSmall / 4), (widthSmall / 4 * 3), (heightSmall / 3 * 2));
            int color3 = Color
                .rgb(mRandomColorGenerator.getRgbColor(), mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor());
            paint.setColorFilter(new PorterDuffColorFilter(color3, Mode.SRC_ATOP));
            canvasBig.drawRect(rectBigCenter, paint);
            canvasSmall.drawRect(rectSmallCenter, paint);

        } else if (randomNumber <= 9) {
            int color3 = Color.rgb(7, 7, 7);
            paint.setColorFilter(new PorterDuffColorFilter(color3, Mode.SRC_ATOP));
            String text = mRandomWordGenerator.getWord();
            paint.setTextSize(widthBig / 12);
            canvasBig.drawText(text, (widthBig / 3), (heightBig / 2), paint);
            paint.setTextSize(widthSmall / 12);
            canvasSmall.drawText(text, (widthSmall / 3), (heightSmall / 2), paint);

        } else if (randomNumber <= 11) {
            int color3 = Color
                .rgb(mRandomColorGenerator.getRgbColor(), mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor());
            paint.setColorFilter(new PorterDuffColorFilter(color3, Mode.SRC_ATOP));
            Point point1_drawBig = new Point(widthBig/2, 0);
            Point point2_drawBig = new Point(0, (widthBig / 4 * 2));
            Point point3_drawBig = new Point((widthBig / 4 * 2), (widthBig / 4 * 2));
            Path pathBig = new Path();
            pathBig.moveTo(point1_drawBig.x, point1_drawBig.y);
            pathBig.lineTo(point2_drawBig.x, point2_drawBig.y);
            pathBig.lineTo(point3_drawBig.x, point3_drawBig.y);
            pathBig.lineTo(point1_drawBig.x, point1_drawBig.y);
            pathBig.close();
            canvasBig.drawPath(pathBig, paint);
            Point point1_drawSmall = new Point(widthSmall/2, 0);
            Point point2_drawSmall = new Point(0, (widthSmall / 4 * 2));
            Point point3_drawSmall = new Point((widthSmall / 4 * 2), (widthSmall / 4 * 2));
            Path pathSmall = new Path();
            pathSmall.moveTo(point1_drawSmall.x, point1_drawSmall.y);
            pathSmall.lineTo(point2_drawSmall.x, point2_drawSmall.y);
            pathSmall.lineTo(point3_drawSmall.x, point3_drawSmall.y);
            pathSmall.lineTo(point1_drawSmall.x, point1_drawSmall.y);
            pathSmall.close();
            canvasSmall.drawPath(pathSmall, paint);

        } else {
            //center
            RectF rectBigCenter = new RectF((widthBig / 4), (heightBig / 4), (widthBig / 4 * 3), (heightBig / 4 * 3));
            RectF rectSmallCenter = new RectF((widthSmall / 4), (heightSmall / 4), (widthSmall / 4 * 3), (heightSmall / 4 * 3));
            int color3 = Color
                .rgb(mRandomColorGenerator.getRgbColor(), mRandomColorGenerator.getRandomColor(), mRandomColorGenerator.getRandomColor());
            paint.setColorFilter(new PorterDuffColorFilter(color3, Mode.SRC_ATOP));
            canvasBig.drawOval(rectBigCenter, paint);
            canvasSmall.drawOval(rectSmallCenter, paint);
        }

        bitmaps[0] = mutableBigBitmap;
        bitmaps[1] = mutableSmallBitmap;
        return bitmaps;
    }
}
