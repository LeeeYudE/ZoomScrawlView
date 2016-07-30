package com.charco.zoomscrawlview.view;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by admin on 2016/5/29.
 */
public class ScrawlPath {
    //记录涂鸦路径的类,路径包含path之外还需保存与之相关的颜色还有是否为橡皮擦模式

    public Paint mPaint;
    public Path mPath;

    public ScrawlPath(Paint mPaint, Path mPath) {
        this.mPaint = mPaint;
        this.mPath = mPath;
    }
}
