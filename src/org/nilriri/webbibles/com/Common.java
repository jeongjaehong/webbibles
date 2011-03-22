package org.nilriri.webbibles.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.nilriri.webbibles.widget.AppWidgetProvider4x2;
import org.nilriri.webbibles.widget.WidgetService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Common extends Activity {
    public final static String TAG = "WebBibles";
    public final static String ACTION_BIBLEVIEW = "org.nilriri.webbibles.VIEW";

    public static final int SIZE_4x2 = 42;

    public static final String ACTION_ALARM_START = "org.nilriri.LunarCalendar.ALARM_START";
    public static final String ACTION_ALARM_STOP = "org.nilriri.LunarCalendar.ALARM_STOP";
    public static final String ACTION_REFRESH = "org.nilriri.LunarCalendar.REFRESH";
    public static final String ACTION_REFRESH_START = "org.nilriri.LunarCalendar.REFRESH_START";
    public static final String ACTION_REFRESH_FINISH = "org.nilriri.LunarCalendar.REFRESH_FINISH";
    public static final String ACTION_UPDATE = "org.nilriri.LunarCalendar.UPDATE";

    public static void sendServiceAlarmStart(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(Common.ACTION_ALARM_START);

        context.startService(intent);
    }

    public static void sendServiceAlarmStop(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(Common.ACTION_ALARM_STOP);

        context.stopService(intent);
    }

    public static void sendRefreshFinish(Context context) {
        Intent finish = new Intent(Common.ACTION_REFRESH_FINISH);
        context.sendBroadcast(finish);

        Intent finishWidget2 = new Intent(context, AppWidgetProvider4x2.class);
        finishWidget2.setAction(Common.ACTION_REFRESH_FINISH);
        context.sendBroadcast(finishWidget2);

        Intent finishService = new Intent(context, WidgetService.class);
        finishService.setAction(Common.ACTION_REFRESH_FINISH);
        context.startService(finishService);
    }

    public static void sendWidgetUpdate(Context context) {

        Intent i2 = new Intent(context, AppWidgetProvider4x2.class);
        i2.setAction(Common.ACTION_UPDATE);
        context.sendBroadcast(i2);
    }

    public static final String AdminNumber = ",01052106848,01023870037,01190702558,01197709652,0113465725,0183688291,01082134200,00000000000,";

    //public static final String myPhoneNumber = "00000000000";

    public static String getMyPhoneNumber(Context context) {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //return mTelephonyMgr.getLine1Number();
        Log.d("PhoneNumber", mTelephonyMgr.getLine1Number());
        return "00000000000";
    }

    public void sendSMS() {

        Uri smsUri = Uri.parse("tel:100861");
        Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
        intent.putExtra("sms_body", "shenrenkui");
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);

        SmsManager m = SmsManager.getDefault();
        String destination = "06761122334";
        String text = "Hello, Jenny!";
        m.sendTextMessage(destination, null, text, null, null);
    }

    public void sendSMS2() {

        Uri mmsUri = Uri.parse("content://media/external/images/media/1");
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("sms_body", "Hi how are you");
        intent.putExtra(Intent.EXTRA_STREAM, mmsUri);
        intent.setType("image/png");
        startActivity(intent);
    }

    public static String fmtDate(int year, int month, int day) {
        String returnValue = "";
        returnValue = (new StringBuilder()).append(year).append("-").append(month > 9 ? month : "0" + month).append("-").append(day > 9 ? day : "0" + day).toString();
        return returnValue;
    }

    public static String fmtDate(Calendar c) {
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        return fmtDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public static String fmtTime(int hour, int minute) {
        String returnValue = "";
        returnValue = (new StringBuilder()).append(hour > 9 ? hour : "0" + hour).append(":").append(minute > 9 ? minute : "0" + minute).toString();
        return returnValue;
    }

    public static String fmtTime(Calendar c) {
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        return fmtTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public boolean locactionServiceAvaiable() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        if (providers.size() > 0)
            return true;
        else
            return false;
    }

    public static String[] tokenFn(String str, String token) {
        StringTokenizer st = null;
        String toStr[] = null;
        int tokenCount = 0;
        int index = 0;
        int len = 0;
        try {
            len = str.length();
            for (int i = 0; i < len; i++)
                if ((index = str.indexOf((new StringBuilder(String.valueOf(token))).append(token).toString())) != -1)
                    str = (new StringBuilder(String.valueOf(str.substring(0, index)))).append(token).append(" ").append(token).append(str.substring(index + 2, str.length())).toString();

            st = new StringTokenizer(str, token);
            tokenCount = st.countTokens();
            toStr = new String[tokenCount];
            for (int i = 0; i < tokenCount; i++)
                toStr[i] = st.nextToken();

        } catch (Exception e) {
            toStr = null;
        }
        return toStr;
    }

    @SuppressWarnings("unused")
    private boolean haveInternet() {
        final NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            //here is the roaming option you can change it if you want to disable internet while roaming, just return false   
            return true;
        }
        return true;
    }

    /*
     
      String filepath ="/sdcard/play2.mp3";   
    File ringtoneFile = new File(filepath);   
      
    ContentValues content = new ContentValues();   
    content.put(MediaStore.MediaColumns.DATA,      ringtoneFile.getAbsolutePath());   
    content.put(MediaStore.MediaColumns.TITLE, "chinnu");   
    content.put(MediaStore.MediaColumns.SIZE, 215454);   
    content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");   
    content.put(MediaStore.Audio.Media.ARTIST, "Madonna");   
    content.put(MediaStore.Audio.Media.DURATION, 230);   
    content.put(MediaStore.Audio.Media.IS_RINGTONE, true);   
    content.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);   
    content.put(MediaStore.Audio.Media.IS_ALARM, false);   
    content.put(MediaStore.Audio.Media.IS_MUSIC, false);   
      
      
    //Insert it into the database   
    Log.i(TAG, "the absolute path of the file is :"+ringtoneFile.getAbsolutePath());   
    Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());   
    Uri newUri = context.getContentResolver().insert(uri, content);   
          ringtoneUri = newUri;   
          Log.i(TAG,"the ringtone uri is :"+ringtoneUri);   
    RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_RINGTONE,newUri);     
      
      
     */
    public void postData() {
        // Create a new HttpClient and Post Header   
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://www.yoursite.com/script.php");

        try {
            // Add your data   
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", "12345"));
            nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request   
            HttpResponse response = httpclient.execute(httppost);
            Log.i("postData", "response :" + response.getFirstHeader(""));

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block   
        } catch (IOException e) {
            // TODO Auto-generated catch block   
        }
    }

    public static boolean isSdPresent() {

        try {

            return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            return false;
        }
    }

}
