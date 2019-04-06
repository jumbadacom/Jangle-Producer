package com.test.jangleproducer;

import java.util.Random;

public class RandomColorGenerator {

    private Random mRandom;

    public RandomColorGenerator() {
        mRandom = new Random();
    }

/*
    public String getRandomColor() {
        // create random object - reuse this as often as possible
        Random random = new Random();

        // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
        int nextInt = random.nextInt(0xffffff + 1);

        // format it as hexadecimal string (with hashtag and leading zeros)
        String colorCode = String.format("0xff%06x", nextInt);

        return colorCode;


    }*/


    public int getRgbColor() {
        // create random object - reuse this as often as possible
        return mRandom.nextInt(256);
    }

    public int getRandomColor() {
        return (int) ((0xff * Math.random()));
    }

    public int getOptNumber(int bound) {
        // create random object - reuse this as often as possible
        return mRandom.nextInt(bound);
    }

    public int getNo(int bound) {
        // create random object - reuse this as often as possible
        return mRandom.nextInt(bound);
    }




}
