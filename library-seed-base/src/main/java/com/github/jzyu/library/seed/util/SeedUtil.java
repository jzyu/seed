package com.github.jzyu.library.seed.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import java.util.Collection;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */

public class SeedUtil {

    private SeedUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.toString().equals("");
    }

    public static int dp2px(Context context, float dpVal) {
        return Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dpVal, context.getResources().getDisplayMetrics())
        );
    }

    public static boolean isEmpty(Collection<?> list) {
        return list == null || list.size() == 0;
    }

    public static boolean isConnected(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != cm) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isActivityDestroyed(Activity aty) {
        if (aty.isFinishing())
            return true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (aty.isDestroyed())
                return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }
}
