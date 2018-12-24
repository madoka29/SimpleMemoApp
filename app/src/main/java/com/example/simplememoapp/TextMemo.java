package com.example.simplememoapp;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Realmのモデルクラス
// データベースを作成するクラス
// RealmObjectを継承することでモデルクラスを定義できる
public class TextMemo extends RealmObject {
    @PrimaryKey
    public long id;

    public Date date;
    public String title;
    public String content;
    public String memoKind;
}

