package com.shannon.openvoice.util;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * @Package: com.shannon.openvoice.util
 * @ClassName: EllipsisFilter
 * @Description: 作用描述
 * @Author: czhen
 * @CreateDate: 2022/8/8 15:45
 */
public class EllipsisFilter extends InputFilter.LengthFilter {

    public EllipsisFilter(int max) {
        super(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence cc = super.filter(source, start, end, dest, dstart, dend);
        if (cc == null) {
            return null;
        } else {
            return cc + "...";
        }

    }
}
