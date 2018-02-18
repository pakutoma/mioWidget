package pakutoma.miowidget.activity

/**
 * Created by PAKUTOMA on 2016/12/04.
 */

import android.content.Context
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Created by PAKUTOMA on 2016/12/04.
 */
class AboutDialog : DialogPreference {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        negativeButtonText = null
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        negativeButtonText = null
    }

    override fun onCreateDialogView(): View {
        val webView = WebView(this.context)
        webView.setWebViewClient(WebViewClient())
        webView.loadUrl("file:///android_asset/about.html")
        return webView
    }
}
