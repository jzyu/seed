package com.github.jzyu.library.seed.util;

import android.view.View;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */

public interface ILoadingView {
    void attach(View anchorView, View.OnClickListener retryListener);
    void detach();
    boolean isAttached();

    void setStatusAsLoading();
    void setStatusAsRetry(String errorMsg);
    void setStatusAsRetry();
}
