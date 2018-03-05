package pakutoma.miowidget.widget

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

import pakutoma.miowidget.R
import pakutoma.miowidget.service.GetTraffic
import pakutoma.miowidget.service.SwitchCoupon
import android.app.job.JobInfo
import android.app.job.JobScheduler

/**
 * Implementation of App Widget functionality.
 */

class SwitchWidget : AppWidgetProvider() {
    companion object {
        private const val JOB_ID = 1
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        setSwitchPendingIntent(context)
    }

    override fun onEnabled(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, GetTraffic::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic((1000 * 60 * 5).toLong())
                .setPersisted(true)
                .build()
        jobScheduler.schedule(jobInfo)
    }

    override fun onDisabled(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(JOB_ID)
        super.onDisabled(context)
    }
}

fun changeToWaitMode(context: Context, isCouponEnable: Boolean) {
    val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_end, View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_start, View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_start, if (isCouponEnable) View.VISIBLE else View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_end, if (isCouponEnable) View.INVISIBLE else View.VISIBLE)
    val thisWidget = ComponentName(context, SwitchWidget::class.java)
    val manager = AppWidgetManager.getInstance(context)
    manager.updateAppWidget(thisWidget, remoteViews)
}

fun updateSwitchStatus(context: Context, hasToken: Boolean, couldGet: Boolean, remains: Int = -1, isCouponEnable: Boolean = false) {
    val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
    remoteViews.setViewVisibility(R.id.coupon_switch_base_on, if (isCouponEnable) View.VISIBLE else View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_base, if (isCouponEnable) View.INVISIBLE else View.VISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_end, if (isCouponEnable) View.VISIBLE else View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_start, if (isCouponEnable) View.INVISIBLE else View.VISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_on_start, View.INVISIBLE)
    remoteViews.setViewVisibility(R.id.coupon_switch_top_end, View.INVISIBLE)
    if (!hasToken) {
        remoteViews.setTextViewText(R.id.data_traffic, "未認証")
    } else if (!couldGet) {
        remoteViews.setTextViewText(R.id.data_traffic, "エラー")
    } else {
        remoteViews.setTextViewText(R.id.data_traffic, convertPrefixString(remains))
    }
    val clickIntent = Intent(context,SwitchCoupon::class.java)
    val pendingIntent = PendingIntent.getService(context, 0, clickIntent, FLAG_CANCEL_CURRENT)
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
    val clickIntent = Intent(context,SwitchCoupon::class.java)
    val pendingIntent = PendingIntent.getService(context, 0, clickIntent, FLAG_CANCEL_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.transparent_button, pendingIntent)
    val thisWidget = ComponentName(context, SwitchWidget::class.java)
    val manager = AppWidgetManager.getInstance(context)
    manager.updateAppWidget(thisWidget, remoteViews)
}