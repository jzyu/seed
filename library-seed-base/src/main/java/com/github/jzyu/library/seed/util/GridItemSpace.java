package com.github.jzyu.library.seed.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridItemSpace extends RecyclerView.ItemDecoration {
    private final int space;
    private final int rowCount;

    public GridItemSpace(int space, int rowCount) {
        this.space = space;
        this.rowCount = rowCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        outRect.bottom = space;

        //只有第一行需要top，避免space = top+bottom
        if (position < rowCount) {
            outRect.top = space;
        }

        if (position % rowCount == 0) {
            outRect.left = space;
            outRect.right = space / 2;
        } else {
            outRect.left = space / 2;
            outRect.right = space;
        }
    }
}
