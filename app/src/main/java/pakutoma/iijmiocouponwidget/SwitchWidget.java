package pakutoma.iijmiocouponwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class SwitchWidget extends AppWidgetProvider {


    private static final String ACTION_GET_TRAFFIC = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_GET_TRAFFIC";
    private static final String ACTION_SWITCH_COUPON = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_SWITCH_COUPON";
    private static final String ACTION_CHANGE_COUPON = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_CHANGE_COUPON";

    private static Boolean isCouponEnable = true;

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_GET_TRAFFIC)) {
           if (ACTION_GET_TRAFFIC.equals(intent.getAction())) {
                    Intent serviceIntent = new Intent(context, GetTraffic.class);
                    context.startService(serviceIntent);
           }
            setAlarm(context);
        }
    }
    private void setAlarm(Context context) {
        Intent alarmIntent = new Intent(context, SwitchWidget.class);
        alarmIntent.setAction(ACTION_GET_TRAFFIC);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis();
        final long interval = 1000 * 60;
        long oneMinuteAfter = now + interval;
        am.set(AlarmManager.RTC, oneMinuteAfter, operation);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setAlarm(context);
        Intent GetTrafficIntent = new Intent(context, GetTraffic.class);
        context.startService(GetTrafficIntent);
        Intent switchCouponIntent = new Intent(context, SwitchCoupon.class);
        context.startService(switchCouponIntent);
    }

    public static class UpdateTraffic extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags,int startId) {
            Toast.makeText(this, "Traffic取得完了", Toast.LENGTH_SHORT).show();
            CharSequence widgetText;
            int traffic = intent.getIntExtra("TRAFFIC",0);
            if (traffic < 1000) {
                widgetText = String.format(Locale.US,"%dMB",traffic);
            } else if (traffic < 10000) {
                widgetText = String.format(Locale.US,"%1$.2fGB",traffic / 1000.0);
            } else {
                widgetText = String.format(Locale.US,"%1$.1fGB",traffic / 1000.0);
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.switch_widget);
            remoteViews.setTextViewText(R.id.data_traffic, widgetText);
            isCouponEnable = intent.getBooleanExtra("COUPON",true);
            remoteViews.setTextViewText(R.id.coupon_switch, isCouponEnable ? "ON" : "OFF");
            ComponentName thisWidget = new ComponentName(this, SwitchWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, remoteViews);

            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }

    public static class ChangeSwitch extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags,int startId) {
            Toast.makeText(this,"Coupon変更完了",Toast.LENGTH_SHORT).show();
            if(intent.getBooleanExtra("CHANGE",false)) {
                isCouponEnable = !isCouponEnable;
            }
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.switch_widget);
            remoteViews.setTextViewText(R.id.coupon_switch, isCouponEnable ? "ON" : "OFF");
            ComponentName thisWidget = new ComponentName(this, SwitchWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, remoteViews);
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    public static class SwitchCoupon extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            ComponentName thisWidget = new ComponentName(this, SwitchWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.switch_widget);
            if (ACTION_SWITCH_COUPON.equals(intent.getAction())) {
                Toast.makeText(this, "ボタンが押された", Toast.LENGTH_SHORT).show();
                Intent changeIntent = new Intent(ACTION_CHANGE_COUPON);
                changeIntent.putExtra("SWITCH",!isCouponEnable);
                changeIntent.setPackage("pakutoma.iijmiocouponwidget");
                startService(changeIntent);
            }

            Intent clickIntent = new Intent();
            clickIntent.setAction(ACTION_SWITCH_COUPON);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, clickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.coupon_switch, pendingIntent);
            manager.updateAppWidget(thisWidget, remoteViews);
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

