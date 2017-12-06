package com.github.jzyu.library.seed.ui.ptr;

import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Author: jzyu
 * Date  : 2017/6/2
 */
interface IRowConvert<T> {
    void onRowConvert(ViewHolder holder, final T item, int position);
}
