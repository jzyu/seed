package com.github.jzyu.library.seed.ui.base;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.jzyu.library.seed.http.HttpUtil;
import com.github.jzyu.library.seed.http.RetroHelper;

/**
 * Author: jzyu
 * Date  : 2017/5/16
 */

public class SeedBaseActivity<API_SET_CLASS> extends AppCompatActivity {

    public HttpUtil http;
    private RetroHelper<API_SET_CLASS> retroHelper;

    public SeedBaseActivity getAty() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (http != null && retroHelper != null) {
            retroHelper.cancelRequests(http.getRequests());
        }
    }

    public void setRetroHelper(RetroHelper<API_SET_CLASS> retroHelper) {
        this.retroHelper = retroHelper;
    }

    public RetroHelper<API_SET_CLASS> getRetroHelper() {
        return retroHelper;
    }

    public void postDelay(Runnable action, int waitMs) {
        getWindow().getDecorView().postDelayed(action, waitMs);
    }

    public void post(Runnable action) {
        getWindow().getDecorView().post(action);
    }

    final public void showView(int viewId, boolean isShow) {
        showView(findViewById(viewId), isShow);
    }

    final public void showView(View view, boolean isShow) {
        view.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }
}
