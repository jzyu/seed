package cn.finalteam.loadingviewfinal;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.jzyu.library.seed.R;

import java.lang.reflect.Constructor;



/**
 * Desction:
 * Author:pengjianbo
 * Date:16/3/7 下午6:40
 */
public class RecyclerViewFinal extends RecyclerView implements OnScrollBottomListener {

    public static final String TAG = RecyclerViewFinal.class.getSimpleName();

    /**
     * 加载更多UI
     */
    ILoadMoreView mLoadMoreView;

    /**
     * 加载更多方式，默认滑动到底部加载更多
     */
    LoadMoreMode mLoadMoreMode = LoadMoreMode.SCROLL;
    /**
     * 加载更多lock
     */
    private boolean mLoadMoreLock;
    /**
     * 是否可以加载跟多
     */
    boolean mHasMore;
    /**
     * 是否加载失败
     */
    //private boolean mHasLoadFail;

    /**
     * 加载更多事件回调
     */
    private OnLoadMoreListener mOnLoadMoreListener;

    /**
     * emptyview
     */
    private View mEmptyView;

    /**
     * 没有更多了是否隐藏loadmoreview
     */
    private boolean mHideNorMoreTip;

    private HeaderAndFooterRecyclerViewAdapter mOuterAdapter;
    private boolean mLoadMoreFooterAdded;

    public RecyclerViewFinal(Context context) {
        super(context);
        init(context, null);
    }

    public RecyclerViewFinal(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RecyclerViewFinal(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mOuterAdapter = new HeaderAndFooterRecyclerViewAdapter();
        super.setAdapter(mOuterAdapter);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingViewFinal);

        if (a.hasValue(R.styleable.LoadingViewFinal_loadMoreMode)) {
            mLoadMoreMode = LoadMoreMode.mapIntToValue(a.getInt(R.styleable.LoadingViewFinal_loadMoreMode, 0x01));
        } else {
            mLoadMoreMode = LoadMoreMode.SCROLL;
        }

        if (a.hasValue(R.styleable.LoadingViewFinal_noLoadMoreHideView)) {
            mHideNorMoreTip = a.getBoolean(R.styleable.LoadingViewFinal_noLoadMoreHideView, false);
        } else {
            mHideNorMoreTip = false;
        }

        if (a.hasValue(R.styleable.LoadingViewFinal_loadMoreView)) {
            try {
                String loadMoreViewName = a.getString(R.styleable.LoadingViewFinal_loadMoreView);
                Class clazz = Class.forName(loadMoreViewName);
                Constructor c = clazz.getConstructor(Context.class);
                ILoadMoreView loadMoreView = (ILoadMoreView) c.newInstance(context);
                mLoadMoreView = loadMoreView;
            } catch (Exception e) {
                e.printStackTrace();
                mLoadMoreView = new DefaultLoadMoreView(context);
            }
        } else {
            mLoadMoreView = new DefaultLoadMoreView(context);
        }

        mLoadMoreView.getView().setOnClickListener(new OnMoreViewClickListener());

        //setHasMore(false);
        a.recycle();
        addOnScrollListener(new RecyclerViewOnScrollListener());
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        try {
            adapter.unregisterAdapterDataObserver(mDataObserver);
        } catch (Exception e){}
        adapter.registerAdapterDataObserver(mDataObserver);

        mOuterAdapter.setInnerAdapter(adapter);
        if (null == getLayoutManager()) {
            throw new IllegalArgumentException("must set layout manager first!");
        } else {
            mOuterAdapter.putLayoutManager(getLayoutManager());
        }
    }

    @Override
    public void onScrollBottom() {
        if (mHasMore && mLoadMoreMode == LoadMoreMode.SCROLL) {
            executeLoadMore();
        }
    }

