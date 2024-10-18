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
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readConfig();
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
        ((Switch)findViewById(R.id.enable_msg)).setChecked(pref.getBoolean("enable_msg",false));
        ((Switch)findViewById(R.id.enable_jump)).setChecked(pref.getBoolean("enable_jump",false));
        ((EditText)findViewById(R.id.ssid)).setText(pref.getString("ssid","UQ"));
        ((EditText)findViewById(R.id.pkgname)).setText(pref.getString("pkgname","air.com.cellopark.au"));
        ((RadioGroup)findViewById(R.id.cool_cd)).check(pref.getInt("cool_cd",R.id.cd_day));
    }

    public void saveConfig(View view) {
        SharedPreferences pref = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("enable_msg", ((Switch)findViewById(R.id.enable_msg)).isChecked());
        editor.putBoolean("enable_jump", ((Switch)findViewById(R.id.enable_jump)).isChecked());
        editor.putString("ssid", ((EditText) findViewById(R.id.ssid)).getText().toString());
        editor.putString("pkgname", ((EditText) findViewById(R.id.pkgname)).getText().toString());
        editor.putInt("cool_cd", ((RadioGroup) findViewById(R.id.cool_cd)).getCheckedRadioButtonId());
        editor.apply();
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
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