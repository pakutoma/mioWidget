package pakutoma.miowidget.activity

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

import java.util.regex.Matcher
import java.util.regex.Pattern

import pakutoma.miowidget.R
import pakutoma.miowidget.service.GetTraffic
import pakutoma.miowidget.widget.SwitchWidget

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

            // TODO: Service呼び出し部分書き換え
            val getTrafficIntent = Intent(this, GetTraffic::class.java)
            this.startService(getTrafficIntent)

            Toast.makeText(this, "認証が完了しました。", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
