package pakutoma.iijmiocouponwidget.service

import android.app.job.JobParameters
import kotlinx.coroutines.launch
import android.app.job.JobService
import kotlinx.coroutines.GlobalScope

class FetchRemainsJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        GlobalScope.launch {
            fetchRemains(applicationContext)
            jobFinished(params, true)
        }
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        return false
    }
}