package org.nilriri.webbibles.tools;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BibleDao;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class DownloadBible extends Activity implements OnClickListener {
    private static final String TAG = "CopyBible";

    public static final int MENU_ITEM_DELNOTE = Menu.FIRST;

    private ListView mListView = null;
    private int mVersion = -1;
    private int mBook = -1;
    private int mChapter = -1;

    private String mBaseUrl;

    private String[] mBooks;
    private String[] mVersions;
    private String[] mKVersions;
    private String[] mUrls;
    private String[] mChapters;

    private BibleDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.datamanager_target);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mVersion = getIntent().getIntExtra("VERSION", 0);

        mListView = (ListView) findViewById(R.id.ContentsListView);

        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.all_testment, R.layout.multiple_choice);
        mListView.setAdapter(adapter);

        mListView.setAdapter(adapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        findViewById(R.id.btn_allcheck).setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);

        mVersions = getResources().getStringArray(R.array.site1version);
        mKVersions = getResources().getStringArray(R.array.site1versionkor);
        mUrls = getResources().getStringArray(R.array.site1);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public class listOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            Log.e(TAG, "onListItemClick position = " + pos);
            Log.e(TAG, "onListItemClick id = " + id);

        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.btn_allcheck:

                for (int i = 0; i < this.mListView.getCount(); i++) {
                    mListView.setItemChecked(i, !mListView.isItemChecked(i));
                }

                break;
            case R.id.btn_start:

                StartDownload();

                this.setResult(Activity.RESULT_OK);

                this.finish();

                break;

        }
    }

    public void StartDownload() {

        // 웹에서 조회할 주소 생성.
        mBaseUrl = mUrls[mVersion].replace("$VERSION$", mVersions[mVersion]);
        mChapters = getResources().getStringArray(R.array.chapterlist1 + mBook);

        if (mVersion >= 0 && 6 >= mVersion) {
            mBooks = getResources().getStringArray(R.array.site1book);
        } else if (mVersion >= 7) {
            mBooks = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66" };
        }

        Thread thr = new Thread(null, mTask, "Bible_Downloading");
        thr.start();
    }

    Runnable mTask = new Runnable() {

        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private NotificationManager mNM;

        public void run() {

            mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            try {

                if (Prefs.getSDCardUse(getBaseContext())) {
                    dao = new BibleDao(DownloadBible.this, null, true);
                } else {
                    dao = new BibleDao(DownloadBible.this, null, false);
                }

                for (mBook = 0; mBook < mListView.getCount(); mBook++) {

                    if (!mListView.isItemChecked(mBook))
                        continue;

                    String[] chapter = Common.tokenFn(mChapters[mBook], ",");

                    for (mChapter = 0; mChapter < chapter.length; mChapter++) {

                        if (dao.queryExistsContents(mVersion, mBook, mChapter)) {
                            CharSequence title2 = mKVersions[mVersion] + " Exists contents...";
                            CharSequence msg2 = mListView.getItemAtPosition(mBook) + " " + (mChapter + 1) + "/" + chapter.length;

                            Notification notification = new Notification(android.R.drawable.stat_notify_sync, "Download Complete...", System.currentTimeMillis());
                            PendingIntent contentIntent = PendingIntent.getActivity(DownloadBible.this, mVersion, new Intent(DownloadBible.this, DownloadBible.class).putExtra("VERSION", mVersion), 0);
                            notification.setLatestEventInfo(DownloadBible.this, title2, msg2, contentIntent);
                            mNM.notify(mVersion, notification);

                            continue;
                        }

                        String url = mBaseUrl.replace("$BOOK$", mBooks[mBook]).replace("$CHAP$", (mChapter + 1) + "");

                        Log.e(TAG, "Download url = " + url);

                        HttpGet httpget = new HttpGet(url);
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();
                        Content = Client.execute(httpget, responseHandler);

                        if (mVersion >= 0 && 6 >= mVersion) {
                            parsingBibleSource(Content);
                        } else if (mVersion >= 7) {
                            parsingC3TVBible(Content);
                        }

                        CharSequence title = mKVersions[mVersion] + " Downloading...";
                        CharSequence msg = mListView.getItemAtPosition(mBook) + " " + (mChapter + 1) + "/" + chapter.length;
                        //Toast.makeText(DownloadBible.this, msg, Toast.LENGTH_LONG).show();

                        Notification notification = new Notification(android.R.drawable.stat_notify_sync, "Download Complete...", System.currentTimeMillis());
                        PendingIntent contentIntent = PendingIntent.getActivity(DownloadBible.this, mVersion, new Intent(DownloadBible.this, DownloadBible.class).putExtra("VERSION", mVersion), 0);
                        notification.setLatestEventInfo(DownloadBible.this, title, msg, contentIntent);
                        mNM.notify(mVersion, notification);

                    }
                }

                dao.CloseDatabase();

            } catch (ClientProtocolException e) {
                Log.e(TAG, "ClientProtocolException = " + e.getMessage());

            } catch (IOException e) {
                Log.e(TAG, "ClientProtocolException = " + e.getMessage());

            } finally {
                mNM.cancelAll();
            }

        }
    };

    private boolean parsingBibleSource(String HTMLSource) {
        Log.d(TAG, "RegExpr Result^^ = " + HTMLSource);
        HTMLSource = HTMLSource.substring(HTMLSource.indexOf("<td id=\"tdBible1\""));
        Log.d(TAG, "RegExpr Result&& = " + HTMLSource);
        Log.d(TAG, "RegExpr Result HTMLSource.indexOf(\"tdBible1\") = " + HTMLSource.indexOf("tdBible1"));
        //HTMLSource = HTMLSource.substring(HTMLSource.indexOf("tdBible1"));
        //Content = Content.substring(Content.indexOf("tdBible2"));

        Log.d(TAG, "RegExpr Result00 = " + HTMLSource);
        //HTMLSource = HTMLSource.substring(HTMLSource.indexOf(">") + 1);
        HTMLSource = HTMLSource.substring(0, HTMLSource.indexOf("/td>") - 1);
        Log.d(TAG, "RegExpr Result11 = " + HTMLSource);

        HTMLSource = HTMLSource.replace("<br>", "\n");
        HTMLSource = HTMLSource.replace("<BR>", "\n");
        HTMLSource = HTMLSource.replace("&nbsp;&nbsp;&nbsp;", "##");
        Log.d(TAG, "RegExpr Result22 = " + HTMLSource);
        Matcher matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher("");

        StringBuffer result = new StringBuffer();
        matcher.reset(HTMLSource);
        while (matcher.find()) {
            matcher.appendReplacement(result, "");
        }
        matcher.appendTail(result);

        HTMLSource = result.toString();

        String WebData[] = Common.tokenFn(HTMLSource, "\n");

        dao.insert(mVersions[mVersion], mKVersions[mVersion], mVersion, mBook, mChapter, WebData);

        return true;

    }

    private boolean parsingC3TVBible(String HTMLSource) {

        //HTMLSource = HTMLSource.substring(0, HTMLSource.indexOf("<object"));
        HTMLSource = HTMLSource.substring(HTMLSource.indexOf("<!-- 본문전체 -->") + 17, HTMLSource.indexOf("<!--//본문전체 -->"));

        HTMLSource = HTMLSource.replace(".</span>", "##").replace("&nbsp;", "").replace("\t", " ");

        Matcher matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher("");

        StringBuffer result = new StringBuffer();
        matcher.reset(HTMLSource);
        while (matcher.find()) {
            matcher.appendReplacement(result, "");
        }
        matcher.appendTail(result);

        HTMLSource = result.toString().trim();
        //Log.d(TAG, "RegExpr Result2 = " + Content);

        matcher = Pattern.compile("[\\r|\\n|\\r\\n]").matcher("");

        matcher.reset(HTMLSource);

        result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, "||");
        }
        matcher.appendTail(result);

        HTMLSource = result.toString().replace("## ||", "##").replace("##||", "##");
        //Log.d(TAG, "RegExpr Result3 = " + Content);

        // 공백제거
        matcher = Pattern.compile("(\\s)+").matcher("");

        matcher.reset(HTMLSource);

        result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, " ");
        }
        matcher.appendTail(result);

        HTMLSource = result.toString().replace("|| ", "||");
        //Log.d(TAG, "RegExpr Result4 = " + Content);

        // 공백제거
        matcher = Pattern.compile("[||]+").matcher("");

        matcher.reset(HTMLSource);

        result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, "||");
        }
        matcher.appendTail(result);

        HTMLSource = result.toString();
        //Log.d(TAG, "RegExpr Result5 = " + Content);

        HTMLSource = result.toString().replace("## ||", "##").replace("##||", "##");

        String WebData[] = Common.tokenFn(HTMLSource, "||");

        dao.insert(mVersions[mVersion], mKVersions[mVersion], mVersion, mBook, mChapter, WebData);

        return true;
    }

}
