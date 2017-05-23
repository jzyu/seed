package com.github.jzyu.library.seed.http;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by weplant on 16/4/15.
 */
public abstract class HttpCallback<T> {
    public abstract void onSuccess(Call<T> call, Response<T> response);

    public abstract void onError(int responseCode, String errorMsg);

    public void onCancel(Call<T> call) {/*do noting*/ }
}
