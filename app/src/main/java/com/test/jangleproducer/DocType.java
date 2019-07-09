package com.test.jangleproducer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.test.jangleproducer.DocType.COMPLETION;
import static com.test.jangleproducer.DocType.JANGLE;

@Retention(RetentionPolicy.SOURCE)
@IntDef({JANGLE,COMPLETION})
public @interface DocType {

    int JANGLE = 0;
    int COMPLETION = 1;

}
