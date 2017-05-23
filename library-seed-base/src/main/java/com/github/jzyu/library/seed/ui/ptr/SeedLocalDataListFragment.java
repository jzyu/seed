package com.github.jzyu.library.seed.ui.ptr;

import java.util.List;

import retrofit2.Response;

/**
 * Author: jzyu
 * Date  : 2017/5/18
 */

public abstract class SeedLocalDataListFragment<T> extends SeedPtrListFragment<T, Void> {

    final protected void onLoadData(Void service, final boolean isRefresh) {
        // do nothing
    }

    final protected List<T> onProvideItemsInResponse(Response response) {
        // do nothing
        return null;
    }

    protected ConfigBuilder<T> getBuilder(int rowLayoutId, List<T> items) {
        return super.getBuilder(rowLayoutId, items).firstLoadFromLocal();
    }
}