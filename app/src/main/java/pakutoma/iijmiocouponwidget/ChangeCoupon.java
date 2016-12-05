package pakutoma.iijmiocouponwidget;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;


/**
 * ChangeCoupon change iijmio's coupon switch on/off.
 * Created by PAKUTOMA on 2016/06/21.
 */
public class ChangeCoupon extends IntentService {
    private static final String ACTION_CALLBACK_CHANGE_COUPON = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_CALLBACK_CHANGE_COUPON";

    public ChangeCoupon() {
        super("ChangeCoupon");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
        String accessToken = preferences.getString("X-IIJmio-Authorization","");
        if (accessToken.equals("")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("has_token",false);
            editor.apply();

            sendCallback(false,false);
            return;
        }

        CouponAPI coupon = new CouponAPI(accessToken);
        CouponData cd = new CouponData(!intent.getBooleanExtra("SWITCH",false));
        try {
            coupon.changeCouponStatus(cd);
        } catch (IOException e) {
            sendCallback(true,false);
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
        startService(callbackIntent);
    }

}
