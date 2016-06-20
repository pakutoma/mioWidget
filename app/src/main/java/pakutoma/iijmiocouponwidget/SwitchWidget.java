package pakutoma.iijmiocouponwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of App Widget functionality.
 */
public class SwitchWidget extends AppWidgetProvider {


    private static final String ACTION_UPDATE_TRAFFIC = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_UPDATE_TRAFFIC";
    private static final String ACTION_SWITCH_COUPON = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_SWITCH_COUPON";
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_UPDATE_TRAFFIC)) {
            if (ACTION_UPDATE_TRAFFIC.equals(intent.getAction())) {
                Intent serviceIntent = new Intent(context, UpdateTraffic.class);
                context.startService(serviceIntent);
            }
            setAlarm(context);
        }
    }
    private void setAlarm(Context context) {
        Intent alarmIntent = new Intent(context, SwitchWidget.class);
        alarmIntent.setAction(ACTION_UPDATE_TRAFFIC);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis();
        final long interval = 1000 * 60;
        long oneSecondAfter = now + interval;
        am.set(AlarmManager.RTC, oneSecondAfter, operation);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setAlarm(context);
        Intent updateTrafficIntent = new Intent(context, UpdateTraffic.class);
        context.startService(updateTrafficIntent);
        Intent switchCouponIntent = new Intent(context, SwitchCoupon.class);
        context.startService(switchCouponIntent);
    }

    public static class UpdateTraffic extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags,int startId) {
            Toast.makeText(this, "UpdateTraffic開始", Toast.LENGTH_SHORT).show();
            CharSequence widgetText;
            int traffic = getTraffic();
            if (traffic < 1000) {
                widgetText = String.format(Locale.US,"%dMB",traffic);
            } else if (traffic < 10000) {
                widgetText = String.format(Locale.US,"%1$.2fGB",traffic / 1000.0);
            } else {
                widgetText = String.format(Locale.US,"%1$.1fGB",traffic / 1000.0);
            }
            // Construct the RemoteViews object
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.switch_widget);
            remoteViews.setTextViewText(R.id.data_traffic, widgetText);

            // Instruct the widget manager to update the widget
            ComponentName thisWidget = new ComponentName(this, SwitchWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, remoteViews);
            return START_STICKY;
        }

        private int getTraffic() {
            HttpURLConnection connection;
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
                connection.setRequestProperty("X-IIJmio-Authorization", "v7jUG7mxz4Vy74dBLlHlXekNdoLalKW1466339013");
                connection.connect();
                BufferedReader br = new BufferedReader( new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
            } catch (Exception e) {
                Toast.makeText(this, "error: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
            String regex = "\"volume\": (\\d+)";
            Pattern p = Pattern.compile(regex,Pattern.MULTILINE);
            Matcher m = p.matcher(sb.toString());
            int traffic = 0;
            while (m.find()){
                traffic += Integer.parseInt(m.group(1));
            }
            Toast.makeText(this, String.valueOf(traffic), Toast.LENGTH_SHORT).show();
            return traffic;

        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }



    public static class SwitchCoupon extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Toast.makeText(this, "SwitchCoupon開始", Toast.LENGTH_SHORT).show();
            ComponentName thisWidget = new ComponentName(this, SwitchWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.switch_widget);
            if (ACTION_SWITCH_COUPON.equals(intent.getAction())) {
                Toast.makeText(this, "ボタンが押された", Toast.LENGTH_SHORT).show();
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

