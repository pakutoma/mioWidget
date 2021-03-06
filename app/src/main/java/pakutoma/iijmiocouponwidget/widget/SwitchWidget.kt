package pakutoma.iijmiocouponwidget.widget

import android.annotation.TargetApi
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

import java.util.Locale

import pakutoma.iijmiocouponwidget.R
import pakutoma.iijmiocouponwidget.service.FetchRemainsJobService
import pakutoma.iijmiocouponwidget.service.SwitchCouponService
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import pakutoma.iijmiocouponwidget.service.FetchRemainsService
import java.util.concurrent.TimeUnit


/**
 * Implementation of App Widget functionality.
 */

class SwitchWidget : AppWidgetProvider() {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "switch_service"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            startFetchRemainsService(context)
        }
        if(intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            registerFetchRemainsJobService(context)
            registerNotificationChannel(context)
            startFetchRemainsService(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        registerFetchRemainsJobService(context)
        registerNotificationChannel(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        startFetchRemainsService(context)
    }

    override fun onDisabled(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(context.resources.getInteger(R.integer.periodic_fetch_remains))
        super.onDisabled(context)
    }

    private fun registerNotificationChannel(context: Context) {
        @TargetApi(Build.VERSION_CODES.O)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if(manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.switch_service),
                        NotificationManager.IMPORTANCE_MIN
                )
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun registerFetchRemainsJobService(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobs = jobScheduler.allPendingJobs
        val jobId = context.resources.getInteger(R.integer.periodic_fetch_remains)
        if(jobs?.any { it.id == jobId } != true) {
            val jobInfo = JobInfo.Builder(jobId, ComponentName(context, FetchRemainsJobService::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                    .setPersisted(true)
                    .build()
            jobScheduler.schedule(jobInfo)
        }
    }

    private fun startFetchRemainsService(context: Context) {
        val intent = Intent(context, FetchRemainsService::class.java)
        @TargetApi(Build.VERSION_CODES.O)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}

fun changeToFetchingMode(context: Context) {
    val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
    remoteViews.setTextViewText(R.id.data_traffic, "取得中")
    val thisWidget = ComponentName(context, SwitchWidget::class.java)
    val manager = AppWidgetManager.getInstance(context)
    manager.updateAppWidget(thisWidget, remoteViews)
}

fun changeToWaitMode(context: Context, isCouponEnabled: Boolean) {
    val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_end, View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_start, View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_start, if (isCouponEnabled) View.VISIBLE else View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_end, if (isCouponEnabled) View.INVISIBLE else View.VISIBLE)
    val thisWidget = ComponentName(context, SwitchWidget::class.java)
    val manager = AppWidgetManager.getInstance(context)
    manager.updateAppWidget(thisWidget, remoteViews)
}

fun updateSwitchStatus(context: Context, hasToken: Boolean, couldGet: Boolean, remains: Int = -1, isCouponEnabled: Boolean = false) {
    val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
    remoteViews.setViewVisibility(R.id.coupon_switch_base_on, if (isCouponEnabled) View.VISIBLE else View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_base, if (isCouponEnabled) View.INVISIBLE else View.VISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_end, if (isCouponEnabled) View.VISIBLE else View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_start, if (isCouponEnabled) View.INVISIBLE else View.VISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_start, View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_end, View.INVISIBLE)
    if (!hasToken) {
        remoteViews.setTextViewText(R.id.data_traffic, "未認証")
    } else if (!couldGet) {
        remoteViews.setTextViewText(R.id.data_traffic, "エラー")
    } else {
        remoteViews.setTextViewText(R.id.data_traffic, convertPrefixString(remains))
    }
    val clickIntent = Intent(context, SwitchCouponService::class.java)
    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        PendingIntent.getForegroundService(context, 0, clickIntent, FLAG_CANCEL_CURRENT)
    } else {
        PendingIntent.getService(context, 0, clickIntent, FLAG_CANCEL_CURRENT)
    }
    remoteViews.setOnClickPendingIntent(R.id.transparent_button, pendingIntent)
    val thisWidget = ComponentName(context, SwitchWidget::class.java)
    val manager = AppWidgetManager.getInstance(context)
    manager.updateAppWidget(thisWidget, remoteViews)
}

private fun convertPrefixString(traffic: Int): String {
    return when (traffic) {
        in 0..999 -> String.format(Locale.US, "%dMB", traffic)
        in 1000..10000 -> String.format(Locale.US, "%1$.2fGB", traffic / 1000.0)
        else -> String.format(Locale.US, "%1$.1fGB", traffic / 1000.0)
    }
}

fun setSwitchPendingIntent(context: Context) {
    val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
    val clickIntent = Intent(context, SwitchCouponService::class.java)
    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        PendingIntent.getForegroundService(context, 0, clickIntent, FLAG_CANCEL_CURRENT)
    } else {
        PendingIntent.getService(context, 0, clickIntent, FLAG_CANCEL_CURRENT)
    }
    remoteViews.setOnClickPendingIntent(R.id.transparent_button, pendingIntent)
    val thisWidget = ComponentName(context, SwitchWidget::class.java)
    val manager = AppWidgetManager.getInstance(context)
    manager.updateAppWidget(thisWidget, remoteViews)
}