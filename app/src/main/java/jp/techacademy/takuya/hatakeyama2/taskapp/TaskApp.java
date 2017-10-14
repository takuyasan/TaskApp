package jp.techacademy.takuya.hatakeyama2.taskapp;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;

/**
 * Created by Takuya on 2017/10/13.
 */

public class TaskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
