package jzyu.github.com.seeddemo.model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Author: jzyu
 * Date  : 2017/5/18
 */

public interface DemoService {
    @GET("movie/top250")
    Call<ApiData.Movies> getMovieTop250(@Query("start") int start, @Query("count") int count);
}
