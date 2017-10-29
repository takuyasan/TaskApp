package jp.techacademy.takuya.hatakeyama2.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.takuya.hatakeyama2.taskapp.TASK";

    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private Spinner spinner;
    private TextView filterTextView;
    private static boolean flag = false; //スピナーフィルターをセットする際にsetOnItemClickListenerが一度呼ばれてしまうため

    MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);
        filterTextView = (TextView) findViewById(R.id.main_current_category_name_text_view);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        reloadListView();
    }
//別アクティビティでSearchを行おうとした実験の残り
//    @Override
//    protected void onNewIntent(Intent intent) {
//        Log.d("Test", "mainOnNewIntent");
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String newText = intent.getStringExtra(SearchManager.QUERY);
//            RealmResults<Task> realmResults = mRealm.where(Task.class).equalTo("category", newText).findAll();
//            mTaskAdapter.setTaskList(mRealm.copyFromRealm(realmResults));
//            // TaskのListView用のアダプタに渡す
//            mListView.setAdapter(mTaskAdapter);
//            // 表示を更新するために、アダプターにデータが変更されたことを知らせる
//            mTaskAdapter.notifyDataSetChanged();
//
//            Log.d("Test", "change");
//        }
//    }

    private void reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAllSorted("date", Sort.DESCENDING);
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);

//        このコメントのリスナーを設定しようとするとエラーが発生します。
//        おそらくAndroidSDKのバグ？あるいは属性の設定が悪いのかな？原因不明……
//        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                Log.d("Test","Expand");
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                Log.d("Test","Collapse");
//                return true;
//            }
//        });
//        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem menuItem) {
//                Log.d("Test","Expand");
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
//                Log.d("Test","Collapse");
//                return false;
//            }
//        });

        SearchView searchView =
                (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                displaySearchResult(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                displaySearchResult(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                reloadListView();
                return false;
            }
        });

        MenuItem spinnerMenuItem = menu.findItem(R.id.category_search_spinner);
        spinner = (Spinner) spinnerMenuItem.getActionView();
        spinner.setAdapter(MyUtilSpinner.createSpinnerAdapter(this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (flag) {
                    String filterText = parent.getItemAtPosition(position).toString();
                    filterTextView.setText(filterText);
                    RealmResults<Task> realmResults = mRealm.where(Task.class).equalTo("category", filterText).findAll();
                    mTaskAdapter.setTaskList(mRealm.copyFromRealm(realmResults));
                    mListView.setAdapter(mTaskAdapter);
                    mTaskAdapter.notifyDataSetChanged();
                }
                flag = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//別アクティビティでSearchを行おうとした実験の残り
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(new ComponentName(this,SearchResultsActivity.class)));
//        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        return true;

    }


    private void displaySearchResult(String query) {
        if (query.length() != 0) {
            RealmResults<Task> realmResults = mRealm.where(Task.class).like("category", "*" + query + "*", Case.INSENSITIVE).findAll();
            mTaskAdapter.setTaskList(mRealm.copyFromRealm(realmResults));
            // TaskのListView用のアダプタに渡す
            mListView.setAdapter(mTaskAdapter);
            // 表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged();
        } else {
            reloadListView();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                reloadListView();
                flag = false;
                spinner.setAdapter(MyUtilSpinner.createSpinnerAdapter(this));
                filterTextView.setText(null);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}