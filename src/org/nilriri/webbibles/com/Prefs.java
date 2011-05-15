package org.nilriri.webbibles.com;

import org.nilriri.webbibles.R;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Prefs extends PreferenceActivity {
    // Option names and default values
    private static final String OPT_AUTOREAD = "autosave";
    private static final boolean OPT_AUTOREAD_DEF = false;

    private static final String OPT_AUTOSAVE = "autosave";
    private static final boolean OPT_AUTOSAVE_DEF = true;

    private static final String OPT_AUTOLOAD = "autoload";
    private static final boolean OPT_AUTOLOAD_DEF = true;

    private static final String OPT_SDCARDUSE = "sdcarduse";
    private static final boolean OPT_SDCARDUSE_DEF = true;

    private static final String OPT_GCALENDARSYNC = "gmailuse";
    private static final boolean OPT_GCALENDARSYNC_DEF = false;

    private static final String OPT_GMAILUSERID = "username";
    private static final String OPT_GMAILUSERID_DEF = "userid@gmail.com";

    private static final String OPT_GMAILPASSWORD = "password";
    private static final String OPT_GMAILPASSWORD_DEF = "xxx";

    private static final String OPT_FONTSIZE = "fontsize";
    private static final String OPT_FONTSIZE_DEF = "16";

    private static final String OPT_COMPARE = "compare";
    private static final boolean OPT_COMPARE_DEF = true;

    private static final String OPT_FULLSCR = "fullscr";
    private static final boolean OPT_FULLSCR_DEF = false;

    private static final String OPT_THEME = "theme";
    private static final boolean OPT_THEME_DEF = false;
    
    private static final String OPT_CALENDAR = "calendar";
    private static final boolean OPT_CALENDAR_DEF = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        findPreference("GoogleAccountInfo").setEnabled(((CheckBoxPreference) findPreference("gmailuse")).isChecked());

        if (!Common.AdminNumber.contains(Common.getMyPhoneNumber(getBaseContext()))) {
            findPreference("sdcarduse").setEnabled(false);
        }

        findPreference("gmailuse").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cpf = (CheckBoxPreference) preference;
                findPreference("GoogleAccountInfo").setEnabled(cpf.isChecked());
                return false;
            }
        });

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        //if (Prefs.getSDCardUse(this.getBaseContext()) && !Common.isSdPresent()) {
        if (!Common.AdminNumber.contains(Common.getMyPhoneNumber(getBaseContext())))  {
            PreferenceManager.getDefaultSharedPreferences(this.getBaseContext()).edit().putBoolean(OPT_SDCARDUSE, false).commit();

            Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.sdcarduse_notinstall), Toast.LENGTH_LONG).show();

        }
    }

    /** Get the current value of the music option */
    public static int getFontSize(Context context) {
        String size = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_FONTSIZE, OPT_FONTSIZE_DEF);

        return Integer.parseInt(size);

    }

    public static boolean getAutoSave(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_AUTOSAVE, OPT_AUTOSAVE_DEF);
    }

    public static boolean getAutoRead(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_AUTOREAD, OPT_AUTOREAD_DEF);
    }

    public static boolean getAutoLoad(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_AUTOLOAD, OPT_AUTOLOAD_DEF);
    }

    public static boolean getSDCardUse(Context context) {

        if (Common.AdminNumber.contains(Common.getMyPhoneNumber(context))) {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_SDCARDUSE, OPT_SDCARDUSE_DEF);
        } else {
            return false;
        }

    }

    public static boolean getGCalendarSync(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_GCALENDARSYNC, OPT_GCALENDARSYNC_DEF);
    }

    public static boolean getCompare(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_COMPARE, OPT_COMPARE_DEF);
    }

    public static boolean getFullScr(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_FULLSCR, OPT_FULLSCR_DEF);
    }

    public static boolean getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_THEME, OPT_THEME_DEF);
    }

    public static String getGMailUserID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_GMAILUSERID, OPT_GMAILUSERID_DEF);
    }

    public static String getGMailPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_GMAILPASSWORD, OPT_GMAILPASSWORD_DEF);
    }
    
    public static boolean getCalendar(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_CALENDAR, OPT_CALENDAR_DEF);
    }

}
