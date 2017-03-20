package pakutoma.iijmiocouponwidget.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Locale;

import pakutoma.iijmiocouponwidget.R;
import pakutoma.iijmiocouponwidget.exception.NotFoundValidTokenException;
import pakutoma.iijmiocouponwidget.service.GetTraffic;
import pakutoma.iijmiocouponwidget.service.SwitchCoupon;
import pakutoma.iijmiocouponwidget.utility.CouponAPI;

/**
 * Implementation of App Widget functionality.
 */
public class SwitchWidget extends AppWidgetProvider {


    private static final String ACTION_GET_TRAFFIC = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_GET_TRAFFIC";
    private static final String ACTION_WIDGET_ENABLE = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_WIDGET_ENABLE";

    private static final String ACTION_CALLBACK_GET_TRAFFIC = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_CALLBACK_GET_TRAFFIC";
    private static final String ACTION_CALLBACK_CHANGE_COUPON = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_CALLBACK_CHANGE_COUPON";

    private static Boolean isCouponEnable = true;

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_GET_TRAFFIC)) {
            setAlarm(context);
            Intent getTrafficIntent = new Intent(context, GetTraffic.class);
            context.startService(getTrafficIntent);
        }

        if (intent.getAction().equals(ACTION_CALLBACK_GET_TRAFFIC)) {
            updateTraffic(context,intent);
        }

        if (intent.getAction().equals(ACTION_CALLBACK_CHANGE_COUPON)) {
            changeSwitch(context,intent);
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED) || intent.getAction().equals(ACTION_WIDGET_ENABLE)) {
            setAlarm(context);
            Intent getTrafficIntent = new Intent(context, GetTraffic.class);
            context.startService(getTrafficIntent);
            Intent switchCouponIntent = new Intent(context, SwitchCoupon.class);
            context.startService(switchCouponIntent);
        }
    }

    private void setAlarm(Context context) {
        Intent alarmIntent = new Intent(context, SwitchWidget.class);
        alarmIntent.setAction(ACTION_GET_TRAFFIC);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final long interval = 1000 * 60 * 5;
        final long nextAlarm = System.currentTimeMillis() + interval;
        am.set(AlarmManager.RTC, nextAlarm, operation);
    }

    private void cancelAlarm(Context context) {
        Intent alarmIntent = new Intent(context, SwitchWidget.class);
        alarmIntent.setAction(ACTION_GET_TRAFFIC);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(operation);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setAlarm(context);
        Intent getTrafficIntent = new Intent(context, GetTraffic.class);
        context.startService(getTrafficIntent);
        Intent switchCouponIntent = new Intent(context, SwitchCoupon.class);
        context.startService(switchCouponIntent);
    }

    private void updateTraffic(Context context,Intent intent) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.switch_widget);
        if (!intent.getBooleanExtra("HAS_TOKEN",false)) {
            remoteViews.setTextViewText(R.id.data_traffic, "未認証");
        } else if (!intent.getBooleanExtra(("GET"),false)) {
            remoteViews.setTextViewText(R.id.data_traffic, "エラー");
        } else {
            int traffic = intent.getIntExtra("TRAFFIC",0);
            remoteViews.setTextViewText(R.id.data_traffic, convertPrefixString(traffic));
            isCouponEnable = intent.getBooleanExtra("COUPON",false);
            changeSwitchMode(remoteViews,isCouponEnable);
        }
        ComponentName thisWidget = new ComponentName(context, SwitchWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);
    }

    private String convertPrefixString(int traffic) {
        String result;
        if (traffic < 1000) {
            result = String.format(Locale.US,"%dMB",traffic);
        } else if (traffic < 10000) {
            result = String.format(Locale.US,"%1$.2fGB",traffic / 1000.0);
        } else {
            result = String.format(Locale.US,"%1$.1fGB",traffic / 1000.0);
        }
        return result;
    }

    private void changeSwitch(Context context,Intent intent) {
        if (!intent.getBooleanExtra("HAS_TOKEN",false)) {
            Toast.makeText(context, "認証が行われていません。", Toast.LENGTH_SHORT).show();
            return;
        }
        if(intent.getBooleanExtra("CHANGE",false)) {
            isCouponEnable = intent.getBooleanExtra("NOW_SWITCH",false);
            Toast.makeText(context,"クーポンを" + (isCouponEnable ? "ON" : "OFF") + "に変更しました。",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,"切り替えに失敗しました。",Toast.LENGTH_SHORT).show();
        }
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.switch_widget);
        changeSwitchMode(remoteViews,isCouponEnable);

        ComponentName thisWidget = new ComponentName(context, SwitchWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);
    }

    private void changeSwitchMode(RemoteViews remoteViews,Boolean isCouponEnable) {
        remoteViews.setViewVisibility(R.id.coupon_switch_base_on, isCouponEnable ? View.VISIBLE : View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.coupon_switch_base, isCouponEnable ? View.INVISIBLE : View.VISIBLE);
        remoteViews.setViewVisibility(R.id.coupon_switch_top_on, isCouponEnable ? View.VISIBLE : View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.coupon_switch_top, isCouponEnable ? View.INVISIBLE : View.VISIBLE);

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        cancelAlarm(context);
        super.onDisabled(context);
    }
}

