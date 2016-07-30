package com.charco.zoomscrawlview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by admin on 2016/5/28.
 */
public class ScaleDrawView extends View {
    
    private Bitmap mBgBitmap;//下层bitmap，即图片层
    private Bitmap mFgBitmap;//上层bitmap,即涂鸦层
    private Paint mPaint;
    private Canvas mCanvas;
    private Path mPath;
    private ArrayList<ScrawlPath> backTrack=new ArrayList<>();
    private ArrayList<ScrawlPath> returnTrack=new ArrayList<>();
    private boolean isEraser=false;
    private static final String TAG="charco";
    private float mOldDist;
    private Matrix matrix=new Matrix();

    public static final float MAX_SCALE = 4;// 最大的放缩比例
    public static final float MIN_SCALE = 1;// 最小的放缩比例

    private int dx=0;
    private int dy=0;

    private int width;
    private int height;

    private int colorRes=Color.BLUE;

    private boolean canDraw=true;
    private float paintSize=2;

    public ScaleDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
     //   Log.e("charco","getMeasuredWidth:"+getMeasuredWidth()+"getMeasuredHeight"+getMeasuredHeight());
        int desiredWidth = getResources().getDisplayMetrics().widthPixels;
        int desiredHeight = getResources().getDisplayMetrics().heightPixels;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = desiredWidth;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }


        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = desiredHeight;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);

    }

    public void setBitmapPath(String path){//设置图片路径
        //根据路径创建bitmap
        mBgBitmap=getZoomBmpByDecodePath(path,getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);
        //创建一张与下层bitmap一样大小的bitmap，用于涂鸦
        //我们所做的涂鸦操作都是在这一层上面,与下层无关
        //当保存的时候再把两张bitmap融合
        mFgBitmap=Bitmap.createBitmap(mBgBitmap.getWidth(),mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //此画板用与绘制上层bitmap
        mCanvas=new Canvas(mFgBitmap);
    }

    public Bitmap getZoomBmpByDecodePath(String path, int w, int h) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, w, h);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        int bitmapWidth=bitmap.getWidth();
        int bitmapHeight=bitmap.getHeight();
        //将图片居中摆放
        if (bitmapWidth<w && bitmapHeight<h){//当图片宽高都小于屏幕的时候
            float scaleWidth=w*1.0f/bitmapWidth;
            float scaleHeight=h*1.0f/bitmapHeight;
            float scale=scaleWidth<scaleHeight?scaleWidth:scaleHeight;
            bitmapWidth=(int) (bitmapWidth*scale);
            bitmapHeight=(int) (bitmapHeight*scale);
            moveMatrix(bitmapWidth,bitmapHeight);
            return Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight,true);
        }else if (bitmapWidth>w||bitmapHeight>h){//当图片宽或高大于屏幕的时候
            float scaleWidth=bitmapWidth*1.0f/w;
            float scaleHeight=bitmapHeight*1.0f/h;
            float scale=scaleWidth>scaleHeight?scaleWidth:scaleWidth;
            bitmapWidth=(int) (bitmapWidth/scale);
            bitmapHeight=(int) (bitmapHeight/scale);
            moveMatrix(bitmapWidth,bitmapHeight);
            return Bitmap.createScaledBitmap(bitmap, bitmapWidth,bitmapHeight ,true);
        }
        return bitmap;
    }


    private void moveMatrix(int w,int h){
        if (height>h){
            dy=(height-h)/2;
        }
        if (width>w){
            dx=(width-w)/2;
        }
        //根据图片与屏幕边缘的距离位移matrix,达到图片居中的效果
        matrix.setTranslate(dx,dy);
    }

    /**
     * 计算缩略图压缩的比列，因为每张图片长宽不一样，压缩比列也不一样
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //使用需要的宽高的最大值来计算比率
            final int suitedValue = reqHeight > reqWidth ? reqHeight : reqWidth;
            final int heightRatio = Math.round((float) height / (float) suitedValue);
            final int widthRatio = Math.round((float) width / (float) suitedValue);

            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;//用最大
        }

        return inSampleSize;
    }

    public void initPaint(){
        mPaint=new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(dip2Dimension(paintSize,getContext()));
        if (isEraser){
            mPaint.setAlpha(0);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }else{
            mPaint.setAlpha(255);
            mPaint.setColor(colorRes);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        if (pointerCount>1){//如果是两只手指操作,视为缩放
            canDraw=false;
            zoomScale(event);
        }else{
            switch (event.getAction()){//如果是一指手指操作,视为涂鸦
                case MotionEvent.ACTION_DOWN:
                    canDraw=true;
                    initPaint();//初始化paint
                    mPath=new Path();//初始化path
                    pathTo(event);
                    if (!backTrack.isEmpty()){//清空撤销list
                        backTrack.clear();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!canDraw){
                        return true;
                    }
                    pathTo(event);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!canDraw){
                        return true;
                    }
                    returnTrack.add(new ScrawlPath(mPaint,mPath));
                    break;
            }
            mCanvas.drawPath(mPath,mPaint);
        }
        invalidate();
       return true;
    }

    /**
     * 缩放处理
     *
     * @param event
     * @return
     */
    private void zoomScale(MotionEvent event) {
        int count = event.getPointerCount();
        if (count > 1) {
            int action = event.getAction();
            action = action & MotionEvent.ACTION_MASK;
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    mOldDist = getDistOfTowPoints(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float mNewDist = getDistOfTowPoints(event);
                    if (Math.abs(mNewDist - mOldDist) > 50) {
                        float[] value = new float[9];
                        matrix.getValues(value);
                        float scale = value[Matrix.MSCALE_X];//原来的放缩量
                        float px = (event.getX(0) + event.getX(1)) / 2;
                        float py = (event.getY(0) + event.getY(1)) / 2;
                        if (mOldDist > mNewDist) {
                            scale -= Math.abs(mNewDist - mOldDist) / 500f;//计算现在的放缩量
                        } else {
                            scale += Math.abs(mNewDist - mOldDist) / 500f;//计算现在的放缩量
                        }
                        if (scale < MIN_SCALE) {//如果放缩量小于最低的就置为最低放缩比
                            scale = MIN_SCALE;

                        } else if (scale > MAX_SCALE) {//如果放缩量大于最高的就置为最高放缩比
                            scale = MAX_SCALE;
                        }
                        if (scale == MIN_SCALE) {//如果放缩量为最小就把矩阵重置
                            matrix.reset();
                            matrix.setTranslate(dx,dy);
                        } else {
                            scale = scale / value[Matrix.MSCALE_X];//计算出相对的放缩量，使矩阵的放缩量为放缩到计算出来的放缩量。
                            matrix.postScale(scale, scale, px, py);
                        }
                        Log.i(TAG, "" + scale / value[Matrix.MSCALE_X] + ":" + Math.abs(mNewDist - mOldDist));
                        mOldDist = mNewDist;
                        postInvalidate();
                    }
                    break;
            }
        }
    }


    /**
     * 获取两点之间的距离
     * @return int 返回两点间的距离
     * */
    private float getDistOfTowPoints(MotionEvent event) {
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);
        float lengthX = Math.abs(x0 - x1);
        float lengthY = Math.abs(y0 - y1);
        return (float) Math.sqrt(lengthX * lengthX + lengthY * lengthY);
    }


    private void pathTo(MotionEvent event) {
        Point pointD = new Point((int) event.getX(), (int) event.getY());
        calculationRealPoint(pointD, matrix);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPath.moveTo(pointD.x, pointD.y);
        } else {
            mPath.lineTo(pointD.x, pointD.y);
        }
    }

    public void calculationRealPoint(Point point, Matrix matrix) {
        //缩放涂鸦的关键点,公式我也不懂,从其他demo找到的
        float[] values = new float[9];
        matrix.getValues(values);
        int sX = point.x;
        int sY = point.y;
        point.x = (int) ((sX - values[Matrix.MTRANS_X]) / values[Matrix.MSCALE_X]);
        point.y = (int) ((sY - values[Matrix.MTRANS_Y]) / values[Matrix.MSCALE_Y]);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mBgBitmap==null){
            return;
        }
        canvas.setMatrix(matrix);
        //绘制上层bitmap和下层bitmap,但是从视觉效果看是同一层
        canvas.drawBitmap(mBgBitmap,0,0,null);
        canvas.drawBitmap(mFgBitmap,0,0,null);
    }

    public void returnTrack(){
        if (!returnTrack.isEmpty()){
            int index=returnTrack.size() - 1;
            ScrawlPath track = returnTrack.get(index);
            backTrack.add(track);
            returnTrack.remove(index);
            mFgBitmap=Bitmap.createBitmap(mBgBitmap.getWidth(),mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            //重新绘制所有路径
            mCanvas=new Canvas(mFgBitmap);
            for (ScrawlPath track1:returnTrack){
                mCanvas.drawPath(track1.mPath,track1.mPaint);
            }
            invalidate();
        }
    }

    public void backTrack(){
        if (!backTrack.isEmpty()){
            int index=backTrack.size() - 1;
            ScrawlPath track = backTrack.get(index);
            returnTrack.add(track);
            backTrack.remove(index);
            mFgBitmap=Bitmap.createBitmap(mBgBitmap.getWidth(),mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            //重新绘制所有路径
            mCanvas=new Canvas(mFgBitmap);
            for (ScrawlPath track1:returnTrack){
                mCanvas.drawPath(track1.mPath,track1.mPaint);
            }
            invalidate();

        }
    }

    public void clear(){
        mFgBitmap=Bitmap.createBitmap(mBgBitmap.getWidth(),mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas=new Canvas(mFgBitmap);
        invalidate();
        if (!backTrack.isEmpty()){
            backTrack.clear();
        }
        if (!returnTrack.isEmpty()){
            returnTrack.clear();
        }
    }

    public void setEraserMode(boolean boo){//设置橡皮擦模式
        isEraser=boo;
    }

    public void setColor(int colorRes){//设置画笔颜色
        isEraser=false;
        this.colorRes=colorRes;
    }

    public Bitmap getBitmap(){
        //创建新画板
        //将两层的bitmap融合
        Bitmap bitmap=Bitmap.createBitmap(mBgBitmap.getWidth(),mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mBgBitmap,0,0,null);
        canvas.drawBitmap(mFgBitmap,0,0,null);
        return bitmap;
    }

    /**
     * dip 转换成 px
     *
     * @param dip
     * @param context
     * @return
     */
    private float dip2Dimension(float dip, Context context) {
        DisplayMetrics displayMetrics = context.getResources()
                .getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                displayMetrics);
    }

    public void setPaintSize(int size) {//设置画笔大小
        this.paintSize=size;
    }
}
