package pakutoma.miowidget.activity

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast

import pakutoma.miowidget.R

/**
 * 認証画面をブラウザで開くためのActivity
 * Created by PAKUTOMA on 2017/03/20.
 */

class OpenBrowserActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val extras = intent.extras
        var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val views = RemoteViews(this.packageName, R.layout.switch_widget)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Toast.makeText(this, "認証を開始します。", Toast.LENGTH_SHORT).show()
        val uri = Uri.parse(resources.getText(R.string.authUri).toString())
        val authIntent = Intent(Intent.ACTION_VIEW, uri)
        authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(authIntent)
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}


