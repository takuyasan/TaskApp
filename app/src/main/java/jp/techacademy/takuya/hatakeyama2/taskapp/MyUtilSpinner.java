package jp.techacademy.takuya.hatakeyama2.taskapp;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Takuya on 2017/10/29.
 */

public class MyUtilSpinner {
    public static SpinnerAdapter createSpinnerAdapter(Context context) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Category> categoriesRealmResults = realm.where(Category.class).findAll();
        ArrayAdapter<String> adapter;
        if (categoriesRealmResults != null) {
            ArrayList<Category> categoryArrayList = new ArrayList<>(realm.copyFromRealm(categoriesRealmResults));
            ArrayList<String> categoryNameArrayList = new ArrayList<>();
            for (Category category : categoryArrayList) {
                categoryNameArrayList.add(category.getName());
            }
            adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryNameArrayList);
        } else {
            adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new String[]{""});
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        realm.close();
        return adapter;
    }
}
