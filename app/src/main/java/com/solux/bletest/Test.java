package com.solux.bletest;

import android.support.annotation.NonNull;

import com.clj.fastble.utils.HexUtil;
import com.solux.bletest.utils.HexUtils;
import com.solux.bletest.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by qindl
 * on 2018/10/16
 */
public class Test {
    private static int write_index = 1;

    public static void main(String asd[]) {
        int[] ints = StringUtils.listToInts(0X10);
        System.out.println("list to 数组：" + Arrays.toString(ints));
        String hexString = StringUtils.intsToHexString(ints);
        System.out.println("16进制string：" + hexString);
        byte[] bytes = HexUtils.hexStringToBytes(hexString);
        for (int i = 0; i < bytes.length; i++) {
            System.out.println("16进制string to 数组：" + bytes[i]);
        }
        String string = Arrays.toString(bytes);
        System.out.println("16进制string：" + string);

        List<Integer> list = new ArrayList<>();
        list.add(0X98);
        list.add(0X66);
        list.add(0X10);
        list.add(0XAD);
        byte[] datas = new byte[list.size()];
        for(int i = 0 ; i < datas.length ; i++){
            datas[i] = list.get(i).byteValue();
        }
        System.out.println("bytes[]:：" + Arrays.toString(datas));
        System.out.println("16进制string：" + HexUtils.encodeHexStr(datas));
        System.out.println("--------------------------------------------");


        byte[] bytes1 = HexUtils.hexStringToBytes("986610ad");
        List<Byte> list1 = new ArrayList<>(4);
        for(int i = 0 ; i < bytes1.length ; i++){
            list1.add(bytes1[i]);
        }
        System.out.println(Arrays.toString(bytes1));
        StringBuilder builder = new StringBuilder(4);
        builder.append(((Byte)bytes1[1]).intValue());
        System.out.println(((Byte)bytes1[1]).intValue());

        System.out.println("--------------------------------------------");
        int[] int1 = StringUtils.listToInts(0X10);
        System.out.println(Arrays.toString(int1));
        String[] stringArray = StringUtils.intArrayTo0xStringArray(int1);
        System.out.println(Arrays.toString(stringArray));
        StringBuilder stringBuilder1 = new StringBuilder();
//        for (int i = 0; i < stringArray.length; i++) {
//            if (stringArray[i].length() == 1) {
//                stringBuilder1.append("0X0" + stringArray[i]+"\t");
//            } else {
//                stringBuilder1.append("0X"+stringArray[i]+"\t");
//            }
//        }
        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i].length() == 1) {
                stringBuilder1.append("0" + stringArray[i]);
            } else {
                stringBuilder1.append(stringArray[i]);
            }
        }
        System.out.println(stringBuilder1.toString());
        byte[] bys = StringUtils.hexStringToBytes(stringBuilder1.toString());
        System.out.println(Arrays.toString(bys));

        String hexStr = HexUtils.encodeHexStr(bys);
        System.out.println(hexStr);
        System.out.println(get0XHexString(hexStr));

        System.out.println("--------------------stool------------------------");
        processdata();

    }

    @NonNull
    private static String get0XHexString(String hexStr) {
        StringBuilder mBuilder = new StringBuilder();
        for(int i = 0 ; i < hexStr.length() ; i=i+2){
            String s = hexStr.substring(i, i + 2);
            mBuilder.append("0X").append(s.toUpperCase()).append("\t");
        }
        return mBuilder.toString();
    }

    private static void processdata() {
        List<Integer> bleSendData = new ArrayList<>();
        bleSendData.add(0xA2);
        int[] data_int = editSendBleData(bleSendData);
        System.out.println(Arrays.toString(data_int));

        String[] data_string_encode = StringUtils.intArrayTo0xStringArray(data_int);
        System.out.println(Arrays.toString(data_string_encode));

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data_string_encode.length; i++) {
            if (data_string_encode[i].length() == 1) {
                stringBuilder.append("0" + data_string_encode[i]);
            } else {
                stringBuilder.append(data_string_encode[i]);
            }
        }
        System.out.println(stringBuilder.toString());
        byte[] bytes1 = StringUtils.hexStringToBytes(stringBuilder.toString());
        System.out.println(Arrays.toString(bytes1));
    }

    public static int[] editSendBleData(List<Integer> command) {
        List<Integer> data = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            data.add(0);
        }
        //前两位固定
        data.set(0, 0x51);
        data.set(1, 0x98);
        //第三、四字节：数据包不一样时序号自动累加1。编号在0000~ffff间循环累加。
        if (write_index > 0xffff) {
            write_index = 0;
        }
        write_index++;
        //高位
        int mHeight = write_index / 0xff;
        String mHeightStr = Integer.toHexString(mHeight);
        data.set(2, mHeight);
        //低位
        int mLow = write_index % 0xff;
        String mLowStr = Integer.toHexString(mLow);
        data.set(3, mLow);
        //第五字节开始：命令
        for (int i = 0; i < command.size(); i++) {
            data.set(4 + i, command.get(i));
        }
        //第十六位校验，将第三位加到第十五位，和除以255或ff求余
        int num = 0;
        for (int i = 2; i < 15; i++) {
            num += data.get(i);
        }
        data.set(15, num % 0xff);
        //把list转数组
        int[] intData = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            intData[i] = data.get(i);
        }
        return intData;
    }

}
