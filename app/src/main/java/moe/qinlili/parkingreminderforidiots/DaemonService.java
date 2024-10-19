package moe.qinlili.parkingreminderforidiots;

import static android.net.ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.*;
import android.net.wifi.WifiInfo;
import android.os.IBinder;
import android.util.Log;

public class DaemonService extends Service {

    static String  CHANNEL_SERVICE = "ReminderService";
    static String CHANNEL_MSG = "ReminderMsg";
    String target_ssid;
    String target_app;
    String current_ssid;
    Notification notification;
    SharedPreferences pref;

    Context myContext;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("PRFI-DaemonService", "onStartCommand");
        registerChannel();
        myContext = this;
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_SERVICE)
                .setContentTitle("停车提醒服务已启动")
                .setContentText("及时缴费，不做被罚钱的傻逼")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                .setSmallIcon(R.drawable.noti_icon);
        notification = builder.build();
        pref= getSharedPreferences("config",0);
        target_ssid=pref.getString("ssid","UQ");
        target_app=pref.getString("pkgname","air.com.cellopark.au");
        listenNetwork();
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_SERVICE, "前台服务", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("保持服务运行");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        channel = new NotificationChannel(CHANNEL_MSG, "缴费提醒", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("及时缴费，不做被罚钱的傻逼");
        notificationManager.createNotificationChannel(channel);
    }

    private void listenNetwork() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                WifiInfo wifiInfo = (WifiInfo) networkCapabilities.getTransportInfo();
                if(wifiInfo==null){
                    return;
                }
                String ssid = wifiInfo.getSSID();
                ssid=ssid.substring(1,ssid.length()-1);
                Log.w("PRFI","当前SSID：" + ssid);
                if(current_ssid!=null && current_ssid.equals(ssid)){
                    return;
                }
                current_ssid=ssid;
                if(ssid.equals(target_ssid)){
                    Log.w("PRFI","已连接到目标SSID");
                    if(pref.getBoolean("enable_msg",false)){
                        Notification.Builder builder = new Notification.Builder(myContext, CHANNEL_MSG)
                                .setContentTitle("你已到达目标停车场")
                                .setContentText("点击此处立即缴费")
                                .setContentIntent(PendingIntent.getActivity(myContext, 0, getPackageManager().getLaunchIntentForPackage(target_app), PendingIntent.FLAG_IMMUTABLE))
                                .setSmallIcon(R.drawable.noti_icon);
                        Notification noti = builder.build();
                        noti.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.notify(2, noti);
                    }
                    if(pref.getBoolean("enable_jump",false)) {
                        try {
                            Intent intent = getPackageManager().getLaunchIntentForPackage(target_app);
                            startActivity(intent);
                        } catch (Exception e) {
                        }
                    }
                }else{
                    Log.w("PRFI","未连接到目标SSID");
                    if(pref.getBoolean("enable_msg",false)){
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.cancel(2);
                    }
                }
            }


        });
    }
}
