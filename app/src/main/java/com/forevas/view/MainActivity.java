package com.forevas.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by zhuchenchen on 2018/3/5 0005.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView ivCrop;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_crop).setOnClickListener(this);
        ivCrop=findViewById(R.id.iv_crop);
    }

    @Override
    public void onClick(View v) {
//        CropImageActivity.cropImage(this,"/sdcard/test.jpg",0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==0){
                String path = data.getStringExtra("path");
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ivCrop.setImageBitmap(bitmap);
            }
        }
    }
}
