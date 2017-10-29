package jp.techacademy.takuya.hatakeyama2.taskapp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Takuya on 2017/10/22.
 */

public class Category extends RealmObject {

    //データ登録の際に条件をしぼっているので、name columnも一意で主キーになりうるが、課題に沿ってidを追加している
    //実態はカテゴリー名が主キーになっていて違和感……
    @PrimaryKey
    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
