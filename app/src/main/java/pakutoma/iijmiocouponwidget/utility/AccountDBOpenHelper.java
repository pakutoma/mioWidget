package pakutoma.iijmiocouponwidget.utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * account.db SQLiteOpenHelper
 * Created by PAKUTOMA on 2016/12/10.
 */
public class AccountDBOpenHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE = "create table accounttable ( hdoServiceCode text,number text,canSwitch integer);";
    private static final String DROP_TABLE = "drop table accounttable;";

    public AccountDBOpenHelper(Context context) {
        super(context,"account.db",null,1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db,int oldversion,int newversion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

}
