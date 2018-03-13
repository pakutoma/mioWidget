package pakutoma.miowidget.utility

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class CouponInfoDbHelper(context: Context) : ManagedSQLiteOpenHelper(context, "MyDatabase", null, 1) {
    companion object {
        private var instance: CouponInfoDbHelper? = null

        @Synchronized
        fun getInstance(context: Context): CouponInfoDbHelper {
            if (instance == null) {
                instance = CouponInfoDbHelper(context.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable("coupon_info", true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                "service_code" to TEXT,
                "plan_name" to TEXT,
                "number" to TEXT,
                "regulation" to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}

val Context.database: CouponInfoDbHelper
    get() = CouponInfoDbHelper.getInstance(applicationContext)

class CouponInfoFromDb (val serviceCode:String,private val planName:String,val number:String, val regulation:Boolean) {
    val plan:String
        get() = "$serviceCode ($planName)"
    val line:String
        get() = "$number ($serviceCode)"
}