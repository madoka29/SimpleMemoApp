package com.example.simplememoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MojiRead extends AppCompatActivity {

    // カメラ（遷移先）から返しを受けたときの値。onActivityResultで受け取る
    private final static int RESULT_CAMERA = 1;

    private final static int REQUEST_PERMISSION = 2;

    private String filePath;
    private Uri cameraUri;

    private ImageView imageView;
    private TextView readText;

    // 画像から文字読みこむ時に必要なもの
    // デフォルト言語を決める。
    // "eng+jpn"の場合英語がメイン、日本語サブとなる
    static final String DEFAULT_LANGUAGE = "eng+jpn";

    String filepath;
    Bitmap bitmap;
    TessBaseAPI tessBaseAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moji_read);

        // カメラで撮った画像がセットされる
        imageView = (ImageView) findViewById(R.id.imageView);
        readText = (TextView) findViewById(R.id.readText);

        // カメラを起動させるボタン
        Button cameraButton = (Button) findViewById(R.id.cameraButton);
        // カメラボタンを押したときの動作
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    // Android 6, API 23以上ではパーミッションを確認しなければならない。パーミッションの確認へ
                    // パーミッションチェックメソッドへ
                    checkPermission();
                } else {
                    // カメラ起動インテント
                    cameraIntent();
                }
            }
        });

        // 戻るボタン
        Button back = (Button) findViewById(R.id.save);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 画像から文字を読みこむボタン
        Button mojiReadButton = (Button) findViewById(R.id.mojiRead);
        mojiReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap != null) {

                    filepath = getFilesDir() + "/tesseract/"; // Path: ~内部ストレージの絶対パス/tesseract/
                    tessBaseAPI = new TessBaseAPI();

                    // checkFileメソッドを呼び出す
                    checkFile(new File(filepath + "tessdata/")); // Path: ~内部ストレージの絶対パス/tesseract/tessdata/

                    // tessBaseAPIの初期化
                    tessBaseAPI.init(filepath, DEFAULT_LANGUAGE);

                    // tessBaseAPIに画像をセットして文字認識させる
                    tessBaseAPI.setImage(bitmap);

                    // 文字認識の結果でUTF8テキストを取得する
                    String result = tessBaseAPI.getUTF8Text();

                    readText.setText(result);
                    readText.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    // Runtime Permission check
    private void checkPermission() {
        // support v4ライブラリに含まれるActivityCompatを使うことで、パーミッションチェックを行える
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // すでに許可されている場合はカメラ起動へ
            cameraIntent();
        } else {
            // 拒否していた場合は許可をリクエストする
            requestPermission();
        }
    }

    private void requestPermission() {
        // ActivityCompatのshouldShowRequestPermissionRationable:許可ダイアログの再表示判定。永続的に不許可設定の場合はfalseが返る
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )) {
            // 許可された場合
            // requestPermissions：権限の許可ダイアログを表示する
            ActivityCompat.requestPermissions(
                    MojiRead.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION
            );
        } else {
            // 許可されなかった場合
            Toast toast = Toast.makeText(
                    this,
                    "許可されないとアプリが実行できません",
                    Toast.LENGTH_SHORT
            );
            toast.show();

            // 権限の許可ダイアログを表示させる
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION
            );
        }
    }

    // パーミッションリクエストの結果を受け取る
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 使用が許可された場合
                cameraIntent();
            } else {
                // 拒否された時
                Toast toast = Toast.makeText(
                        this,
                        "どうもできません",
                        Toast.LENGTH_LONG
                );
                toast.show();
            }
        }
    }

    // カメラの起動
    private void cameraIntent() {
        // 保存先のフォルダをカメラに指定する
        File cameraFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");

        // 保存ファイル名を作成
        String fileName = new SimpleDateFormat("ddHHmmss", Locale.JAPAN).format(new java.util.Date());
        filePath = String.format("%s%s.jpg", cameraFolder.getPath(), fileName);

        // 写真を撮ってキャプチャーする画像のファイルパス
        File cameraFile = new File(filePath);
        cameraUri = FileProvider.getUriForFile(
                MojiRead.this,
                getApplicationContext().getPackageName() + ".fileprovider",
                cameraFile
        );

        // インテントを渡す
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, RESULT_CAMERA);
    }

    // startActivityのインテントを渡した後の結果を受け取る
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == RESULT_CAMERA) {
            if (cameraUri != null) {
                try {
                    // イメージのuriがあればimageViewへとセットする
                    imageView.setImageURI(cameraUri);
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cameraUri);
                    imageView.setVisibility(View.VISIBLE);
                    readText.setVisibility(View.INVISIBLE);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 画像から文字を読み込むときに必要なもの
    // checkFileメソッド： fileの有無を確認する
    private void checkFile(File file) {
        if (!file.exists() && file.mkdirs()) {
            // copyFilesメソッドを呼び出す
            String jpnFilePath = filepath + "/tessdata/jpn.traineddata";
            copyFiles(jpnFilePath, "jpn");

            String engFilePath = filepath + "/tessdata/eng.traineddata";
            copyFiles(engFilePath, "eng");
        }

        if (file.exists()) {

            // 日本語データを内部ストレージに保存
            String jpnFilePath = filepath + "/tessdata/jpn.traineddata";
            File jpnFile = new File(jpnFilePath);
            if (!jpnFile.exists()) {
                copyFiles(jpnFilePath, "jpn");
            }

            // 英語データを内部ストレージに保存
            String engFilePath = filepath + "/tessdata/eng.traineddata";
            File engFile = new File(engFilePath);
            if (!engFile.exists()) {
                copyFiles(engFilePath, "eng");
            }
        }
    }

    // copyFilesメソッド
    private void copyFiles(String lang, String fileLang) {
        try {
            String datapath = lang;

            // InputStream: 入力を抽象化する。ファイルやら何やらよくわからないものをとりあえず入力処理できるようにする。
            // 機械と繋がる入力用の道を作るみたいな感じ
            InputStream inputStream = null;
            if (fileLang.equals("jpn")) {
                inputStream = getAssets().open("tessdata/jpn.traineddata");
            } else if (fileLang.equals("eng")) {
                inputStream = getAssets().open("tessdata/eng.traineddata");
            }
            // OutputStream: 出力を抽象化する。ファイルやら何やらよくわからないものをとりあえず出力処理できるようにする
            // 機械と繋がる出力用の道を作るみたいな感じ
            OutputStream outputStream = new FileOutputStream(datapath);

            byte[] buffer = new byte[1024];
            int read;

            // InputStream.read(buffer)：readメソッド。引数のbyte配列にデータを読み込んで格納する
            while ((read = inputStream.read(buffer)) != -1) {
                // OutputStream.write(byte[] b, int off, int len)
                // 指定されたバイト配列のオフセット位置offから始まるreadバイトを出力ストリームに書き込む
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            File file = new File(datapath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
