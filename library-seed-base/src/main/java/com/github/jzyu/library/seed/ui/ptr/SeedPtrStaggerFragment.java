package com.github.jzyu.library.seed.ui.ptr;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */

import android.support.v7.widget.StaggeredGridLayoutManager;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

public abstract class SeedPtrStaggerFragment<T, API_SET_CLASS>
        extends SeedPtrRecyclerViewFragment<T, API_SET_CLASS>
        implements SeedPtrRecyclerViewFragment.RowConvert<T> {

    protected ConfigBuilder<T> getBuilder(int rowLayoutId, List<T> items) {
        MultiItemTypeAdapter<T> adapter = new CommonAdapter<T>(getContext(), rowLayoutId, items) {
            @Override
            protected void convert(ViewHolder holder, T item, int position) {
                onRowConvert(holder, item, position);
            }
        };

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        return new ConfigBuilder<T>()
                .items(items)
                .adapter(adapter)
                .layoutManager(layoutManager);
    }
}

