package pakutoma.miowidget.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import pakutoma.miowidget.R;
import pakutoma.miowidget.exception.NotFoundValidTokenException;
import pakutoma.miowidget.utility.CouponAPI;
import pakutoma.miowidget.widget.SwitchWidget;

/**
 * Created by PAKUTOMA on 2017/03/20.
 */

public class OpenBrowserActivity extends Activity {
    private static final String ACTION_WIDGET_ENABLE = "pakutoma.miowidget.widget.SwitchWidget.ACTION_WIDGET_ENABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = new RemoteViews(this.getPackageName(),R.layout.switch_widget);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        boolean isAuth = false;
        try {
            new CouponAPI(this);
        } catch (NotFoundValidTokenException e) {
            isAuth = true;
            Toast.makeText(this, "認証を開始します。", Toast.LENGTH_SHORT).show();
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https");
            builder.authority("api.iijmio.jp");
            builder.path("/mobile/d/v1/authorization");
            builder.encodedQuery("response_type=token&client_id=IilCI1xrAgqKrXV9Zt4&state=example_state&redirect_uri=pakutoma.miowidget://callback");
            Uri uri = builder.build();
            Intent authIntent = new Intent(Intent.ACTION_VIEW, uri);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(authIntent);
        }
        if(!isAuth) {
            setWidgetButtonIntent();
        }
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
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


