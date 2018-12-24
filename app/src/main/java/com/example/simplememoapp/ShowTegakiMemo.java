package com.example.simplememoapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;

public class ShowTegakiMemo extends AppCompatActivity {
    private Realm realm;

    private long memoId; // メモのID
    private Date memoDate; // メモの日付
    private String memoTitle; // メモのタイトル
    private String memoContent; // メモの内容

    private TextView memoDateView; // メモの日付
    private TextView memoTitleView; // メモのタイトル
    private ImageView memoTegakiView; // メモの内容
    private String tegakiImagePath;

    private final int REQUEST_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tegaki_memo);

        memoDateView = (TextView) findViewById(R.id.date);
        memoTitleView = (TextView) findViewById(R.id.title);
        memoTegakiView = (ImageView) findViewById(R.id.tegakiImage);

        realm = Realm.getDefaultInstance();

        // データを取り出す
        if (getIntent() != null) {
            // インテントで渡したデータを取り出す
            memoId = getIntent().getLongExtra("ID", -1);

            // RealmのデータベースからIDに紐づいたメモのデータを取り出す
            TextMemo textMemo = realm.where(TextMemo.class)
                    .equalTo("id", memoId)
                    .findFirst();

            // 日付データ
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String formatDate = sdf.format(textMemo.date);

            // 日付とデータをそれぞれのビューにセットする
            memoDateView.setText(formatDate);
            memoTitleView.setText(textMemo.title);

            tegakiImagePath = textMemo.content;

            if (Build.VERSION.SDK_INT >= 23) {
                checkPermission();
            } else {
                hyouji(tegakiImagePath);
            }
        }

        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void hyouji(String filepath) {
        File file = new File(filepath);
        try (InputStream inputStream = new FileInputStream(file)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            memoTegakiView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            hyouji(tegakiImagePath);
        } else {
            requestLocalPermission();
        }
    }

    public void requestLocalPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(ShowTegakiMemo.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            Toast.makeText(this, "許可して", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(ShowTegakiMemo.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hyouji(tegakiImagePath);
            } else {
                Toast.makeText(this, "なんもない", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
