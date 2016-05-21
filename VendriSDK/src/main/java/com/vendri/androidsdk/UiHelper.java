package com.vendri.androidsdk;

import android.os.Handler;
import android.os.Looper;

public class UiHelper {
    public static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
