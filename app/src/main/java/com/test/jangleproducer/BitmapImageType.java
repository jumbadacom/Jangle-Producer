package com.test.jangleproducer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.test.jangleproducer.BitmapImageType.AVATAR;
import static com.test.jangleproducer.BitmapImageType.BG;
import static com.test.jangleproducer.BitmapImageType.BG2;
import static com.test.jangleproducer.BitmapImageType.DEF_JANGLE_IMAGE;

@Retention(RetentionPolicy.SOURCE)
@IntDef({AVATAR,DEF_JANGLE_IMAGE,BG,BG2})
public @interface BitmapImageType {

    int DEF_JANGLE_IMAGE = 0;
    int AVATAR = 1;
    int BG = 2;
    int BG2 = 3;
}
