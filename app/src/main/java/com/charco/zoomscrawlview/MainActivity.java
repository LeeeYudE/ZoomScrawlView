package com.charco.zoomscrawlview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.charco.zoomscrawlview.view.ScaleDrawView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "charco";
    private Button btnCancle, btnSava, btnBack;
    private RadioGroup colorGroup;
    private File files;
    private SeekBar skWordSize;
    private Bitmap bitmap;
    private TextView tvSize;
    private ScaleDrawView drawView;
    private String savePath;
    private CheckBox cbEraser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        btnCancle = (Button) findViewById(R.id.cancle);
        btnSava = (Button) findViewById(R.id.sava);
        colorGroup = (RadioGroup) findViewById(R.id.color);
        skWordSize = (SeekBar) findViewById(R.id.sk_word_size);
        tvSize = (TextView) findViewById(R.id.tv_size);
        btnBack = (Button) findViewById(R.id.back);
        btnCancle.setOnClickListener(this);
        btnSava.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        colorGroup.setOnCheckedChangeListener(this);
        skWordSize.setMax(20);
        skWordSize.setOnSeekBarChangeListener(this);
        cbEraser= (CheckBox) findViewById(R.id.eraser);
        cbEraser.setOnCheckedChangeListener(this);
        drawView= (ScaleDrawView) findViewById(R.id.drawview);
        getCamera();
        selectColor(colorGroup.getCheckedRadioButtonId());
    }


    private void getCamera() {
        Intent data = getIntent();
        String file = data.getStringExtra("photoPath");
        if (TextUtils.isEmpty(file)){
            finish();
        }
        files = new File(file);
        drawView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                drawView.setBitmapPath(files.getAbsolutePath());
                drawView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sava:
                Bitmap bitmap = drawView.getBitmap();
                if (bitmap != null) {
                    savaToSd(bitmap);
                    Intent intent=new Intent(files.getAbsolutePath());
                    setResult(RESULT_OK,intent);
                    finish();
                }
                break;
            case R.id.cancle:
                drawView.returnTrack();
                break;
            case R.id.back:
                drawView.clear();
                break;
            case R.id.nocancle:
                drawView.backTrack();
                break;
        }
    }

    /**
     * 把图片保存到sd卡
     *
     * @param bitmap
     */
    private void savaToSd(Bitmap bitmap) {
        if (files == null) {
            return;
        }
        savePath=getDiskFileDir(getApplicationContext())+"/"+"charco.png";
        File file = new File(savePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public  String getDiskFileDir(Context context) {
        StringBuilder sb = new StringBuilder();
        File file;
        String filePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            file = context.getExternalFilesDir(null);
            if (file != null) {
                sb.append(file.getPath());
            } else {
                sb.append(Environment.getExternalStorageDirectory().getPath())
                        .append("/Android/data/")
                        .append(context.getPackageName()).append("/files");
            }
            filePath = sb.toString();
        } else {
            file = context.getFilesDir();
            if (file != null) {
                sb.append(file.getPath());
            } else {
                sb.append(Environment.getDataDirectory().getPath())
                        .append("/data/").append(context.getPackageName())
                        .append("/files");
            }
            filePath = sb.toString();
        }
        File tempFile = new File(filePath);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        return filePath;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
            selectColor(checkedId);
    }

    /**
     * 选择画笔颜色
     *
     * @param checkedId
     */
    private void selectColor(int checkedId) {
        switch (checkedId) {
            case R.id.green:
                drawView.setColor(Color.GREEN);
                break;
            case R.id.blue:
                drawView.setColor(Color.BLUE);
                break;
            case R.id.yellow:
                drawView.setColor(Color.YELLOW);
                break;
            case R.id.red:
                drawView.setColor(Color.RED);
                break;
            case R.id.black:
                drawView.setColor(Color.BLACK);
                break;
            case R.id.white:
                drawView.setColor(Color.WHITE);
                break;
            default:
                break;
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            drawView.setPaintSize(progress+5);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected void onDestroy() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        drawView.setEraserMode(b);
    }
}
