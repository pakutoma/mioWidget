package pakutoma.iijmiocouponwidget;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Intent intent = getIntent();
        if (intent != null) {
            String data = intent.getDataString();
            if (data != null) {
                Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse(data);
                Pattern p = Pattern.compile("(?<=access_token=).+?(?=&)");
                Matcher m = p.matcher(uri.getFragment());
                if(m.find()) {
                    String token = m.group();
                    Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
                    SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("X-IIJmio-Authorization",token);
                    editor.putBoolean("has_token",true);
                    editor.apply();
                }
            }
        }
        finish();
    }
}
