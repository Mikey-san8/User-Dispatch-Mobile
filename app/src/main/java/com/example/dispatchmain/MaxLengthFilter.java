package com.example.dispatchmain;

import android.text.InputFilter;
import android.text.Spanned;

public class MaxLengthFilter implements InputFilter {
    private int maxLength;

    public MaxLengthFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int keep = maxLength - (dest.length() - (dend - dstart));
        if (keep <= 0) {
            return "";
        } else if (keep >= end - start) {
            return null; // Accept the original input
        } else {
            return source.subSequence(start, start + keep); // Trim the input to the desired length
        }
    }
}

