package moe.qinlili.parkingreminderforidiots;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshBatteryWhitelist();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        refreshBatteryWhitelist();
    }

    protected void readConfig(){
        SharedPreferences pref= getSharedPreferences("config",0);

    }

    @SuppressLint("BatteryLife")
    public void requestBatteryWhitelist(View view){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        Toast.makeText(this, "请点击允许", Toast.LENGTH_SHORT).show();
        this.startActivity(intent);
    }
    private void refreshBatteryWhitelist(){
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(getPackageName())){
            Button battbtn=findViewById(R.id.battery_opt);
            battbtn.setEnabled(false);
            battbtn.setText("已启用后台保活");
        }
    }
}