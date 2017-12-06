package com.github.jzyu.library.seed.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.Locale;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Author: jzyu
 * Date  : 2017/6/21
 * Note  : 修复 PtrFrameLayout 嵌套 HSView 后横向滑动冲突问题；
 *         HSView 包括：ViewPage, HorizontalScrollView, HorizontalRecyclerView
 * Refer : https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh/issues/282
 */

public class SeedPtrFrameLayout extends PtrFrameLayout {
    public static final String TAG = SeedPtrFrameLayout.class.getSimpleName();

    public SeedPtrFrameLayout(Context context) {
        super(context);
    }

    public SeedPtrFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeedPtrFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private float startY;
    private float startX;

    private boolean enableHorizontalMove;
    private boolean isHorizontalMove;
    private boolean isDeal;

    private int sysTouchSlop;

    @Override
    public void disableWhenHorizontalMove(boolean disable) {
        super.disableWhenHorizontalMove(disable);

        enableHorizontalMove = disable;
        sysTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        Log.v(TAG, "sysPagingTouchSlop = " + sysTouchSlop);
    }


    /**
     * 根据手指 X Y 方向滑动距离，判断是否横向滑动，再相应分流给手指下方 View 或 PtrFrameLayout
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (! enableHorizontalMove) {
            return super.dispatchTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的位置
                startY = ev.getY();
                startX = ev.getX();
                // 初始化标记
                isHorizontalMove = false;
                isDeal = false;
                break;

            case MotionEvent.ACTION_MOVE:
                // 已判断出是否横向滑动
                if (isDeal)
                    break;

                float offsetX = Math.abs(ev.getX() - startX);
                float offsetY = Math.abs(ev.getY() - startY);
                Log.v(TAG, String.format(Locale.getDefault(), "offset = (%f, %f)", offsetX, offsetY));

                // 默认横向滚动，否则手指滑出 HSView 外面时滑动无效
                isHorizontalMove = true;

                // 判断是否横向滑动
                if (offsetX > sysTouchSlop || offsetY > sysTouchSlop) {
                    isHorizontalMove = offsetX > offsetY;
                    isDeal = true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 下拉刷新状态时如果滚动了右滑控件 此时mIsHorizontalMove为true 会导致PtrFrameLayout无法恢复原位
                // 初始化标记,
                isHorizontalMove = false;
                isDeal = false;
                break;
        }

        if (isHorizontalMove) {
            // 横向滑动，ACTION_MOVE 消息交给手指下方 View 执行默认处理：产生横向滑动效果
            return dispatchTouchEventSupper(ev);
        } else {
            // 竖向滑动，全部消息交给 PtrFrameLayout 处理：产生下拉效果
            return super.dispatchTouchEvent(ev);
        }
    }
}
