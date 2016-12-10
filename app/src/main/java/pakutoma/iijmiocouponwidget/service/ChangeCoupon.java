package pakutoma.iijmiocouponwidget.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import pakutoma.iijmiocouponwidget.utility.CouponAPI;
import pakutoma.iijmiocouponwidget.utility.CouponData;
import pakutoma.iijmiocouponwidget.exception.NotFoundValidTokenException;


/**
 * ChangeCoupon change iijmio's coupon switch on/off.
 * Created by PAKUTOMA on 2016/06/21.
 */
public class ChangeCoupon extends IntentService {
    private static final String ACTION_CALLBACK_CHANGE_COUPON = "pakutoma.iijmiocouponwidget.widget.SwitchWidget.ACTION_CALLBACK_CHANGE_COUPON";

    public ChangeCoupon() {
        super("ChangeCoupon");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            CouponAPI coupon = new CouponAPI(getApplicationContext());
            CouponData cd = coupon.getCouponData();
            cd.setSwitch(!cd.getSwitch());
            coupon.changeCouponStatus(cd);
        } catch (NotFoundValidTokenException e) {
            SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("X-IIJmio-Authorization","");
            editor.putBoolean("has_token",false);
            editor.apply();
            sendCallback(false,false);
        } catch (Exception e) {
            sendCallback(true, false);
            return;
        }

        sendCallback(true,true);
    }

    private void sendCallback(boolean hasToken,boolean isChanged) {
        Intent callbackIntent = new Intent(ACTION_CALLBACK_CHANGE_COUPON);
        callbackIntent.putExtra("HAS_TOKEN",hasToken);
        if(hasToken) {
            callbackIntent.putExtra("CHANGE",isChanged);
        }
        callbackIntent.setPackage("pakutoma.iijmiocouponwidget");
        sendBroadcast(callbackIntent);
    }

}
