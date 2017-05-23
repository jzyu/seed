package com.github.jzyu.library.seed.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.jzyu.library.seed.http.HttpUtil;
import com.github.jzyu.library.seed.http.RetroHelper;

/**
 * Author: jzyu
 * Date  : 2017/5/16
 */

public class SeedBaseFragment<API_SET_CLASS> extends Fragment {

    public HttpUtil http;
    private RetroHelper<API_SET_CLASS> retroHelper;

    // -- 初始化http --
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initHttp(context);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initHttp(getContext());
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initHttp(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (http != null && retroHelper != null) {
            retroHelper.cancelRequests(http.getRequests());
        }
    }

    private void initHttp(Context context) {
        if (context instanceof SeedBaseActivity && http == null) {
            http = ((SeedBaseActivity) context).http;
            retroHelper = ((SeedBaseActivity<API_SET_CLASS>) context).getRetroHelper();
        }
    }

    protected RetroHelper<API_SET_CLASS> getRetroHelper() {
        return retroHelper;
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        WeplantApplication.getRefWatcher(getActivity()).watch(this);
    }*/

    public SeedBaseActivity getAty() {
        return (SeedBaseActivity) getActivity();
    }

    final public void showView(int viewId, boolean isShow) {
        if (getView() != null) {
            showView(getView().findViewById(viewId), isShow);
        }
    }

    final public void showView(View view, boolean isShow) {
        view.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("unchecked")
    static protected <T extends View> T findView(View parentView, int viewId) {
        if (parentView != null) {
            return (T) parentView.findViewById(viewId);
        } else {
            return null;
        }
    }
}
