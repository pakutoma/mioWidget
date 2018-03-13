package pakutoma.miowidget.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.github.kittinunf.fuel.core.HttpException
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import pakutoma.miowidget.R
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.utility.CouponInfo
import pakutoma.miowidget.widget.changeToFetchingMode
import pakutoma.miowidget.widget.updateSwitchStatus
import java.io.IOException

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
                    .setContentTitle(applicationContext.getString(R.string.switch_notification))
                    .build()
            startForeground(FETCH_REMAINS_NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        changeToFetchingMode(applicationContext)
        launch {
            fetchRemains(applicationContext)
            stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
