package pakutoma.iijmiocouponwidget.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pakutoma.iijmiocouponwidget.R
import pakutoma.iijmiocouponwidget.widget.changeToFetchingMode

/**
 * Created by PAKUTOMA on 2018/03/06.
 * fetch remains service
 */
class FetchRemainsService : Service() {

    companion object {
        private const val FETCH_REMAINS_NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        @TargetApi(Build.VERSION_CODES.O)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = Notification
                    .Builder(applicationContext, "switch_service")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(applicationContext.getString(R.string.notification_title))
                    .setContentText(applicationContext.getString(R.string.fetch_remains_notification_text))
                    .build()
            startForeground(FETCH_REMAINS_NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        changeToFetchingMode(applicationContext)
        GlobalScope.launch {
            fetchRemains(applicationContext)
            stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
