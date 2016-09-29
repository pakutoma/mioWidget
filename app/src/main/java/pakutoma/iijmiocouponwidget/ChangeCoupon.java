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

        CouponAPI coupon = new CouponAPI();

        String couponStatus;
        try {
            couponStatus = coupon.getCouponStatus(accessToken);
        } catch (IOException e) {
            sendCallback(true,false);
            return;
        }

        ObjectNode sendNode;
        try {
            sendNode = createToSendJsonNode(couponStatus,intent.getBooleanExtra("SWITCH",false));
        } catch (IOException e) {
            sendCallback(true,false);
            return;
        }

        try {
            coupon.putCouponStatus(accessToken,sendNode);
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

    private ObjectNode createToSendJsonNode(String couponStatus,boolean isOnSwitch) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode getNode = mapper.readTree(couponStatus);
        ObjectNode putNode = mapper.createObjectNode();
        ArrayNode hdoNode = putNode.putArray("couponInfo").addObject().putArray("hdoInfo");
        for (JsonNode item : getNode.get("couponInfo").get(0).get("hdoInfo")) {
            ObjectNode sim = hdoNode.addObject();
            sim.put("hdoServiceCode",item.get("hdoServiceCode").asText());
            sim.put("couponUse",isOnSwitch);
        }
        return putNode;
    }
}
