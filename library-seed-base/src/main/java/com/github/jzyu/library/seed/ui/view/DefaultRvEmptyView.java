package com.github.jzyu.library.seed.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jzyu.library.seed.R;
import com.github.jzyu.library.seed.ui.ptr.IRvEmpty;
import com.github.jzyu.library.seed.util.SeedUtil;

/**
 * Author: jzyu
 * Date  : 2017/5/18
 */

public class DefaultRvEmptyView extends LinearLayout implements IRvEmpty {

    public static final String EMPTY_TIP_DEFAULT = "空空如也";

    private ViewGroup containerLoading;
    private ViewGroup containerError;
    private ViewGroup containerEmpty;
    private TextView tvError;
    private TextView tvEmpty;

    public DefaultRvEmptyView(Context context) {
        this(context, null);
    }

    public DefaultRvEmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultRvEmptyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        View view = LayoutInflater.from(context).inflate(R.layout.seed_view_rv_empty_default, this);

        containerLoading = SeedUtil.findById(view, R.id.container_loading);
        containerError = SeedUtil.findById(view, R.id.container_error);
        containerEmpty = SeedUtil.findById(view, R.id.container_empty);
        tvEmpty = SeedUtil.findById(view, R.id.tv_empty);
        tvError = SeedUtil.findById(view, R.id.tv_error);

        setStatusEmpty(EMPTY_TIP_DEFAULT);
    }

    private void show(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setStatus(Status status) {
        switch (status) {
            default:
            case EMPTY:
                show(containerEmpty, true);
                show(containerLoading, false);
                show(containerError, false);
                break;

            case LOADING:
                show(containerEmpty, false);
                show(containerLoading, true);
                show(containerError, false);
                break;

            case ERROR:
                show(containerEmpty, false);
                show(containerLoading, false);
                show(containerError, true);
                break;
        }
    }

    private enum Status {
        EMPTY,
        LOADING,
        ERROR
    }

    @Override
    public void setStatusEmpty(String emptyTip) {
        tvEmpty.setText(emptyTip);
        setStatus(Status.EMPTY);
    }

    @Override
    public void setStatusLoading() {
        setStatus(Status.LOADING);
    }

    @Override
    public void setStatusError(String errorMsg) {
        tvError.setText(SeedUtil.isEmpty(errorMsg) ? "咦，电波无法到达" : errorMsg);
        setStatus(Status.ERROR);
    }
}
