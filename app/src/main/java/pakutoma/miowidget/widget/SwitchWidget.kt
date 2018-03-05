package pakutoma.miowidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast

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
        private const val ACTION_WIDGET_ENABLE = "pakutoma.miowidget.widget.SwitchWidget.ACTION_WIDGET_ENABLE"
        private const val ACTION_WAIT_CHANGE_SWITCH = "pakutoma.miowidget.widget.SwitchWidget.ACTION_WAIT_CHANGE_SWITCH"

        private const val ACTION_CALLBACK_GET_TRAFFIC = "pakutoma.miowidget.widget.SwitchWidget.ACTION_CALLBACK_GET_TRAFFIC"
        private const val ACTION_CALLBACK_CHANGE_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_CALLBACK_CHANGE_COUPON"
        private const val JOB_ID = 1

        private var isCouponEnable: Boolean = true
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CALLBACK_GET_TRAFFIC) {
            updateTraffic(context, intent)
        }

        if (intent.action == ACTION_CALLBACK_CHANGE_COUPON) {
            changeSwitch(context, intent)
        }

        if (intent.action == ACTION_WAIT_CHANGE_SWITCH) {
            changeToWaitMode(context)
        }

        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED || intent.action == ACTION_WIDGET_ENABLE) {
            val getTrafficIntent = Intent(context, GetTraffic::class.java)
            context.startService(getTrafficIntent)
            val switchCouponIntent = Intent(context, SwitchCoupon::class.java)
            context.startService(switchCouponIntent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val getTrafficIntent = Intent(context, GetTraffic::class.java)
        context.startService(getTrafficIntent)
        val switchCouponIntent = Intent(context, SwitchCoupon::class.java)
        context.startService(switchCouponIntent)
    }

    private fun updateTraffic(context: Context, intent: Intent) {
        val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
        if (!intent.getBooleanExtra("HAS_TOKEN", false)) {
            remoteViews.setTextViewText(R.id.data_traffic, "未認証")
        } else if (!intent.getBooleanExtra("GET", false)) {
            remoteViews.setTextViewText(R.id.data_traffic, "エラー")
        } else {
            val traffic = intent.getIntExtra("TRAFFIC", 0)
            remoteViews.setTextViewText(R.id.data_traffic, convertPrefixString(traffic))
            isCouponEnable = intent.getBooleanExtra("COUPON", false)
            changeSwitchMode(remoteViews)
        }
        val thisWidget = ComponentName(context, SwitchWidget::class.java)
        val manager = AppWidgetManager.getInstance(context)
        manager.updateAppWidget(thisWidget, remoteViews)
    }

    private fun convertPrefixString(traffic: Int): String {
        return when (traffic) {
            in 0..999 -> String.format(Locale.US,"%dMB",traffic)
            in 1000..10000 -> String.format(Locale.US, "%1$.2fGB", traffic / 1000.0)
            else -> String.format(Locale.US, "%1$.1fGB", traffic / 1000.0)
        }
    }

    private fun changeSwitch(context: Context, intent: Intent) {
        if (!intent.getBooleanExtra("HAS_TOKEN", false)) {
            Toast.makeText(context, "認証が行われていません。", Toast.LENGTH_SHORT).show()
            return
        }
        if (intent.getBooleanExtra("CHANGE", false)) {
            isCouponEnable = intent.getBooleanExtra("NOW_SWITCH", false)
            Toast.makeText(context, "クーポンを" + (if (isCouponEnable) "ON" else "OFF") + "に変更しました。", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "切り替えに失敗しました。", Toast.LENGTH_SHORT).show()
        }
        val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
        changeSwitchMode(remoteViews)

        val thisWidget = ComponentName(context, SwitchWidget::class.java)
        val manager = AppWidgetManager.getInstance(context)
        manager.updateAppWidget(thisWidget, remoteViews)
    }

    private fun changeToWaitMode(context: Context) {
        val remoteViews = RemoteViews(context.packageName, R.layout.switch_widget)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_on_end, View.INVISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_start, View.INVISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_on_start, if (isCouponEnable) View.VISIBLE else View.INVISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_end, if (isCouponEnable) View.INVISIBLE else View.VISIBLE)
        val thisWidget = ComponentName(context, SwitchWidget::class.java)
        val manager = AppWidgetManager.getInstance(context)
        manager.updateAppWidget(thisWidget, remoteViews)
    }

    private fun changeSwitchMode(remoteViews: RemoteViews) {
        remoteViews.setViewVisibility(R.id.coupon_switch_base_on, if (isCouponEnable) View.VISIBLE else View.INVISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_base, if (isCouponEnable) View.INVISIBLE else View.VISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_on_end, if (isCouponEnable) View.VISIBLE else View.INVISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_start, if (isCouponEnable) View.INVISIBLE else View.VISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_on_start, View.INVISIBLE)
        remoteViews.setViewVisibility(R.id.coupon_switch_top_end, View.INVISIBLE)
    }

    override fun onEnabled(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context,GetTraffic::class.java))
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

