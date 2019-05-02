package com.test.jangleproducer.util;

import com.test.jangleproducer.DebugLog;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;

public class UserUtil {

    public static ArrayList<String> getUsernameList(String baseName, int min, int max) {
        ArrayList<String> names = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(baseName).append(i);
            names.add(sb.toString());
        }
        return names;
    }

    public static ArrayList<String> getRandomTokensFromList(int count, @NonNull final ArrayList<String> tokenList) {
        if (tokenList == null && tokenList.size() < count) {
            throw new IllegalArgumentException("token list count should be bigger than count");
        }
        Random random = new Random();
        ArrayList<String> returnList = new ArrayList<>();
        ArrayList<String> tempList = new ArrayList<>(tokenList);
        while (count > 0) {
            count--;
            int ind = tempList.size() - 1;
            String token = tempList.remove(random.nextInt(ind));
            returnList.add(token);
        }
        DebugLog.write("TempList size= "+tempList.size());
        DebugLog.write(returnList.size());
        return returnList;
    }
}
