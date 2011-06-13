package org.nilriri.webbibles.com;

import org.nilriri.webbibles.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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

    private static final String OPT_BACKGROUND_COLOR = "backgroundcolor";

    private static final String OPT_FONT_COLOR = "fontcolor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        if (!Common.AdminNumber.contains(Common.getMyPhoneNumber(getBaseContext()))) {
            findPreference("sdcarduse").setEnabled(false);
        }

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        //if (Prefs.getSDCardUse(this.getBaseContext()) && !Common.isSdPresent()) {
        if (!Common.AdminNumber.contains(Common.getMyPhoneNumber(getBaseContext()))) {
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

    public static boolean getCompare(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_COMPARE, OPT_COMPARE_DEF);
    }

    public static boolean getFullScr(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_FULLSCR, OPT_FULLSCR_DEF);
    }

    public static boolean getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_THEME, OPT_THEME_DEF);
    }

    public static boolean getCalendar(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_CALENDAR, OPT_CALENDAR_DEF);
    }

    public static int getBackgroundColor(Context context) {
        try {
            String c = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_BACKGROUND_COLOR, Color.WHITE + "");
            switch (Integer.parseInt(c)) {
                case 0:
                    return Color.BLACK;
                case 1:
                    return Color.DKGRAY;
                case 2:
                    return Color.GRAY;
                case 3:
                    return Color.LTGRAY;
                default:
                case 4:
                    return Color.WHITE;
                case 5:
                    return Color.RED;
                case 6:
                    return Color.GREEN;
                case 7:
                    return Color.BLUE;
                case 8:
                    return Color.YELLOW;
                case 9:
                    return Color.CYAN;
                case 10:
                    return Color.MAGENTA;
            }
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    public static int getFontColor(Context context) {

        try {
            String c = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_FONT_COLOR, Color.DKGRAY + "");
            switch (Integer.parseInt(c)) {
                case 0:
                    return Color.BLACK;
                default:
                case 1:
                    return Color.DKGRAY;
                case 2:
                    return Color.GRAY;
                case 3:
                    return Color.LTGRAY;
                case 4:
                    return Color.WHITE;
                case 5:
                    return Color.RED;
                case 6:
                    return Color.GREEN;
                case 7:
                    return Color.BLUE;
                case 8:
                    return Color.YELLOW;
                case 9:
                    return Color.CYAN;
                case 10:
                    return Color.MAGENTA;
            }
        } catch (Exception e) {
            return Color.DKGRAY;
        }
    }
}
