package jp.techacademy.takuya.hatakeyama2.taskapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static jp.techacademy.takuya.hatakeyama2.taskapp.R.id.spinner;

public class TaskInputActivity extends AppCompatActivity {

    private EditText mEditText;
    private Button mRegistrationButton;
    private Button mDeleteButton;
    private Spinner mSpinner;
    private View.OnClickListener registrationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String categoryName = mEditText.getText().toString().trim();
            if (categoryName.length() == 0) {
                Toast.makeText(TaskInputActivity.this, "カテゴリーを入力してください。", Toast.LENGTH_SHORT).show();
            } else {
                Realm realm = Realm.getDefaultInstance();
                Category category = realm.where(Category.class).equalTo("name", categoryName).findFirst();
                if (category != null) {
                    Toast.makeText(TaskInputActivity.this, "既に登録されているカテゴリーと同じものは登録できません。", Toast.LENGTH_SHORT).show();
                } else {
                    realm.beginTransaction();

                    category = new Category();
                    RealmResults<Category> categoryRealmResults = realm.where(Category.class).findAll();
                    int identifier;
                    if (categoryRealmResults.max("id") != null) {
                        identifier = categoryRealmResults.max("id").intValue() + 1;
                    } else {
                        identifier = 0;
                    }

                    category.setId(identifier);
                    category.setName(categoryName);
                    realm.copyToRealmOrUpdate(category);
                    realm.commitTransaction();
                    realm.close();

                    Toast.makeText(TaskInputActivity.this, "新規カテゴリー： " + categoryName + "が追加されました。", Toast.LENGTH_SHORT).show();
                    mEditText.setText(null);

                }
            }

        }
    };
    private View.OnClickListener deleteOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_input);

        mEditText = (EditText) findViewById(R.id.category_edit_text_in_category);
        mRegistrationButton = (Button) findViewById(R.id.registration_button);
        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mSpinner = (Spinner) findViewById(spinner);

        mRegistrationButton.setOnClickListener(registrationOnClickListener);
        mDeleteButton.setOnClickListener(deleteOnClickListener);


        //Spinnerの内容（現在のカテゴリーテーブルの中身）を設定
        mSpinner.setAdapter(createSpinnerAdapter());
    }

    private SpinnerAdapter createSpinnerAdapter() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Category> categoriesRealmResults = realm.where(Category.class).findAll();
        ArrayAdapter<String> adapter;
        if (categoriesRealmResults != null) {
            ArrayList<Category> categoryArrayList=new ArrayList<>(realm.copyFromRealm(categoriesRealmResults));
            ArrayList<String> categoryNameArrayList=new ArrayList<>();
            for(Category category:categoryArrayList){
                categoryNameArrayList.add(category.getName());
            }
            adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,categoryNameArrayList);
        } else {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{""});
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        realm.close();
        return adapter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//オーバーフローアイコン内に項目が表示されます
//        MenuInflater menuInflater=new MenuInflater(this);
//        menuInflater.inflate(R.menu.menu_task_input,menu);

        //refreshアイコンが表示される
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_task_input, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                mSpinner.setAdapter(createSpinnerAdapter());
                break;
            default:
                break;
        }
        return true;
    }
}
