package com.charco.zoomscrawlview;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

/**
 * Created by admin on 2016/7/30.
 */
public class SplashActivity extends AppCompatActivity {
    private File photoFile;
    private static final int ACTION_PICK=0x11;
    private static final int ACTION_CAPTURE=0x22;
    private static final int RESPONSE_CODE=0x33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    public void onAlbum(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ACTION_PICK);
    }
    public void onCamera(View v){
        if (createFile()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(intent, ACTION_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK){

            switch (requestCode){
                case ACTION_CAPTURE:
                    Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                    Log.e("charco","photoFile "+photoFile);
                    intent.putExtra("photoPath",photoFile.getAbsolutePath());
                    SplashActivity.this.startActivityForResult(intent,RESPONSE_CODE);
                    break;
                case ACTION_PICK:
                    Uri uri1 = data.getData();
                    Cursor cursor = this.getContentResolver().query(uri1, new String[]{"_data"},null, null, null);
                    if(cursor.moveToFirst()){
                        Intent intent1=new Intent(SplashActivity.this,MainActivity.class);
                        String otherfile = cursor.getString(0);
                        intent1.putExtra("photoPath",otherfile);
                        Log.e("charco","otherfile "+otherfile);
                        SplashActivity.this.startActivityForResult(intent1,RESPONSE_CODE);
                    }
                    break;
                case RESPONSE_CODE:
                    String action = data.getAction();
                    Toast.makeText(SplashActivity.this, "保存地址为 "+action, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean createFile() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Toast.makeText(this, "SD卡不可用", Toast.LENGTH_SHORT).show();
            return false;
        }
        String fileDir = Environment.getExternalStorageDirectory().getPath() + "/example/";
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = new Date().getTime() + ".jpg";
        String originCameraImgPath = fileDir + fileName;
        photoFile = new File(originCameraImgPath);
        Log.e("charco","photoFile "+photoFile);
        return true;
    }
}
