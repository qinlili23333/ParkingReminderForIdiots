package moe.qinlili.parkingreminderforidiots;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JumpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("PRFI", "onReceive");
        SharedPreferences pref = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = pref.edit();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        editor.putString("last_date", currentDate);
        editor.apply();
        DaemonService.updateNotification();
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pref.getString("pkgname", "air.com.cellopark.au"));
            if (intent == null) {
                Toast.makeText(this, "应用不存在！", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "打开失败！", Toast.LENGTH_SHORT).show();
        }
    finishAndRemoveTask();
    }
}
