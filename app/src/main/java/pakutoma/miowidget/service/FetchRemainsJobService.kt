package pakutoma.miowidget.service

import android.app.job.JobParameters
import android.content.Context
import kotlinx.coroutines.experimental.launch
import pakutoma.miowidget.R

import java.io.IOException
import android.app.job.JobService
import com.github.kittinunf.fuel.core.HttpException
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.withContext

import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponInfo
import pakutoma.miowidget.widget.updateSwitchStatus


/**
 * Created by PAKUTOMA on 2016/06/21.
 * Get Traffic Function
 */
class FetchRemainsJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            fetchRemains(applicationContext)
            jobFinished(params, true)
        }
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        return false
    }
}