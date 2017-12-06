package com.github.jzyu.library.seed.ui.ptr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jzyu.library.seed.R;
import com.github.jzyu.library.seed.ui.base.SeedBaseFragment;
import com.github.jzyu.library.seed.util.ScrollSpeedLinearLayoutManger;
import com.github.jzyu.library.seed.util.SeedUtil;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

import cn.finalteam.loadingviewfinal.HeaderAndFooterRecyclerViewAdapter;
import cn.finalteam.loadingviewfinal.ILoadMoreView;
import cn.finalteam.loadingviewfinal.OnLoadMoreListener;
import cn.finalteam.loadingviewfinal.RecyclerViewFinal;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;

/**
 * Author: jzyu
 * Date  : 2017/6/2
 */

public abstract class SeedCustomLoadListFragment<T, API_SET_CLASS>
        extends SeedBaseFragment<API_SET_CLASS>
        implements IRowConvert<T>, CustomListLoader, CustomListLoaderListener<T> {

    public static final String TAG = SeedCustomLoadListFragment.class.getSimpleName();

    protected RecyclerViewFinal recyclerView;
    protected PtrFrameLayout ptrFrame;
    private ViewGroup containerEmptyView;
    private Config<T> config;

    protected static final class Config<T> {
        private List<T> items;
        private int rowLayoutId;

        private PtrUIHandler ptrUIHandler;
        private IRvEmpty emptyView;
        private ILoadMoreView loadMoreView;
        
        private String emptyTip = "暂无内容";
        private boolean enablePtr;
        private boolean enableLoadMore;
    }

    protected static final class ConfigBuilder<T> {
        Config<T> config = new Config<>();

        public ConfigBuilder(int rowLayoutId, List<T> items) {
            config.rowLayoutId = rowLayoutId;
            config.items = items;
        }
        public ConfigBuilder<T> ptrUIHandler(PtrUIHandler ptrUIHandler) {
            config.ptrUIHandler = ptrUIHandler;
            return this;
        }
        public ConfigBuilder<T> customEmptyView(IRvEmpty emptyView) {
            config.emptyView = emptyView;
            return this;
        }
        public ConfigBuilder<T> customLoadMoreFooterView(ILoadMoreView loadMoreView) {
            config.loadMoreView = loadMoreView;
            return this;
        }
        public ConfigBuilder<T> enablePtr() {
            config.enablePtr = true;
            return this;
        }
        public ConfigBuilder<T> enableLoadMore() {
            config.enableLoadMore = true;
            return this;
        }
        public Config<T> build() {
            return config;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seed_ptr_recyclerview, container, false);

        recyclerView = findView(view, R.id.recycler_view);
        ptrFrame = findView(view, R.id.ptr_frame);
        containerEmptyView = findView(view, R.id.container_empty);

        return view;
    }

    private CommonAdapter<T> newAdapter(int rowLayoutId, List<T> items) {
        return new CommonAdapter<T>(getContext(), rowLayoutId, items) {
            @Override
            protected void convert(ViewHolder holder, T item, int position) {
                onRowConvert(holder, item, position);
            }
        };
    }

    protected void init(final Config<T> config) {
        if (config.ptrUIHandler == null) {
            // 注意：因这行代码有副作用 mPtrFrameLayout.setRefreshCompleteHook(mPtrUIHandlerHook);
            // 导致设过 Material PtrUI 就不能换其他 PtrUI，否则 下拉刷新动画一直转；
            // 所以把它留做默认
            config.ptrUIHandler = SeedUtil.newPtrUIMaterial(getContext(), ptrFrame);
        }

        this.config = config;

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new ScrollSpeedLinearLayoutManger(getContext()));
        recyclerView.setAdapter(newAdapter(config.rowLayoutId, config.items));

        containerEmptyView.addView((View) config.emptyView);
        config.emptyView.setStatusEmpty(config.emptyTip);
        ((View) config.emptyView).setVisibility(View.GONE);
        recyclerView.setEmptyView((View) config.emptyView);

        recyclerView.setLoadMoreView(config.loadMoreView);

        recyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void loadMore() {
                Log.v(TAG, "onLoadMore");
                _loadData(false);
            }
        });

        // ptrFrame
        ptrFrame.setHeaderView((View) config.ptrUIHandler);
        ptrFrame.addPtrUIHandler(config.ptrUIHandler);
        ptrFrame.setPinContent(config.ptrUIHandler instanceof MaterialHeader);

        ptrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                Log.v(TAG, "PtrHandler: onRefreshBegin");
                _loadData(true);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                if (! config.enablePtr)
                    return false;

                return super.checkCanDoRefresh(frame, content, header);
            }
        });

        postRefresh();
    }

    public void postRefresh() {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (! ptrFrame.isRefreshing()) {
                    ptrFrame.autoRefresh(false);
                } else {
                    Log.e(TAG, "already in refresh");
                }
            }
        });
    }

    private void _loadData(final boolean isRefresh) {
        onLoadStart(isRefresh);
    }

    private void uiOnLoadComplete(boolean hasMore, String errorMsg) {
        if (! SeedUtil.isEmpty(errorMsg)) {
            if (getItems().size() == 0) {
                config.emptyView.setStatusError(errorMsg);
                getRvAdapter().notifyDataSetChanged();
            }
        }

        if (ptrFrame.isRefreshing()) {
            ptrFrame.refreshComplete();
            recyclerView.setHasMore(hasMore);
        } else {
            recyclerView.onLoadMoreComplete(hasMore, errorMsg);
        }
    }

    @Override
    public void onLoadSuccess(List<T> newItems) {
        if (isDetached()) { // patch
            Log.e(TAG, "fragment has detached!");
            return;
        }

        boolean isRefresh = ptrFrame.isRefreshing();
        boolean hasMore = config.enableLoadMore && newItems.size() > 0;

        Log.d(TAG, "onNewData: hasMore = " + hasMore);
        uiOnLoadComplete(hasMore, null);

        // update
        if (newItems.size() > 0) {
            if (isRefresh) {
                getItems().clear();
                getItems().addAll(newItems);
                getRvAdapter().notifyDataSetChanged();
            } else {
                // load more: append data
                int tail = getItems().size();

                getItems().addAll(newItems);
                getRvAdapter().notifyItemRangeInserted(tail, newItems.size());
            }
        } else {
            // new item is empty
            if (isRefresh) {
                getItems().clear();
                getRvAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoadFailed(String errorMsg) {
        Log.e(TAG, "onLoadFailed, errorMsg = " + errorMsg);
        uiOnLoadComplete(config.enableLoadMore, errorMsg);
    }

    final public List<T> getItems() {
        return config == null ? null : config.items;
    }

    final public RecyclerView.Adapter<RecyclerView.ViewHolder> getRvAdapter() {
        return ((HeaderAndFooterRecyclerViewAdapter) (recyclerView.getAdapter())).getInnerAdapter();
    }
}
