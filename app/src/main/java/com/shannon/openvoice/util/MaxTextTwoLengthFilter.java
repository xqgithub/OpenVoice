package com.shannon.openvoice.util;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;

/**
 * Date:2022/6/28
 * Time:10:51
 * author:dimple
 * 数字英文算一位，中文算两位
 */
public class MaxTextTwoLengthFilter implements InputFilter {

    private int mMaxLength;

    public MaxTextTwoLengthFilter(Context context, int maxLen) {
        mMaxLength = maxLen;
//        toast = Toast.makeText(context, "限" + maxLen / 2 + "个汉字或" + maxLen + "个数字英文！)", Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // 判断是否到达最大长度
        int count = 0;
        // 之前就存在的内容
        int dindex = 0;
        while (count <= mMaxLength && dindex < dest.length()) {
            char c = dest.charAt(dindex++);
            if (c < 128) {
                count = count + 1;
            } else {
                count = count + 2;
            }
        }
        if (count > mMaxLength) {
            return dest.subSequence(0, dindex - 1);
        }

        // 从编辑框刚刚输入进去的内容
        int sindex = 0;
        while (count <= mMaxLength && sindex < source.length()) {
            char c = source.charAt(sindex++);
            if (c < 128) {
                count = count + 1;
            } else {
                count = count + 2;
            }
        }
        if (count > mMaxLength) {
            sindex--;
        }
        return source.subSequence(0, sindex);
    }
}
