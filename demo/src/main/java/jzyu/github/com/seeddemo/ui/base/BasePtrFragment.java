package jzyu.github.com.seeddemo.ui.base;

import com.github.jzyu.library.seed.ui.ptr.SeedPtrListFragment;

import jzyu.github.com.seeddemo.model.DemoService;

/**
 * Author: jzyu
 * Date  : 2017/5/20
 */

public class BasePtrFragment {

    public abstract static class List<T> extends SeedPtrListFragment<T, DemoService> {

    }
}
