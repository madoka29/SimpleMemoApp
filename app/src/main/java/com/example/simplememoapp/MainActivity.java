package com.example.simplememoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // リストビューを使えるようにする
        realm = Realm.getDefaultInstance();
        final ListView listView = (ListView) findViewById(R.id.listView);

        // アダプターと連携
        RealmResults<TextMemo> textMemos = realm.where(TextMemo.class).findAll();
        TextMemoAdapter adapter = new TextMemoAdapter(textMemos);

        listView.setAdapter(adapter);

        // 文字読み込みページへと遷移
        Button mojiReadPage = findViewById(R.id.textMemo);
        mojiReadPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, MojiRead.class);
                startActivity(intent);
            }
        });

        // 新規作成ページへと遷移
        Button newPage = findViewById(R.id.newButton);
        newPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, NewAll.class);
                startActivity(intent);
            }
        });

        // itemがクリックされたときのイベント
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextMemoAdapter adapter = (TextMemoAdapter) listView.getAdapter();
                TextMemo textMemo = adapter.getItem(position);
                if (textMemo.memoKind.equals("text")) {
                    Intent intent = new Intent(MainActivity.this, NewTextMemo.class);
                    intent.putExtra("ID", textMemo.id);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, ShowTegakiMemo.class);
                    intent.putExtra("ID", textMemo.id);
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextMemoAdapter adapter = (TextMemoAdapter) listView.getAdapter();
                final TextMemo textMemo = adapter.getItem(position);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        textMemo.deleteFromRealm();
                    }
                });
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
