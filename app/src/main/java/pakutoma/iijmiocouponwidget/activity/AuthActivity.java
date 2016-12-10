package pakutoma.iijmiocouponwidget.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pakutoma.iijmiocouponwidget.R;
import pakutoma.iijmiocouponwidget.service.GetTraffic;
import pakutoma.iijmiocouponwidget.service.InitialSetting;

public class AuthActivity extends Activity {

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
                    Intent initialSettingIntent = new Intent(getApplicationContext(), InitialSetting.class);
                    getApplicationContext().startService(initialSettingIntent);
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                    Toast.makeText(this, "認証が完了しました。", Toast.LENGTH_SHORT).show();
                }
            }
        }
        finish();
    }
}
