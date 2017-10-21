package jp.techacademy.takuya.hatakeyama2.taskapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import io.realm.Realm;

public class TaskAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ONE_ID = "channel_1";
    private static final String CHANNEL_ONE_NAME = "Channel One";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Android8対応のためのコード Android8では別途Channelが必要になる
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = createNotificationChannel();
            Task task = getCopiedTask(intent);

            Notification.Builder builder = new Notification.Builder(context, CHANNEL_ONE_ID);
            builder.setSmallIcon(R.drawable.small_icon);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.large_icon));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setContentTitle(task.getTitle());
            builder.setContentText(task.getContents());
            builder.setContentIntent(createPendingIntent(context));

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(task.getId(), builder.build());

        } else {
            //sampleコードのままだと通知が表示される際に、TaskAppが立ち上がっていないとアプリが落ちる。
            //途中でRealmをcloseしてるのにRealmのデータベース内を直接参照しにいっているためなのかな？
            // そのためインスタンスを生成してその情報を基に登録するように変更。
            Task task = getCopiedTask(intent);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.small_icon);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.large_icon));
            builder.setWhen(System.currentTimeMillis());
            builder.setDefaults(Notification.DEFAULT_ALL);
            builder.setAutoCancel(true);
            builder.setTicker(task.getTitle());
            builder.setContentTitle(task.getTitle());
            builder.setContentText(task.getContents());
            builder.setContentIntent(createPendingIntent(context));

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(task.getId(), builder.build());
        }
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent startAppIntent = new Intent(context, MainActivity.class);
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        return PendingIntent.getActivity(context, 0, startAppIntent, 0);
    }

    private Task getCopiedTask(Intent intent) {
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        Realm realm = Realm.getDefaultInstance();
        Task task = realm.copyFromRealm(realm.where(Task.class).equalTo("id", taskId).findFirst());
        realm.close();
        return task;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        return notificationChannel;
    }
}