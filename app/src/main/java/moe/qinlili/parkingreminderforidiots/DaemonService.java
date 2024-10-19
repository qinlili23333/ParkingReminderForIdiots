package moe.qinlili.parkingreminderforidiots;

import android.app.*;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DaemonService extends Service {

    static String  CHANNEL_ID = "ReminderService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("PRFI-DaemonService", "onStartCommand");
        registerChannel();
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("停车提醒服务已启动")
                .setContentText("及时缴费，不做被罚钱的傻逼")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                .setSmallIcon(R.drawable.noti_icon);
        Notification notification = builder.build();

        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerChannel() {
        CharSequence name = "停车通知";
        String description = "及时缴费，不做被罚钱的傻逼";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
