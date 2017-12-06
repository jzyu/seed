package com.github.jzyu.library.seed.ui.ptr;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jzyu.library.seed.R;
import com.github.jzyu.library.seed.http.HttpCacheType;
import com.github.jzyu.library.seed.http.HttpCallback;
import com.github.jzyu.library.seed.http.HttpLoadListCallback;
import com.github.jzyu.library.seed.ui.base.SeedBaseFragment;
import com.github.jzyu.library.seed.ui.base.UiFeatures;
import com.github.jzyu.library.seed.ui.view.DefaultRvEmptyView;
import com.github.jzyu.library.seed.util.Callback;
import com.github.jzyu.library.seed.util.SeedConsts;
import com.github.jzyu.library.seed.util.SeedUtil;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.finalteam.loadingviewfinal.HeaderAndFooterRecyclerViewAdapter;
import cn.finalteam.loadingviewfinal.ILoadMoreView;
import cn.finalteam.loadingviewfinal.OnLoadMoreListener;
import cn.finalteam.loadingviewfinal.RecyclerViewFinal;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Author: jzyu
 * Date  : 2017/5/16
 */

public abstract class SeedPtrRecyclerViewFragment<T, API_SET_CLASS> extends SeedBaseFragment<API_SET_CLASS>
        implements UiFeatures.BackTopListener {

    public static final String TAG = SeedPtrRecyclerViewFragment.class.getSimpleName();

    // 避免和 activity request code 冲突
    protected static final int REQUEST_BASE = 100;

    protected RecyclerViewFinal recyclerView;
    protected PtrFrameLayout ptrFrame;

    private ViewGroup containerEmptyView;
    private Config<T> config;

    private boolean isLoadingLocked; // 防止 onLoadData 重入
    private boolean isLoadNetworkCache;

    protected abstract void onLoadData(API_SET_CLASS apiSetClass, final boolean isRefresh);
    protected abstract List<T> onProvideItemsInResponse(Response response);

    public interface ListLoader<T> {
        List<T> onLoadList();
    }

    public interface CanDoRefreshChecker {
        boolean canDoRefresh();
    }

    public interface RefreshErrorTipper {
        void onTipError(String error);
    }

    protected enum LoadFrom {
        LOCAL, NETWORK, NETWORK_CACHE
    }

    protected static final class Config<T> {
        private List<T> items;

        private PtrUIHandler ptrUIHandler;
        private RecyclerView.LayoutManager layoutManager;
        private MultiItemTypeAdapter<T> adapter;

        private boolean enablePtr;
        private boolean enableLoadMore;

        private boolean disableNorMoreTip;
        private boolean disablePtrAnimation; // 刷新数据时不显示下拉刷新动画，界面：私信聊天、图片详情

        private int dataPageLength = 30;//Consts.PAGE_LENGTH;

        private String emptyTip = "暂无内容";
        private IRvEmpty emptyView;
        private ILoadMoreView loadMoreView;

        private boolean dataSorted;
        private boolean enableLoadingView;

        private CanDoRefreshChecker canDoRefreshChecker;
        private RefreshErrorTipper refreshErrorTipper;

        private FirstLoadConfig<T> firstLoad = new FirstLoadConfig();

        private static class FirstLoadConfig<T> {
            private boolean disabled;
            private boolean thenRefresh = true;
            private LoadFrom from = LoadFrom.NETWORK_CACHE;
            private ListLoader<T> cacheLoader;  // exec in another thread
        }
    }

    protected static final class ConfigBuilder<T> {
        Config<T> config = new Config<>();

        public ConfigBuilder<T> items(List<T> items) {
            config.items = items;
            return this;
        }

        public ConfigBuilder<T> ptrUIHandler(PtrUIHandler ptrUIHandler) {
            config.ptrUIHandler = ptrUIHandler;
            return this;
        }

        public ConfigBuilder<T> layoutManager(RecyclerView.LayoutManager layoutManager) {
            config.layoutManager = layoutManager;
            return this;
        }

        public ConfigBuilder<T> adapter(MultiItemTypeAdapter<T> adapter) {
            config.adapter = adapter;
            return this;
        }

        public ConfigBuilder<T> firstLoadFromLocal() {
            config.firstLoad.from = LoadFrom.LOCAL;
            return this;
        }

        public ConfigBuilder<T> firstLoadFromNetwork() {
            config.firstLoad.from = LoadFrom.NETWORK;
            return this;
        }

        public ConfigBuilder<T> enableLoadingView() { // 只动态用到，因动态 api 非常慢
            config.enableLoadingView = true;
            return this;
        }

        public ConfigBuilder<T> firstLoadAsync(ListLoader<T> loader) {
            config.firstLoad.cacheLoader = loader;
            return this;
        }

        public ConfigBuilder<T> disableNetworkCache() {
            config.firstLoad.from = LoadFrom.NETWORK;
            return this;
        }

        public ConfigBuilder<T> disableAutoLoad() {
            config.firstLoad.disabled = true;
            return this;
        }

        public ConfigBuilder<T> disableRefreshAfterFirstLoad() {
            config.firstLoad.thenRefresh = false;
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

        public ConfigBuilder<T> emptyTip(String emptyTip) {
            config.emptyTip = emptyTip;
            return this;
        }

        public ConfigBuilder<T> disableNoMoreTip() { // 加载完时不显示没有更多
            config.disableNorMoreTip = true;
            return this;
        }

        public ConfigBuilder<T> disablePtrAnimation() {
            config.disablePtrAnimation = true;
            return this;
        }

        // 加载数据时一次返回的条数，如实际返回小于此值则已加载完 (没有更多)
        public ConfigBuilder<T> dataPageLength(int pageLength) {
            config.dataPageLength = pageLength;
            return this;
        }

        public ConfigBuilder<T> customCanDoRefresh(CanDoRefreshChecker checker) {
            config.canDoRefreshChecker = checker;
            return this;
        }

        public ConfigBuilder<T> customRefreshErrorTipper(RefreshErrorTipper tipper) {
            config.refreshErrorTipper = tipper;
            return this;
        }

        public ConfigBuilder<T> dataSorted() { // 数据已排序，下拉刷新时启用增量更新
            config.dataSorted = true;
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

        public Config<T> build() {
            return config;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateViewEx(0, inflater, container, savedInstanceState);
    }

    final protected View onCreateViewEx(int customLayoutId, LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                customLayoutId > 0 ? customLayoutId : R.layout.seed_ptr_recyclerview,
                container, false);

        recyclerView = findView(view, R.id.recycler_view);
        ptrFrame = findView(view, R.id.ptr_frame);
        containerEmptyView = findView(view, R.id.container_empty);

        return view;
    }

    final protected void init(final Config<T> config) {
        if (config.ptrUIHandler == null) {
            // 注意：因这行代码有副作用 mPtrFrameLayout.setRefreshCompleteHook(mPtrUIHandlerHook);
            // 导致设过 Material PtrUI 就不能换其他 PtrUI，否则 下拉刷新动画一直转；
            // 所以把它留做默认
            config.ptrUIHandler = SeedUtil.newPtrUIMaterial(getContext(), ptrFrame);
        }

        this.config = config;

        isLoadNetworkCache = (
                config.firstLoad.from == LoadFrom.NETWORK_CACHE
                        && config.firstLoad.cacheLoader == null
                        && ! config.firstLoad.disabled);

        // recyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(config.layoutManager);
        recyclerView.setAdapter(config.adapter);

        // recyclerViewFinal
        if (config.emptyView == null) {
            config.emptyView = new DefaultRvEmptyView(getContext());
        }
        containerEmptyView.addView((View) config.emptyView);

        config.emptyView.setStatusEmpty(config.emptyTip);
        ((View) config.emptyView).setVisibility(View.GONE);
        recyclerView.setEmptyView((View) config.emptyView);

        if (config.disableNorMoreTip) {
            recyclerView.setNoLoadMoreHideView(true);
        }

        if (config.loadMoreView != null) {
            // 即使不启用 loadMore，也要设置 loadMoreFooter，因为要显示没有更多
            recyclerView.setLoadMoreView(config.loadMoreView);
        }
        if (config.enableLoadMore) {
            recyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void loadMore() {
                    Log.v(TAG, "onLoadMore");
                    _loadData(false);
                }
            });
        }

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

                if (config.canDoRefreshChecker != null) {
                    return config.canDoRefreshChecker.canDoRefresh();
                } else {
                    return super.checkCanDoRefresh(frame, content, header);
                }
            }
        });

        if (config.firstLoad.from == LoadFrom.LOCAL) {
            // 此方法放需在 setLoadMoreView 后面，否则刚添加的 footerView 又被移除了
            recyclerView.setHasMore(false);

            // for checkIfEmpty
            getRvAdapter().notifyDataSetChanged();
        } else {
            if (! config.firstLoad.disabled) {
                firstLoad();
            }
        }
    }

    private void asyncLoadCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "firstLoad in thread");

                isLoadingLocked = true;
                final List<T> items = config.firstLoad.cacheLoader.onLoadList();
                isLoadingLocked = false;

                if (null != items) {
                    getItems().addAll(items);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 取到缓存数据才刷新显示，避免首次进入时显示暂无内容
                        if (! SeedUtil.isEmpty(items)) {
                            recyclerView.setHasMore(false);
                            getRvAdapter().notifyDataSetChanged();
                        }

                        if (config.firstLoad.thenRefresh) {
                            postRefresh();
                        }
                    }
                });
            }
        }).start();
    }

    final public RecyclerView.Adapter<RecyclerView.ViewHolder> getRvAdapter() {
        return ((HeaderAndFooterRecyclerViewAdapter) (recyclerView.getAdapter())).getInnerAdapter();
    }

    private void showLoadingView() {
        if (config.enableLoadingView && getItems().size() == 0) {
            config.emptyView.setStatusLoading();
            getRvAdapter().notifyDataSetChanged();
        }
    }

    private void hideLoadingView() {
        if (config.enableLoadingView && getItems().size() == 0) {
            ((View) config.emptyView).setVisibility(View.GONE);
        }
    }

    private void uiOnLoadComplete(boolean hasMore, String errorMsg) {
        if (! SeedUtil.isEmpty(errorMsg)) {
            if (getItems().size() == 0) {
                config.emptyView.setStatusError(errorMsg);
                getRvAdapter().notifyDataSetChanged();
            }

            if (config.refreshErrorTipper != null) {
                config.refreshErrorTipper.onTipError(errorMsg);
            }
        }

        if (ptrFrame.isRefreshing()) {
            ptrFrame.refreshComplete();
            recyclerView.setHasMore(hasMore);
        } else {
            recyclerView.onLoadMoreComplete(hasMore, errorMsg);
        }
    }

    private void refresh() {
        Log.v(TAG, "refresh");

        if (config.disablePtrAnimation) {
            _loadData(true);
        } else {
            if (!ptrFrame.isRefreshing()) {
                ptrFrame.autoRefresh(false);
            } else {
                Log.e(TAG, "already in refresh");
            }
        }
    }

    private void postRefresh(int delayMs) {
        if (delayMs > 0) {
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }, delayMs);
        } else {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }

    public void postRefresh() {
        postRefresh(0);
    }

    private static void trySmoothScrollToTop(RecyclerView rv) {
        if (rv.getLayoutManager() instanceof LinearLayoutManager
                && ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition() < 30) {
            rv.smoothScrollToPosition(0);
        } else {
            rv.scrollToPosition(0);
        }
    }

    // 当 top 可见时执行 preAppend，调用 notifyItemRangeInsert 会看不到插入动画，需要 scrollToPosition(0) 一次
    private static void scrollTopIfTopVisible(RecyclerView rv) {
        if (rv.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();

            if (lm.findFirstVisibleItemPosition() == 0) {
                lm.scrollToPosition(0);
            }
        } else if (rv.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) rv.getLayoutManager();

            boolean topIsVisible = false;
            int positions[] = lm.findFirstVisibleItemPositions(null);
            for (int position : positions) {
                if (position == 0) {
                    topIsVisible = true;
                }
            }

            if (topIsVisible) {
                lm.scrollToPosition(0);
            }
        }
    }

    private boolean isAtTop(RecyclerView recyclerView) {
        // 不能上滚动时，已到顶
        return ! recyclerView.canScrollVertically(-1);
    }

    @Override
    public void onBackTop() {
        if (! isAtTop(recyclerView)) {
            trySmoothScrollToTop(recyclerView);
        } else {
            postRefresh();
        }
    }

    public void scrollToTop() {
        scrollTopThen(null);
    }

    public void scrollTopThen(final Callback.Simple listener) {
        if (isAtTop(recyclerView)) {
            if (listener != null)
                listener.onComplete();
        } else {
            trySmoothScrollToTop(recyclerView);

            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Log.v(TAG, "onComplete async.");
                    if (listener != null)
                        listener.onComplete();
                }
            }, 500);
        }
    }

    public void smoothScrollToPosition(int position) {
        if (position < 0) {
            position = getItems().size() - 1;
        }
        //recyclerView.scrollToPosition(position);
        recyclerView.smoothScrollToPosition(position);
    }

    public void scrollToBottom() {
        recyclerView.scrollToPosition(getItems().size() - 1);
    }

    public List<T> getItems() {
        return config == null ? null : config.items;
    }

    public T getItemOfBottom() {
        if (SeedUtil.isEmpty(getItems())) {
            return null;
        } else {
            return getItems().get(getItems().size() - 1);
        }
    }

    private void firstLoad() {
        showLoadingView();

        if (config.firstLoad.cacheLoader != null) {
            asyncLoadCache();
        } else if (config.firstLoad.from == LoadFrom.NETWORK) {
            postRefresh();
        } else {
            _loadData(true);
        }
    }

    private void _loadData(final boolean isRefresh) {
        // lock it
        if (isLoadingLocked) {
            Log.e(TAG, "_loadData: locked !");
            return;
        }

        Log.v(TAG, String.format(Locale.getDefault(),
                "_loadData: refresh=%s, isLoadNetworkCache=%s", isRefresh, isLoadNetworkCache));

        isLoadingLocked = true;
        onLoadData(getApiSet(isLoadNetworkCache), isRefresh);
    }

    private API_SET_CLASS getApiSet(boolean isLoadNetworkCache) {
        HttpCacheType cacheType = isLoadNetworkCache ? HttpCacheType.ONLY : HttpCacheType.NONE;

        return getRetroHelper().getRetroInfo(cacheType).apiSet;
    }

    final protected <API_DATA_TYPE> void httpGo(final Call<API_DATA_TYPE> httpCall,
                                                final boolean isRefresh,
                                                final HttpLoadListCallback<API_DATA_TYPE, T> extraCallback)
    {
        httpGo(httpCall, isRefresh, false, extraCallback);
    }

    final protected <API_DATA_TYPE> void httpGo(final Call<API_DATA_TYPE> httpCall,
                                                final boolean isRefresh,
                                                final boolean provideItemsInBackground,
                                                final HttpLoadListCallback<API_DATA_TYPE, T> callback)
    {
        //Log.v(TAG, "httpGo begin ts = " + System.currentTimeMillis());

        http.go(httpCall, new HttpCallback<API_DATA_TYPE>() {
            @Override
            public void onSuccess(final Call<API_DATA_TYPE> call, final Response<API_DATA_TYPE> response) {
                //Log.v(TAG, "httpGo onSuccess ts = " + System.currentTimeMillis());

                if (provideItemsInBackground) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<T> newItems = provideItemsInResponseNoneNull(response);

                            if (getAty() != null) {
                                getAty().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onNewData(response, newItems);
                                    }
                                });
                            }
                        }
                    }).start();
                } else {
                    onNewData(response, provideItemsInResponseNoneNull(response));
                }
            }

            private void onNewData(final Response<API_DATA_TYPE> response, List<T> newItems) {
                if (isDetached()) { // patch
                    Log.e(TAG, "fragment has detached!");
                    return;
                }

                boolean hasMore = config.enableLoadMore && newItems.size() > 0;
                if (hasMore && config.dataPageLength > 0) {
                    hasMore = newItems.size() >= config.dataPageLength;
                }

                Log.d(TAG, "onNewData: hasMore = " + hasMore);
                uiOnLoadComplete(hasMore, null);

                // update
                if (newItems.size() > 0) {
                    if (isRefresh) {
                        if (getItems().size() > 0 && config.dataSorted) {
                            // 符合增量更新条件

                            // 查找增量更新起始点
                            int pos = -1;
                            if (getItems().size() > 0) {
                                pos = newItems.indexOf(getItems().get(0));
                            }

                            if (pos == 0) {
                                Log.v(TAG, "data not changed.");
                            } else if (pos > 0) {
                                // 执行增量更新
                                List<T> incrementItems = newItems.subList(0, pos);
                                Log.d(TAG, "increment update, count = " + incrementItems.size());

                                cbBeforeUpdate(response, incrementItems);
                                getItems().addAll(0, incrementItems);
                                getRvAdapter().notifyItemRangeInserted(0, pos);
                                cbAfterUpdate(response);

                                scrollTopIfTopVisible(recyclerView);
                            } else {
                                Log.e(TAG, "data has all changed !");
                                updateAllData(response, newItems);
                            }
                        } else {
                            // 全部更新（可能数据根本没变）
                            updateAllData(response, newItems);
                        }
                    } else {
                        // load more: append data
                        int tail = getItems().size();

                        cbBeforeUpdate(response, newItems);
                        getItems().addAll(newItems);
                        getRvAdapter().notifyItemRangeInserted(tail, newItems.size());
                        cbAfterUpdate(response);
                    }
                } else {
                    // new item is empty
                    if (isRefresh) {
                        cbBeforeUpdate(response, newItems);
                        getItems().clear();
                        getRvAdapter().notifyDataSetChanged();
                        cbAfterUpdate(response);
                    }
                }

                if (isLoadNetworkCache
                        && config.firstLoad.thenRefresh
                        && SeedUtil.isConnected(getContext())) {
                    postRefresh();
                }

                isLoadingLocked = false;
                isLoadNetworkCache = false;
            }

            private void cbBeforeUpdate(Response<API_DATA_TYPE> response, List<T> newItems) {
                if (callback != null) {
                    callback.onBeforeUpdate(response, newItems);
                }
            }

            private void cbAfterUpdate(Response<API_DATA_TYPE> response) {
                if (callback != null) {
                    callback.onAfterUpdate(response);
                }
            }

            private void cbUpdateError(int responseCode, String errorMsg) {
                if (callback != null) {
                    callback.onUpdateError(responseCode, errorMsg);
                }
            }

            private void updateAllData(Response<API_DATA_TYPE> response, List<T> newItems) {
                cbBeforeUpdate(response, newItems);

                getItems().clear();
                getItems().addAll(newItems);
                getRvAdapter().notifyDataSetChanged();

                cbAfterUpdate(response);
            }

            @Override
            public void onError(int responseCode, String errorMsg) {
                cbUpdateError(responseCode, errorMsg);

                if (isLoadNetworkCache && responseCode == SeedConsts.HTTP_STATUS_CODE_CACHE_IS_EMPTY) {
                    hideLoadingView();

                    // load cache 无 ui，不需要处理 complete
                    // 另外：100ms 为话题页打补丁，否则 ptr 动画显示不出来
                    postRefresh(100);
                } else {
                    uiOnLoadComplete(config.enableLoadMore, errorMsg);
                }

                isLoadingLocked = false;
                isLoadNetworkCache = false;
            }
        });
    }

    private List<T> provideItemsInResponseNoneNull(Response response) {
        //Log.v(TAG, "provideItemsInResponse begin ms = " + System.currentTimeMillis());
        List<T> items = onProvideItemsInResponse(response);
        //Log.v(TAG, "provideItemsInResponse end ms = " + System.currentTimeMillis());

        if (items == null) {
            items = new ArrayList<>(0);
        }
        return items;
    }

    final protected int getPageLength() {
        return config.dataPageLength;
    }

    final protected void invalidItem(T item) {
        getRvAdapter().notifyItemChanged(getItems().indexOf(item));
    }

    final public boolean isLoading() {
        return ptrFrame.isRefreshing() || isLoadingLocked;
    }

    public RecyclerViewFinal getRecyclerView() {
        return recyclerView;
    }
}
