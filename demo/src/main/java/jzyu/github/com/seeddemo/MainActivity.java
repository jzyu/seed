package jzyu.github.com.seeddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import jzyu.github.com.seeddemo.model.ApiData;
import jzyu.github.com.seeddemo.model.DemoService;
import jzyu.github.com.seeddemo.ui.base.BaseActivity;
import jzyu.github.com.seeddemo.ui.base.BasePtrFragment;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static class ListFragment extends BasePtrFragment.List<ApiData.MovieItem> {

        public static final int PAGE_LENGTH = 20;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            init(getBuilder(R.layout.row_movie_item, new ArrayList<ApiData.MovieItem>())
                    .enablePtr()
                    .enableLoadMore()
                    .dataPageLength(PAGE_LENGTH)
                    .build());
        }

        @Override
        public void onRowConvert(ViewHolder holder, ApiData.MovieItem item, int position) {
            Picasso.with(getContext()).load(item.images.medium).into((ImageView) holder.getView(R.id.iv_cover));
            holder.setText(R.id.tv_title, item.title);
        }

        @Override
        protected void onLoadData(DemoService demoService, boolean isRefresh) {
            httpGo(demoService.getMovieTop250(isRefresh ? 0 : getItems().size(), PAGE_LENGTH), isRefresh, null);
        }

        @Override
        protected List<ApiData.MovieItem> onProvideItemsInResponse(Response response) {
            return ((Response<ApiData.Movies>) response).body().subjects;
        }
    }
}
