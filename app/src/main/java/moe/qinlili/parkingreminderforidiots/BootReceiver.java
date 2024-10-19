package moe.qinlili.parkingreminderforidiots;

import android.content.BroadcastReceiver;
import android.content.*;
import android.widget.Switch;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences pref = context.getSharedPreferences("config", 0);
            if(pref.getBoolean("enable_msg", false)||pref.getBoolean("enable_jump", false)){
                Intent service = new Intent(context, DaemonService.class);
                context.startForegroundService(service);
            }

        }
    }
}
