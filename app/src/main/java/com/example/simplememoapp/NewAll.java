package com.example.simplememoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.Date;

import io.realm.Realm;

public class NewAll extends AppCompatActivity {

    private Realm realm;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_all);

        // realm = Realm.getDefaultInstance();

        // 普通にメモするページへ
        Button textMemoButton = (Button) findViewById(R.id.textMemo);
        textMemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long[] newId = {0}; // 新しいメモのid作成
                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Number max = realm.where(TextMemo.class).max("id");
                        newId[0] = 0;
                        if (max != null) {
                            newId[0] = max.longValue() + 1; // 新しいIDは一番新しいメモのIDの次の番号となる
                        }

                        // TextMemoデータベースで新たにオブジェクトを作る
                        TextMemo textMemo = realm.createObject(TextMemo.class, newId[0]);
                        textMemo.date = new Date();
                        textMemo.title = "";
                        textMemo.content = "";
                        textMemo.memoKind = "text";
                    }
                });

                // テキストメモ追加のページへ遷移するために遷移先へID情報を渡すようにする
                intent = new Intent(NewAll.this, NewTextMemo.class);
                intent.putExtra("ID", newId[0]);
                startActivity(intent);
            }
        });

        // 手書きでメモするページへ
        Button tegakiMemoButton = (Button) findViewById(R.id.tegakiMemo);
        tegakiMemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long[] newId = {0}; // 新しいメモのid作成
                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Number max = realm.where(TextMemo.class).max("id");
                        newId[0] = 0;
                        if (max != null) {
                            newId[0] = max.longValue() + 1; // 新しいIDは一番新しいメモのIDの次の番号となる
                        }

                        // TextMemoデータベースで新たにオブジェクトを作る
                        TextMemo tegakiMemo = realm.createObject(TextMemo.class, newId[0]);
                        tegakiMemo.date = new Date();
                        tegakiMemo.title = "";
                        tegakiMemo.content = "";
                        tegakiMemo.memoKind = "tegaki";
                    }
                });

                // テキストメモ追加のページへ遷移するために遷移先へID情報を渡すようにする
                intent = new Intent(NewAll.this, NewTegakiMemo.class);
                intent.putExtra("ID", newId[0]);
                startActivity(intent);
            }
        });
    }
}
