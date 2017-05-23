package jzyu.github.com.seeddemo.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.jzyu.library.seed.http.HttpUtil;
import com.github.jzyu.library.seed.http.RetroHelper;
import com.github.jzyu.library.seed.ui.base.SeedBaseActivity;

import jzyu.github.com.seeddemo.model.DemoService;

/**
 * Author: jzyu
 * Date  : 2017/5/20
 */

public class BaseActivity extends SeedBaseActivity<DemoService> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetroHelper(new RetroHelper<>(
                getApplicationContext(),
                "http://api.douban.com/v2/",
                DemoService.class, null));
        http = new HttpUtil(this, null, null);
    }
}
