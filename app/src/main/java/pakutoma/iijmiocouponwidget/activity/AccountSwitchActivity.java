package pakutoma.iijmiocouponwidget.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

import pakutoma.iijmiocouponwidget.R;
import pakutoma.iijmiocouponwidget.utility.AccountDBOpenHelper;

/**
 * Created by PAKUTOMA on 2016/12/10.
 */
public class AccountSwitchActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AccountSwitchPreferenceFragment())
                .commit();
    }

    public static class AccountSwitchPreferenceFragment extends PreferenceFragment {

        private PreferenceScreen root;
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_switch_account);
            this.root = getPreferenceScreen();
            buildAccountSwitchPreference();
        }

        private void buildAccountSwitchPreference() {
            AccountDBOpenHelper helper = new AccountDBOpenHelper(root.getContext());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from accounttable",null);
            boolean isEof = cursor.moveToFirst();
            PreferenceCategory category = new PreferenceCategory(root.getContext());
            category.setTitle("ウィジェットで切り替えるアカウントを選択");
            root.addPreference(category);
            while(isEof) {
                CheckBoxPreference cb = new CheckBoxPreference(root.getContext());
                cb.setTitle(insertHyphen(cursor.getString(cursor.getColumnIndex("number"))));
                cb.setSummary("ID:" + cursor.getString(cursor.getColumnIndex("hdoServiceCode")));
                cb.setChecked(cursor.getInt(cursor.getColumnIndex("canSwitch")) == 1);
                cb.setOnPreferenceChangeListener(onPreferenceChangeListener);
                category.addPreference(cb);
                isEof = cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }

        private void setCanSwitchAccount(String phoneNumber) {
            AccountDBOpenHelper helper = new AccountDBOpenHelper(root.getContext());
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues clearValue = new ContentValues();
            clearValue.put("canSwitch",0);
            db.update("accounttable",clearValue,"canSwitch = 1",null);
            ContentValues setValue = new ContentValues();
            setValue.put("canSwitch",1);
            db.update("accounttable",setValue,"number = ?",new String[]{removeHyphen(phoneNumber)});
            db.close();
        }

        private void removeAccountSwitchPreference() {
            getPreferenceScreen().removeAll();
        }

        private String insertHyphen(String s) {
            return new StringBuilder(s)
                    .insert(7,"-")
                    .insert(3,"-")
                    .toString();
        }

        private String removeHyphen(String s) {
            return s.replace("-","");
        }

        private final Preference.OnPreferenceChangeListener onPreferenceChangeListener =
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(final Preference preference, final Object newValue) {

                        if (Boolean.FALSE.equals(newValue)) {
                            return false;
                        }

                        final String phoneNumber = String.valueOf(preference.getTitle());
                        setCanSwitchAccount(phoneNumber);
                        removeAccountSwitchPreference();
                        buildAccountSwitchPreference();

                        return true;
                    }
                };
    }
}
