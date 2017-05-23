package com.github.jzyu.library.seed.ui.ptr;

import android.support.v7.widget.GridLayoutManager;

import com.github.jzyu.library.seed.util.GridItemSpace;
import com.github.jzyu.library.seed.util.SeedUtil;

import java.util.List;

/**
 * Author: jzyu
 * Date  : 2017/5/17
 */

public abstract class SeedPtrGridFragment<T, API_SET_CLASS> extends SeedPtrListFragment<T, API_SET_CLASS> {

    private int spanCount;

    protected ConfigBuilder<T> getBuilder(int rowLayoutId, List<T> items, int spanCount) {
        this.spanCount = spanCount;

        return getBuilder(rowLayoutId, items)
                .layoutManager(new GridLayoutManager(getActivity(), spanCount));
    }

    final protected void setSpanSpace(int spaceDp) {
        recyclerView.addItemDecoration(new GridItemSpace(
                SeedUtil.dp2px(getActivity(), spaceDp), spanCount));
    }
}
