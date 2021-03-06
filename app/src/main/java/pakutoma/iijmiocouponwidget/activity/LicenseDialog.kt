package pakutoma.iijmiocouponwidget.activity

import android.content.Context
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Created by PAKUTOMA on 2016/12/04.
 */
class LicenseDialog : DialogPreference {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        negativeButtonText = null
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        negativeButtonText = null
    }

    override fun onCreateDialogView(): View {

        val webView = WebView(this.context)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/licenses.html")
        return webView
    }
}
