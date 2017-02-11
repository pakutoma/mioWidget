package pakutoma.iijmiocouponwidget.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.List;

import pakutoma.iijmiocouponwidget.utility.AccountDBOpenHelper;
import pakutoma.iijmiocouponwidget.utility.AccountData;
import pakutoma.iijmiocouponwidget.utility.CouponAPI;
import pakutoma.iijmiocouponwidget.utility.CouponData;
import pakutoma.iijmiocouponwidget.exception.NotFoundValidTokenException;

/**
 * Created by PAKUTOMA on 2016/12/10.
 */
public class InitialSetting extends IntentService{

    public InitialSetting() {
        super("InitialSetting");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CouponAPI coupon;
        List<AccountData> accountDataList;
        try {
            coupon = new CouponAPI(getApplicationContext(),false);
            accountDataList = coupon.getAccountsData();
            AccountDBOpenHelper helper = new AccountDBOpenHelper(getApplicationContext());
            SQLiteDatabase accountdb = helper.getWritableDatabase();
            int count = 0;
            for (AccountData ad : accountDataList) {
                ContentValues values = new ContentValues();
                values.put("hdoServiceCode",ad.getHdoServiceCode());
                values.put("number",ad.getNumber());
                values.put("canSwitch",count == 0 ? 1 : 0);
                accountdb.insert("accounttable",null,values);
                count++;
            }
            accountdb.close();

            Intent getTrafficIntent = new Intent(getApplicationContext(), GetTraffic.class);
            getApplicationContext().startService(getTrafficIntent);
        } catch (NotFoundValidTokenException e) {
            SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("X-IIJmio-Authorization","");
            editor.putBoolean("has_token",false);
            editor.apply();
        } catch (IOException e) {
            return;
        }
    }
}
