package pakutoma.miowidget.activity;

/**
 * Created by PAKUTOMA on 2016/12/04.
 */

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by PAKUTOMA on 2016/12/04.
 */
public class AboutDialog extends DialogPreference {
    public AboutDialog(Context context, AttributeSet attrs) {
        super(context,attrs);
        setNegativeButtonText(null);
    }

    public AboutDialog(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        setNegativeButtonText(null);
    }

    @Override
    protected View onCreateDialogView() {
        WebView webView = new WebView(this.getContext());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/about.html");
        return webView;
    }
}