    /**
     * 设置recyclerview emptyview
     * @param emptyView
     */
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        //checkIfEmpty();
    }

    // 没有数据时也显示headerView
    private boolean isHeadViewFixDisplay;
    public void setHeaderViewFixDisplay() {
        isHeadViewFixDisplay = true;
    }

    private void checkIfEmpty() {
        if (mEmptyView == null) return;

        boolean isEmpty;
        if (isHeadViewFixDisplay) {
            isEmpty = (mOuterAdapter.getItemCount() == 0);
        } else {
            isEmpty = (mOuterAdapter.getInnerAdapter().getItemCount() == 0);
        }

        Log.v(TAG, "checkIfEmpty: isEmpty = " + isEmpty);
        mEmptyView.setVisibility(isEmpty ? VISIBLE : GONE);
        setVisibility(isEmpty ? GONE : VISIBLE);
    }

    /**
     * 设置LoadMoreView
     * @param loadMoreView
     */
    public void setLoadMoreView(ILoadMoreView loadMoreView) {
        if (mLoadMoreView != null) {
            try {
                removeFooterView(mLoadMoreView.getView());
                mLoadMoreFooterAdded = false;
            } catch (Exception e){}
        }
        mLoadMoreView = loadMoreView;
        mLoadMoreView.getView().setOnClickListener(new OnMoreViewClickListener());
    }

    /**
     * 设置加载更多模式
     * @param mode
     */
    public void setLoadMoreMode(LoadMoreMode mode) {
        mLoadMoreMode = mode;
    }

    /**
     * 设置没有更多数据了，是否隐藏fooler view
     * @param hide
     */
    public void setNoLoadMoreHideView(boolean hide) {
        this.mHideNorMoreTip = hide;
    }

    /**
     * 没有很多了
     */
    void showNoMoreUI() {
        mLoadMoreLock = false;
        mLoadMoreView.showNoMore();
    }

    /**
     * 显示失败UI
     */
    public void showFailUI(String errorText) {
        mLoadMoreLock = false;
        mLoadMoreView.showFail(errorText);
    }

    /**
     * 显示默认UI
     */
    void showNormalUI() {
        mLoadMoreLock = false;
        mLoadMoreView.showNormal();
    }

    /**
     * 显示加载中UI
     */
    void showLoadingUI(){
        mLoadMoreView.showLoading();
    }

    public void setHasMore(boolean hasMore) {
        this.mHasMore = hasMore;

        // check and add footerView
        if (! mHideNorMoreTip && ! mLoadMoreFooterAdded) {
            mLoadMoreFooterAdded = true;
            addFooterView(mLoadMoreView.getView());
        }

        if (mHasMore) {
            showNormalUI();
        } else {
            showNoMoreUI();
        }
    }

    /**
     * 设置加载更多事件回调
     * @param loadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.mOnLoadMoreListener = loadMoreListener;
    }

    /**
     * 完成加载更多
     */
    public void onLoadMoreComplete(boolean hasMore, String errorText) {
        if (errorText != null && errorText.length() > 0) {
            showFailUI(errorText);
        } else {
            setHasMore(hasMore);
        }
    }

    /**
     * 添加footer view
     * @param footerView
     */
    public void addFooterView(View footerView) {
        mOuterAdapter.addFooterView(footerView);
    }

    /**
     * 添加header view
     * @param headerView
     */
    public void addHeaderView(View headerView) {
        mOuterAdapter.addHeaderView(headerView);
    }

    public void removeFooterView(View footerView) {
        mOuterAdapter.removeFooter(footerView);
    }

    public void removeHeaderView(View headerView) {
        mOuterAdapter.removeHeader(headerView);
    }

    /**
     * 点击more view加载更多
     */
    class OnMoreViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(mHasMore) {
                executeLoadMore();
            }
        }
    }

    /**
     * 加载更多
     */
    public void executeLoadMore() {
        if(!mLoadMoreLock && mHasMore) {
            mLoadMoreLock = true;//上锁
            showLoadingUI();

            if (mOnLoadMoreListener != null) {
                mOnLoadMoreListener.loadMore();
            }
        }
    }

    public RecyclerView.Adapter<RecyclerView.ViewHolder> getInnerAdapter() {
        return ((HeaderAndFooterRecyclerViewAdapter) getAdapter()).getInnerAdapter();
    }

    public View getChildAtPosition(int position) {
        return super.getChildAt(position + mOuterAdapter.getHeadersCount());
    }

    /**
     * 设置OnItemClickListener
     * @param listener
     */
    public void setOnItemClickListener(HeaderAndFooterRecyclerViewAdapter.OnItemClickListener listener) {
        mOuterAdapter.setOnItemClickListener(listener);
    }

    /**
     * 设置OnItemLongClickListener
     * @param listener
     */
    public void setOnItemLongClickListener(HeaderAndFooterRecyclerViewAdapter.OnItemLongClickListener listener) {
        mOuterAdapter.setOnItemLongClickListener(listener);
    }

    public int getHeaderViewCount() {
        return mOuterAdapter.getHeadersCount();
    }

    /**
     * 滚动到底部自动加载更多数据
     */
    private class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
        public final String TAG = RecyclerViewOnScrollListener.class.getSimpleName();

        /**
         * 最后一个的位置
         */
        private int[] lastPositions;

        /**
         * 最后一个可见的item的位置
         */
        private int lastVisibleItemPosition;

        /**
         * 当前滑动的状态
         */
        private int currentScrollState = 0;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

            if (layoutManager instanceof LinearLayoutManager) {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null) {
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
            } else {
                throw new RuntimeException(
                        "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            currentScrollState = newState;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();

            int footerCount = 0;
            if (mOuterAdapter != null) {
                footerCount = mOuterAdapter.getFootersCount();
            }

            if (visibleItemCount > footerCount
                    && currentScrollState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItemPosition >= totalItemCount - 1) {
                //Log.v(TAG, "onScrollBottom");
                onScrollBottom();
            }
        }

        /**
         * 取数组中最大值
         *
         * @param lastPositions
         * @return
         */
        private int findMax(int[] lastPositions) {
            int max = lastPositions[0];
            for (int value : lastPositions) {
                if (value > max) {
                    max = value;
                }
            }

            return max;
        }
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        public static final String TAG = "DataObserver";

        private void handleDataRangeChanged() {
            checkIfEmpty();
            // 刷新数据时停止滑动,避免出现数组下标越界问题
            dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
        }

        @Override
        public void onChanged() {
            super.onChanged();
            Log.v(RecyclerViewFinal.TAG + "-" + TAG, "onChanged");

            handleDataRangeChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            Log.v(RecyclerViewFinal.TAG + "-" + TAG, "onItemRangeInserted");

            handleDataRangeChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            Log.v(RecyclerViewFinal.TAG + "-" + TAG, "onItemRangeRemoved");

            handleDataRangeChanged();
        }
    };
}
