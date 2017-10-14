package jp.techacademy.takuya.hatakeyama2.taskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Takuya on 2017/10/13.
 */

public class Task extends RealmObject implements Serializable {

    // id をプライマリーキーとして設定
    @PrimaryKey
    private int id;

    private String title; // タイトル
    private String contents; // 内容
    private Date date; // 日時

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
