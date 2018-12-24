package com.example.simplememoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;

public class NewTegakiMemo extends AppCompatActivity {

    private Realm realm;
    private long memoId; // メモのID
    private TextView memoDateView; // メモの日付
    private EditText memoTitleView; // メモのタイトル
    private String tImagePath; // 手書きしたメモへのファイルパス
    private Bitmap memoBitmap;
    private ImageView memoImageView; // 手書きしたメモへのファイルパスから取り出した画像を表示させる

    private final int REQUEST_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tegaki_memo);

        // メモをデータベースに保存できるようにする
        realm = Realm.getDefaultInstance();

        memoDateView = (TextView) findViewById(R.id.date);
        memoTitleView = (EditText) findViewById(R.id.title);

        // データを取り出す
        if (getIntent() != null) {
            // インテントで渡したデータを取り出す
            memoId = getIntent().getLongExtra("ID", -1);

            // RealmのデータベースからIDに紐づいたメモのデータを取り出す
            TextMemo tegakiMemo = realm.where(TextMemo.class)
                    .equalTo("id", memoId)
                    .findFirst();

            // 日付データ
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String formatDate = sdf.format(tegakiMemo.date);

            // 各データをそれぞれのビューにセットする
            memoDateView.setText(formatDate);
            memoTitleView.setText(tegakiMemo.title);
        }

        // 入力されたデータを更新する
        // タイトル
        memoTitleView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        TextMemo memo = realm.where(TextMemo.class).equalTo("id", memoId).findFirst();
                        memo.title = s.toString();
                    }
                });
            }
        });

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission();
                } else {
                    memoSave();
                }
            }
        });
    }

    public void memoSave() {

        // 手書きしたメモを画像として端末のファイルへ保存する
        TegakiPaintView savePaintView = (TegakiPaintView) findViewById(R.id.tegakiView);
        Date saveDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        // 撮りたいViewのキャッシュを取得する
        savePaintView.setDrawingCacheEnabled(true); // キャッシュを取得する設定にする
        Bitmap cache = savePaintView.getDrawingCache();
        Bitmap screenShot = Bitmap.createBitmap(cache);

        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/tagakimemo/");
        if (!file.exists()) {
            file.mkdirs();
        }
        final String saveFileName = file.getPath() + sdf.format(saveDate) + ".png";

        try {
            FileOutputStream fos = new FileOutputStream(saveFileName);
            screenShot.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "保存できました", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error", "" + e.toString());
        }

        // データベースへ画像へのファイルパスを保存する
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TextMemo memo = realm.where(TextMemo.class).equalTo("id", memoId).findFirst();
                memo.content = saveFileName;
            }
        });

        // 保存したらメモ一覧ページへ戻る
        Intent intent = new Intent(NewTegakiMemo.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            memoSave();
        } else {
            requestLocalPermission();
        }
    }

    public void requestLocalPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(NewTegakiMemo.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            Toast.makeText(this, "許可して", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(NewTegakiMemo.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                memoSave();
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
