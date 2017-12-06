package com.github.jzyu.library.seed.http;

import java.util.List;

import retrofit2.Response;

/**
 * Author: jzyu
 * Date  : 2017/03/18
 * Desc  : before / after 一对方法，因为
 *         1. 动态获取更新条数处理要在 before 处理，否则数据已替换
 *         2. 私信聊天收到新信息后滚动到底要在 after 处理，否则最后一条看不到
 */

public class HttpLoadListCallback <API_DATA_TYPE, ITEM_TYPE> {
    public void onBeforeUpdate(Response<API_DATA_TYPE> response, List<ITEM_TYPE> newItems) {

    }

    public void onAfterUpdate(Response<API_DATA_TYPE> response) {

    }

    public void onUpdateError(int responseCode, String errorMsg) {

    }
}
