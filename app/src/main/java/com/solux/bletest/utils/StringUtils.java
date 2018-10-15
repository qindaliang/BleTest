package com.solux.bletest.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by qindl
 * on 2018/9/17
 */
public class StringUtils {
    /**
     * 将字符串编程转16进制
     */
    public static int[] stringTo0xIntArray(String data) {
        char[] dataArray = data.toCharArray();
        int[] dataInt = new int[dataArray.length / 2];
        for (int i = 0; i < dataArray.length; i += 2) {
            dataInt[i / 2] = Integer.parseInt("" + dataArray[i] + dataArray[i + 1], 16);
        }
        return dataInt;
    }

    /**
     * 将int转16进制String
     */
    public static String[] intArrayTo0xStringArray(int[] data) {
        String[] decodeResultString = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            decodeResultString[i] = Integer.toHexString(data[i]);
        }
        return decodeResultString;
    }

    public int[] listToInts(int a) {
        List<Integer> list = new ArrayList<>();
        list.add(0X98);
        list.add(0X66);
        list.add(a);
        list.add(0XAD);
        int[] ints = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ints[i] = list.get(i);
        }
        return ints;
    }

    public String intsToHexString(int[] ints) {
        StringBuilder builder = new StringBuilder(ints.length);
        for (int i = 0; i < ints.length; i++) {
            builder.append(i);
        }
        return builder.toString();
    }
}
