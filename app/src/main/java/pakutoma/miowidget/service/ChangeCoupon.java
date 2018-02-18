package pakutoma.miowidget.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import pakutoma.miowidget.utility.CouponAPI;
import pakutoma.miowidget.exception.NotFoundValidTokenException;


/**
 * ChangeCoupon change iijmio's coupon switch on/off.
 * Created by PAKUTOMA on 2016/06/21.
 */
public class ChangeCoupon extends IntentService {
    private static final String ACTION_CALLBACK_CHANGE_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_CALLBACK_CHANGE_COUPON";

    public ChangeCoupon() {
        super("ChangeCoupon");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            CouponAPI coupon = new CouponAPI(getApplicationContext());
            boolean changedSwitch = coupon.changeCouponStatus();
            sendCallback(true,true,changedSwitch);
        } catch (NotFoundValidTokenException e) {
            SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("X-IIJmio-Authorization","");
            editor.putBoolean("has_token",false);
            editor.apply();
            sendCallback(false,false,false);
        } catch (Exception e) {
            sendCallback(true, false,false);
        }
    }

    private void sendCallback(boolean hasToken,boolean isChanged,boolean nowSwitch) {
        Intent callbackIntent = new Intent(ACTION_CALLBACK_CHANGE_COUPON);
        callbackIntent.putExtra("HAS_TOKEN",hasToken);
        if(hasToken) {
            callbackIntent.putExtra("CHANGE",isChanged);
        }
        if (isChanged) {
            callbackIntent.putExtra("NOW_SWITCH",nowSwitch);
        }
        callbackIntent.setPackage("pakutoma.miowidget");
        sendBroadcast(callbackIntent);
    }

}
