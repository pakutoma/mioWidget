package pakutoma.iijmiocouponwidget;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


/**
 * Created by PAKUTOMA on 2016/06/21.
 * Get Traffic Function
 */
public class GetTraffic extends IntentService {
    private static final String ACTION_CALLBACK_GET_TRAFFIC = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_CALLBACK_GET_TRAFFIC";

    public GetTraffic() {
        super("GetTraffic");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
        String accessToken = preferences.getString("X-IIJmio-Authorization","");
        if (accessToken.equals("")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("has_token",false);
            editor.apply();

            sendCallback(false,false,-1,false);
            return;
        }

        CouponAPI coupon = new CouponAPI(accessToken);
        CouponData cd;
        try {
            cd = coupon.getCouponData();
        } catch (Exception e) {
            sendCallback(true,false,-1,false);
            return;
        }

        boolean isOnCoupon = cd.getSwitch();
        int traffic = cd.getTraffic();

        sendCallback(true,true,traffic,isOnCoupon);
    }


    private void sendCallback (boolean hasToken,boolean couldGet,int traffic,boolean isOnCoupon) {
        Intent callbackIntent = new Intent(ACTION_CALLBACK_GET_TRAFFIC);
        callbackIntent.putExtra("HAS_TOKEN",hasToken);
        if (hasToken) {
            callbackIntent.putExtra("GET",couldGet);
        }
        if (couldGet){
            callbackIntent.putExtra("TRAFFIC",traffic);
            callbackIntent.putExtra("COUPON",isOnCoupon);
        }
        callbackIntent.setPackage("pakutoma.iijmiocouponwidget");
        startService(callbackIntent);
    }
}