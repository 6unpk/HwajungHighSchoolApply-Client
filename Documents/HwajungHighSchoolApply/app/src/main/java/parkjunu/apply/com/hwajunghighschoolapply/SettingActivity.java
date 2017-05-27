package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TimePicker;

public class SettingActivity extends  AppCompatPreferenceActivity implements Preference.OnPreferenceClickListener{

    TimePickerDialog.OnTimeSetListener timeSetListener;
    Preference getNotification;
    Preference timer;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_set);
        getNotification = (Preference)findPreference("get_food");
        timer = (Preference)findPreference("timer");
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();

        timeSetListener =  new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    editor.putInt("hour", hourOfDay);
                    editor.putInt("min", minute);
                    editor.commit();
            }
        };

        getNotification.setOnPreferenceClickListener(SettingActivity.this);
        timer.setOnPreferenceClickListener(SettingActivity.this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference.getKey().equals("get_food")) {
            startService(new Intent(getApplicationContext(), SchoolFoodNotice.class));
        }
        else if ( preference.getKey().equals("timer")){
            new TimePickerDialog(SettingActivity.this, timeSetListener, pref.getInt("hour", 11), pref.getInt("min", 0), false).show();
        }
        return false;
    }
}
