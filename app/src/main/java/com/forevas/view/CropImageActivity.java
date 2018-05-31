package com.forevas.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片裁剪页面
 */
public class CropImageActivity extends Activity implements View.OnClickListener {
    public static final int TYPE_SDCARD = 0;
    public static final int TYPE_RESOURCE = 1;

    public static void cropImage(Activity activity, String srcPath, int requestCode) {
        Intent intent = new Intent(activity, CropImageActivity.class);
        intent.putExtra("type", TYPE_SDCARD);
        intent.putExtra("path", srcPath);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void cropImage(Fragment fragment, String srcPath, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), CropImageActivity.class);
        intent.putExtra("type", TYPE_SDCARD);
        intent.putExtra("path", srcPath);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void cropResImage(Activity activity, int resId, int requestCode) {
        Intent intent = new Intent(activity, CropImageActivity.class);
        intent.putExtra("type", TYPE_RESOURCE);
        intent.putExtra("resId", resId);
        activity.startActivityForResult(intent, requestCode);
    }

    CropImageView imageView;
    TextView tvCancel;
    TextView tvRotate;
    TextView tvCrop;
    TextView tvFinish;
    int type;
    String path;
    int resId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        imageView = findViewById(R.id.imageView);
        tvCancel = findViewById(R.id.tv_cancel);
        tvRotate = findViewById(R.id.tv_rotate);
        tvCrop = findViewById(R.id.tv_crop);
        tvFinish = findViewById(R.id.tv_finish);

        tvCancel.setOnClickListener(this);
        tvRotate.setOnClickListener(this);
        tvCrop.setOnClickListener(this);
        tvFinish.setOnClickListener(this);

        Intent intent = getIntent();
        type = intent.getIntExtra("type", -1);
        if (type == TYPE_SDCARD) {
            path = intent.getStringExtra("path");
            if (new File(path).exists()) {
                imageView.setImagePath(path);
            } else {
                Toast.makeText(this, "无效的图片路径", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (type == TYPE_RESOURCE) {
            resId = intent.getIntExtra("resId", -1);
            imageView.setImageRes(resId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_rotate:
                imageView.rotate();
                break;
            case R.id.tv_crop:
                imageView.crop(true);
                break;
            case R.id.tv_finish:
                Bitmap tempBitmap = imageView.crop(false);
                String path = "/sdcard/temp_crop.png";
                saveBitmap(tempBitmap, path);
                Intent intent = new Intent();
                intent.putExtra("path", path);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
        }
    }

    public void saveBitmap(Bitmap bitmap, String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }

        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (fOut != null)
                    fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
