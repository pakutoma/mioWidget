package pakutoma.miowidget.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast

import java.util.regex.Pattern

import pakutoma.miowidget.R
import pakutoma.miowidget.service.FetchRemainsService

class AuthActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        if (intent?.dataString == null) {
            finish()
        }

        val uri = Uri.parse(intent.dataString)
        val p = Pattern.compile("(?<=access_token=).+?(?=&)")
        val m = p.matcher(uri.fragment)
        if (m.find()) {
            val token = m.group()
            val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("X-IIJmio-Authorization", token)
            editor.putBoolean("has_token", true)
            editor.apply()
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(homeIntent)

            val fetchRemainsIntent = Intent(applicationContext, FetchRemainsService::class.java)
            @TargetApi(Build.VERSION_CODES.O)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(fetchRemainsIntent)
            } else {
                startService(fetchRemainsIntent)
            }

            Toast.makeText(this, "認証が完了しました。", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
