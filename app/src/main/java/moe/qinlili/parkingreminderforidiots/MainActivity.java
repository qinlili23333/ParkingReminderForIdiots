package moe.qinlili.parkingreminderforidiots;

import static android.net.ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.*;
import android.net.wifi.*;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;


public class MainActivity extends Activity {

    static boolean isRunning = false;
    String ssid;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRunning = true;
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("config", 0);
        ((EditText)findViewById(R.id.pkgname)).addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void afterTextChanged(Editable s) {
                String pkgname = s.toString();
                if (!pkgname.isEmpty() && !pkgname.contains(".")) {
                    ((TextView)MainActivity.this.findViewById(R.id.app_name)).setText("包名格式错误！");
                }else{
                    try {
                        ApplicationInfo app = MainActivity.this.getPackageManager().getApplicationInfo(pkgname, 0);
                        Drawable icon = MainActivity.this.getPackageManager().getApplicationIcon(app);
                        String name = (String) MainActivity.this.getPackageManager().getApplicationLabel(app);
                        ((TextView)MainActivity.this.findViewById(R.id.app_name)).setText("已安装："+name);
                        ((ImageView)MainActivity.this.findViewById(R.id.app_icon)).setImageDrawable(icon);
                    } catch (PackageManager.NameNotFoundException e) {
                        ((TextView)MainActivity.this.findViewById(R.id.app_name)).setText("应用不存在！");
                    }
                }
            }
        });
        readConfig();
        setService();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        isRunning = true;
        refreshBatteryWhitelist();
        checkLocationPermission();
        refreshSSID(null);
    }
    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.privacy_menu) {
            showPrivacy();
            return true;
        }
        if (id==R.id.source_menu){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/qinlili23333/ParkingReminderForIdiots"));
            this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPrivacy(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("隐私说明");
        alert.setCancelable(false);
        View view = LayoutInflater.from(this).inflate(R.layout.webview_dialog, null);

        WebView wv = view.findViewById(R.id.webview_dialog);
        wv.getSettings().setUseWideViewPort(false);
        wv.getSettings().setAlgorithmicDarkeningAllowed(true);
        wv.loadUrl("file:///android_asset/privacy.html");
        alert.setView(view);
        alert.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("agree_privacy", true);
                editor.apply();
                Toast.makeText(MainActivity.this, "已同意隐私政策，请继续按照提示允许权限", Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            }
        });
        alert.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        alert.show();
    }
    public void checkLocationPermission(){
        if(!pref.getBoolean("agree_privacy",false))
        {
            showPrivacy();
            return;
        }
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请允许位置权限！", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            if(checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "请允许后台位置！", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
            }
        }
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请允许通知权限！", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 2);
        }
        if (!Settings.canDrawOverlays(this)&&pref.getBoolean("enable_jump",false)) {
            Toast.makeText(this, "自动跳转需要悬浮窗权限！", Toast.LENGTH_SHORT).show();
            Intent intent = new  Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

    }

    protected void readConfig(){
        ((Switch)findViewById(R.id.enable_msg)).setChecked(pref.getBoolean("enable_msg",false));
        ((Switch)findViewById(R.id.enable_jump)).setChecked(pref.getBoolean("enable_jump",false));
        ((EditText)findViewById(R.id.ssid)).setText(pref.getString("ssid","UQ"));
        ((EditText)findViewById(R.id.pkgname)).setText(pref.getString("pkgname","air.com.cellopark.au"));
        ((RadioGroup)findViewById(R.id.cool_cd)).check(pref.getInt("cool_cd",R.id.cd_day));
    }

    public void saveConfig(View view) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("enable_msg", ((Switch)findViewById(R.id.enable_msg)).isChecked());
        editor.putBoolean("enable_jump", ((Switch)findViewById(R.id.enable_jump)).isChecked());
        editor.putString("ssid", ((EditText) findViewById(R.id.ssid)).getText().toString());
        editor.putString("pkgname", ((EditText) findViewById(R.id.pkgname)).getText().toString());
        editor.putInt("cool_cd", ((RadioGroup) findViewById(R.id.cool_cd)).getCheckedRadioButtonId());
        editor.apply();
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        setService();
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

    public void refreshSSID(View view) {
        if (!pref.getBoolean("agree_privacy", false)){
            return;
        }
        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build();
        final ConnectivityManager connectivityManager =
                getSystemService(ConnectivityManager.class);
        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
            @Override
            public void onAvailable(Network network) {}

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                WifiInfo wifiInfo = (WifiInfo) networkCapabilities.getTransportInfo();
                if(wifiInfo==null){
                    return;
                }
                ssid = wifiInfo.getSSID();
                ssid=ssid.substring(1,ssid.length()-1);
                ((TextView) findViewById(R.id.current_ssid)).setText("当前SSID：" + ssid);
            }
            // etc.
        };
        connectivityManager.requestNetwork(request, networkCallback); // For request
    }

    public void setSSID(View view){
        ((EditText)findViewById(R.id.ssid)).setText(ssid);
    }

    public void testOpen(View view){
        String pkgname = ((EditText) findViewById(R.id.pkgname)).getText().toString();
        if (pkgname.isEmpty() || !pkgname.contains(".")) {
            Toast.makeText(this, "包名格式错误！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkgname);
            if (intent == null) {
                Toast.makeText(this, "应用不存在！", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "打开失败！", Toast.LENGTH_SHORT).show();
        }
    }

    private void setService(){
        stopService();
        if(((Switch)findViewById(R.id.enable_msg)).isChecked()||((Switch)findViewById(R.id.enable_jump)).isChecked()){
            checkLocationPermission();
            startService();
            PackageManager pm  = getPackageManager();
            ComponentName componentName = new ComponentName(this, BootReceiver.class);
            pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }else{
            PackageManager pm  = getPackageManager();
            ComponentName componentName = new ComponentName(this, BootReceiver.class);
            pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
    private void startService(){
        Context context = getApplicationContext();
        Intent intent = new Intent(this, DaemonService.class);
        context.startForegroundService(intent);
    }

    private void stopService(){
        Intent intent = new Intent(this, DaemonService.class);
        stopService(intent);
    }

}