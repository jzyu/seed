package com.github.jzyu.library.seed.ui.ptr;

import java.util.List;

/**
 * Author: jzyu
 * Date  : 2017/6/2
 */

public interface CustomListLoaderListener<T> {
    void onLoadSuccess(List<T> newItems);
    void onLoadFailed(String errorMsg);
}
