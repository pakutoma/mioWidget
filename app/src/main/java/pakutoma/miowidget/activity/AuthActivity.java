package pakutoma.miowidget.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pakutoma.miowidget.R;
import pakutoma.miowidget.service.GetTraffic;
import pakutoma.miowidget.widget.SwitchWidget;

public class AuthActivity extends Activity {
    private static final String ACTION_WIDGET_ENABLE = "pakutoma.miowidget.widget.SwitchWidget.ACTION_WIDGET_ENABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Intent intent = getIntent();
        if (intent != null) {
            String data = intent.getDataString();
            if (data != null) {
                Uri uri = Uri.parse(data);
                Pattern p = Pattern.compile("(?<=access_token=).+?(?=&)");
                Matcher m = p.matcher(uri.getFragment());
                if(m.find()) {
                    String token = m.group();
                    SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("X-IIJmio-Authorization",token);
                    editor.putBoolean("has_token",true);
                    editor.apply();
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);

                    Intent getTrafficIntent = new Intent(this, GetTraffic.class);
                    this.startService(getTrafficIntent);

                    Toast.makeText(this, "認証が完了しました。", Toast.LENGTH_SHORT).show();
                    setWidgetButtonIntent();
                }
            }
        }
        finish();
    }

    private void setWidgetButtonIntent() {
        Intent intent = new Intent(getApplicationContext(), SwitchWidget.class);
        intent.setAction(ACTION_WIDGET_ENABLE);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        final long interval = 1000;
        final long nextAlarm = System.currentTimeMillis() + interval;
        am.set(AlarmManager.RTC, nextAlarm, sender);
    }


}
