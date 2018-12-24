package com.example.simplememoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;

public class NewTextMemo extends AppCompatActivity {

    private Realm realm;

    private long memoId; // メモのID
    private Date memoDate; // メモの日付
    private String memoTitle; // メモのタイトル
    private String memoContent; // メモの内容

    private TextView memoDateView; // メモの日付
    private EditText memoTitleView; // メモのタイトル
    private EditText memoContentView; // メモの内容

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_text_memo);

        memoDateView = (TextView) findViewById(R.id.date);
        memoTitleView = (EditText) findViewById(R.id.title);
        memoContentView = (EditText) findViewById(R.id.content);

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

            // 各データをそれぞれのビューにセットする
            memoDateView.setText(formatDate);
            memoTitleView.setText(textMemo.title);
            memoContentView.setText(textMemo.content);
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

        // 入力されたデータを更新する
        // タイトル
        memoContentView.addTextChangedListener(new TextWatcher() {
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
                        memo.content = s.toString();
                    }
                });
            }
        });

        Button register = (Button) findViewById(R.id.save);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public void register() {
        Intent intent = new Intent(NewTextMemo.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
