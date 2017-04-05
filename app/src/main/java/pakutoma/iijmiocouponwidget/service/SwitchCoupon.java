package pakutoma.iijmiocouponwidget.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import pakutoma.iijmiocouponwidget.R;
import pakutoma.iijmiocouponwidget.widget.SwitchWidget;

/**
 * Created by PAKUTOMA on 2016/12/10.
 */
public class SwitchCoupon extends Service {
    private static final String ACTION_SWITCH_COUPON = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_SWITCH_COUPON";
    private static final String ACTION_CHANGE_COUPON = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_CHANGE_COUPON";
    private static final String ACTION_WAIT_CHANGE_SWITCH = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_WAIT_CHANGE_SWITCH";
    private static long lastClickTime = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ComponentName thisWidget = new ComponentName(this, SwitchWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.switch_widget);

        if (intent != null && ACTION_SWITCH_COUPON.equals(intent.getAction())) {
            SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
            if (!preferences.getBoolean("has_token",false)) {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("api.iijmio.jp");
                builder.path("/mobile/d/v1/authorization");
                builder.encodedQuery("response_type=token&client_id=IilCI1xrAgqKrXV9Zt4&state=example_state&redirect_uri=pakutoma.iijmiocouponwidget://callback");
                Uri uri = builder.build();
                Intent authIntent = new Intent(Intent.ACTION_VIEW, uri);
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(authIntent);
            } else {
                if (System.currentTimeMillis() - lastClickTime > 1000 * 60) {
                    lastClickTime = System.currentTimeMillis();
                    Intent waitIntent = new Intent(ACTION_WAIT_CHANGE_SWITCH);
                    sendBroadcast(waitIntent);
                    Intent changeIntent = new Intent(ACTION_CHANGE_COUPON);
                    changeIntent.setPackage("pakutoma.iijmiocouponwidget");
                    startService(changeIntent);
                } else {
                    Toast.makeText(this, "API limit: あと" + Long.toString((1000 * 60 - (System.currentTimeMillis() - lastClickTime)) / 1000) + "秒お待ち下さい。", Toast.LENGTH_SHORT).show();
                }
            }
        }

        Intent clickIntent = new Intent();
        clickIntent.setAction(ACTION_SWITCH_COUPON);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, clickIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.translate_button, pendingIntent);
        manager.updateAppWidget(thisWidget, remoteViews);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
