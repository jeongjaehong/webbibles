package org.nilriri.webbibles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BibleDao;
import org.nilriri.webbibles.dao.BookmarkDao;
import org.nilriri.webbibles.dao.FavoritesDao;
import org.nilriri.webbibles.dao.Constants.Bibles;
import org.nilriri.webbibles.dao.Constants.FavoriteGroup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class BibleViewer extends Activity implements OnTouchListener {

    private static final String TAG = "BibleView";

    private static final String BOOK = "book";
    private static final String CHAPTER = "chapter";
    private static final String VERSE = "verse";

    //protected SmartAdView mAdView;

    /**
     * Standard projection for the interesting columns of a normal note.
     */

    public static final int COLUMN_INDEX_ID = 0;
    public static final int COLUMN_INDEX_BOOK = 1;
    public static final int COLUMN_INDEX_CHAPTER = 2;
    public static final int COLUMN_INDEX_VERSE = 3;
    public static final int COLUMN_INDEX_VERSESTR = 4;
    public static final int COLUMN_INDEX_VERSELIST = 2;
    public static final int COLUMN_INDEX_SUBJECT = 1;

    private ListView mListView = null;
    private OldEvent oldEvent;
    private int mVersion = 0;
    private int mCompletecount = 0;
    private int mVersion2 = 0;
    private int mBook = 0;
    private int mChapter = 0;
    private int mVerse = 0;
    private ArrayList<Long> mCheck = new ArrayList<Long>();
    private String mBaseUrl = "";
    private String mBaseUrl2 = "";
    private String[] mBooks;
    private String[] mVersions;
    private String[] mKVersions;
    private String[] mUrls;

    private String[] mAllTestment;
    private String[] mEngTestment;
    private String[] mBibleShortName;
    //private BibleInternalDao dao = new BibleInternalDao(this, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    private BibleDao dao;// = new BibleDao(this, null, Prefs.getSDCardUse(this));
    private BookmarkDao markDao;// = new BookmarkDao(this, null, Prefs.getSDCardUse(this));
    private FavoritesDao favoritesDao;// = new BookmarkDao(this, null, Prefs.getSDCardUse(this));

    // Menu item ids    
    public static final int MENU_ITEM_SAVE = Menu.FIRST;
    public static final int MENU_ITEM_PREV = Menu.FIRST + 1;
    public static final int MENU_ITEM_NEXT = Menu.FIRST + 2;
    public static final int MENU_LOAD_WEB = Menu.FIRST + 3;
    public static final int MENU_LOAD_DB = Menu.FIRST + 4;
    public static final int MENU_ITEM_NOTELIST = Menu.FIRST + 5;
    public static final int MENU_ITEM_ADDMARK = Menu.FIRST + 6;
    public static final int MENU_ITEM_MARKLIST = Menu.FIRST + 7;
    public static final int MENU_ITEM_ADDNOTE = Menu.FIRST + 8;
    public static final int MENU_ITEM_SENDSMS = Menu.FIRST + 9;
    public static final int MENU_ITEM_ADDFAVORITES = Menu.FIRST + 10;
    public static final int MENU_ITEM_FAVORITELIST = Menu.FIRST + 11;
    public static final int MENU_ITEM_SHARE = Menu.FIRST + 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Prefs.getFullScr(getBaseContext())) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        dao = new BibleDao(this, null, Prefs.getSDCardUse(this));
        markDao = new BookmarkDao(this, null, Prefs.getSDCardUse(this));
        favoritesDao = new FavoritesDao(this, null, Prefs.getSDCardUse(this));

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        //if (Prefs.getTheme(this)) {
        //setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        //} else {
        //setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        //}
        setContentView(R.layout.bible_view);

        //mAdView = (SmartAdView) findViewById(R.id.ad_view);

        // 역본과 읽은 성경선택
        mBook = getIntent().getIntExtra("BOOK", 0);
        mChapter = getIntent().getIntExtra("CHAPTER", 0);
        mVerse = getIntent().getIntExtra("VERSE", 0);

        int defVersion = getPreferences(MODE_PRIVATE).getInt("version", 0);
        mVersion = getIntent().getIntExtra("VERSION", defVersion);
        mVersion2 = getIntent().getIntExtra("VERSION2", mVersion);

        // 리소스에서 역본명, 웹주소, 성경목록등을 로드한다.
        if (mVersion >= 0 && 6 >= mVersion) {
            mBooks = getResources().getStringArray(R.array.site1book);
        } else if (mVersion >= 7) {

            mBooks = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66" };
        }

        mVersions = getResources().getStringArray(R.array.site1version);
        mKVersions = getResources().getStringArray(R.array.site1versionkor);
        mUrls = getResources().getStringArray(R.array.site1);

        mAllTestment = getResources().getStringArray(R.array.all_testment);
        mEngTestment = getResources().getStringArray(R.array.eng_testment);
        mBibleShortName = getResources().getStringArray(R.array.short_biblenames);

        // 웹에서 조회할 주소 생성.
        mBaseUrl = mUrls[mVersion].replace("$VERSION$", mVersions[mVersion]);
        mBaseUrl2 = mUrls[mVersion2].replace("$VERSION$", mVersions[mVersion2]);

        // 역본명으로 제목설정.
        if (Prefs.getCompare(this)) {
            this.setTitle(mKVersions[mVersion] + " vs " + mKVersions[mVersion2]);
        } else {
            this.setTitle(mKVersions[mVersion]);
        }

        mListView = (ListView) findViewById(R.id.ContentsListView);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnTouchListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        Spinner verse_spinner = (Spinner) findViewById(R.id.verses);
        verse_spinner.setOnItemSelectedListener(new VerseOnItemSelectedListener());
        //verse_spinner.setOnCreateContextMenuListener(this);

        Spinner chapter_spinner = (Spinner) findViewById(R.id.chapters);
        chapter_spinner.setOnItemSelectedListener(new ChapterOnItemSelectedListener());
        //chapter_spinner.setOnCreateContextMenuListener(this);

        Spinner spin_books = (Spinner) findViewById(R.id.books);
        ArrayAdapter<CharSequence> adapter_books = ArrayAdapter.createFromResource(this, R.array.all_testment, android.R.layout.simple_spinner_item);
        adapter_books.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_books.setAdapter(adapter_books);

        spin_books.setOnItemSelectedListener(new BookOnItemSelectedListener());
        spin_books.setSelection(mBook);

        //String url = mBaseUrl.replace("$BOOK$", mBooks[mBook]).replace("$CHAP$", "1");

        //Log.e(TAG, "url====" + url);

        //Toast.makeText(getBaseContext(), url, Toast.LENGTH_LONG).show();

        //grabURL(url);
        //grabURL("http://www.bskorea.or.kr/infobank/korSearch/korbibReadpage.aspx?version=CEV&BOOK=psa&chap=23&sec=1&cVersion=&fontString=12px&fontSize=1");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Common.TAG, "============onResume========");
        if (getIntent().hasExtra("BPLANT")) {
            String names[] = getIntent().getStringArrayExtra("BPLANT");
            final String indexs[] = getIntent().getStringArrayExtra("BPLANI");

            if (names.length > 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("읽을 성경을 선택하십시오.");
                builder.setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        try {

                            Log.d(Common.TAG, "============onClick========" + which);
                            String index = indexs[which].replace("bindex:", "");
                            String data[] = Common.tokenFn(index, ",");

                            Log.d(Common.TAG, "==============" + which);

                            mBook = Integer.parseInt(data[0]);
                            mChapter = Integer.parseInt(data[1]);

                            ((Spinner) findViewById(R.id.books)).setSelection(mBook);

                            ReloadBibleContents();
                        } catch (Exception e) {
                            Log.d(Common.TAG, "error=" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }).show();

            }
        } else {
            Log.d(Common.TAG, "============ReloadBibleContents========");
            this.ReloadBibleContents();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Common.TAG, "============onPause========");
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(BOOK, ((Spinner) findViewById(R.id.books)).getSelectedItemPosition()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(CHAPTER, ((Spinner) findViewById(R.id.chapters)).getSelectedItemPosition()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(VERSE, this.mVerse).commit();
    }

    public class listOnItemClickListener implements OnItemClickListener {

        @SuppressWarnings("unchecked")
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

            if (pos < 0)
                return;

            Object obj = mListView.getItemAtPosition(pos);
            if (obj.getClass().toString().indexOf("SQLiteCursor") >= 0) {

                Cursor c = (Cursor) obj;
                if (!c.isNull(Bibles.COL_VERSE)) {
                    mVerse = c.getInt(Bibles.COL_VERSE);

                } else {
                    mVerse = 0;
                }

                if (!mCheck.contains(id)) {
                    mCheck.add(id);

                    //view.findViewById(R.id.biblecontents).setSelected(true);//.setBackgroundColor(android.R.color.background_dark);
                    view.findViewById(R.id.flag).setVisibility(View.VISIBLE);

                } else {//if (mCheck.contains(id)) {
                    mCheck.remove(id);

                    // view.findViewById(R.id.biblecontents).setSelected(false);//.setBackgroundColor(android.R.color.background_light);
                    view.findViewById(R.id.flag).setVisibility(View.INVISIBLE);

                }
                if (mCheck.size() > 2) {
                    Toast.makeText(BibleViewer.this, "두개 이상 선택불가.(시작과 끝만 지정)", Toast.LENGTH_LONG).show();
                    mCheck.remove(id);

                    // view.findViewById(R.id.biblecontents).setSelected(false);//.setBackgroundColor(android.R.color.background_light);
                    view.findViewById(R.id.flag).setVisibility(View.INVISIBLE);

                }

                Log.e(TAG, "select verse====" + mVerse);
                Log.e(TAG, "select id====" + id);
                Log.e(TAG, "select view.getId()====" + view.getId());
                Log.e(TAG, "select mCheck====" + mCheck.toString());

            } else {
                //HashMap<String, String> obj2 = (HashMap<String, String>) obj;
                //HashMap<String, String> map =  ((HashMap<String, String>) obj).get("Number");
                String verse = ((HashMap<String, String>) obj).get("Number");//map.get("Number");

                if ("".equals(verse))
                    mVerse = 0;
                else
                    mVerse = Integer.parseInt(verse.trim()) - 1;
            }

        }
    }

    public String getSelectMsg() {
        String msg = "";

        String from = "";
        String to = "";
        for (int i = 0; i < mListView.getCount(); i++) {
            //if (this.mListView.getChildAt(i).findViewById(R.id.flag).getVisibility() == View.VISIBLE) {
            Long id = new Long(mListView.getItemIdAtPosition(i));// .findViewById(R.id.versestr)).getText().toString().trim();
            Log.d("xxx", "id==>" + id);

            if (mCheck.contains(id)) {
                Log.d("yyy", "id==>" + id);
                if ("".equals(from)) {
                    from = ((Cursor) mListView.getItemAtPosition(i)).getString(6);
                } else {
                    to = ((Cursor) mListView.getItemAtPosition(i)).getString(6);
                    //to = ((TextView) mListView.getChildAt(i).findViewById(R.id.versestr)).getText().toString().trim();
                }
                Log.d("zzz", "from==>" + from);
                Log.d("zzz", "to==>" + to);
            }
        }
        Cursor c = null;
        if (!"".equals(from) && !"".equals(to)) {
            c = dao.queryBibleVerse(mVersion, mBook, mChapter, from, to);
        } else {
            c = dao.queryBibleVerse(mVersion, mBook, mChapter, mVerse);
        }
        // mBibleShortName

        while (c.moveToNext()) {
            msg += " " + c.getString(2);
        }

        if (!"".equals(from) && !"".equals(to)) {
            msg += " (" + mBibleShortName[mBook] + " " + (mChapter + 1) + ":" + from + "~" + to + ")";
        } else {
            msg += " (" + mBibleShortName[mBook] + " " + (mChapter + 1) + ":" + mVerse + ")";
        }
        c.close();
        mCheck.clear();
        return msg;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int offset = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldEvent = new OldEvent(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:

                Log.d(TAG, "oldEvent():" + oldEvent.toString());
                Log.d(TAG, "upEvent():" + event.toString());
                if (event.getX() - oldEvent.getX() > 30 && Math.abs(event.getY() - oldEvent.getY()) < 70) {
                    offset = -1;
                } else if (event.getX() - oldEvent.getX() < -30 && Math.abs(event.getY() - oldEvent.getY()) < 70) {
                    offset = 1;
                } else {
                    return false;
                }
                // 초기화.
                oldEvent = new OldEvent(event.getX(), event.getY());

                Spinner spinchapter = (Spinner) findViewById(R.id.chapters);

                int newPosition = spinchapter.getSelectedItemPosition() + offset;
                if (spinchapter.getCount() > newPosition && newPosition >= 0) {
                    mVerse = 0;
                    spinchapter.setSelection(newPosition);
                } else {
                    // 이전및 다음장이 없을때는 이전또는 다음성경으로 이동한다. 
                    Spinner bookspin = (Spinner) findViewById(R.id.books);
                    if (newPosition < 0) {
                        // 이전성경으로
                        if (mBook > 0) {
                            mChapter = 0;
                            bookspin.setSelection(mBook - 1);
                        }
                    } else if (newPosition >= spinchapter.getCount()) {
                        // 다음성경으로
                        if (mBook < bookspin.getCount()) {
                            mChapter = 0;
                            bookspin.setSelection(mBook + 1);
                        }
                    }
                }

                break;
            default:

                return false;

        }
        return false;
    }

    private class GrabURL extends AsyncTask<String, Void, Void> {
        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private int TargetVersion;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(BibleViewer.this);

        protected void onPreExecute() {
            Dialog.setMessage("Loading Contents..");
            Dialog.show();
        }

        protected Void doInBackground(String... urls) {
            try {
                TargetVersion = Integer.parseInt(urls[1]);
                HttpGet httpget = new HttpGet(urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);

            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();
                cancel(true);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            Dialog.dismiss();
            if (Error != null) {
                Toast.makeText(BibleViewer.this, Error, Toast.LENGTH_SHORT).show();
            } else {
                boolean ret = false;

                if (mVersion >= 0 && 6 >= mVersion) {
                    ret = parsingBibleSource(Content);
                } else if (mVersion >= 7) {
                    ret = parsingC3TVBible(Content);
                }

                if (!ret) {
                    Toast.makeText(BibleViewer.this, mBooks[mBook] + " Load error.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private boolean parsingC3TVBible(String HTMLSource) {

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
            Log.d(TAG, "RegExpr Result2 = " + HTMLSource);

            matcher = Pattern.compile("[\\r|\\n|\\r\\n]").matcher("");

            matcher.reset(HTMLSource);

            result = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(result, "||");
            }
            matcher.appendTail(result);

            HTMLSource = result.toString().replace("## ||", "##").replace("##||", "##");
            Log.d(TAG, "RegExpr Result3 = " + HTMLSource);

            // 공백제거
            matcher = Pattern.compile("(\\s)+").matcher("");

            matcher.reset(HTMLSource);

            result = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(result, " ");
            }
            matcher.appendTail(result);

            HTMLSource = result.toString().replace("|| ", "||");
            Log.d(TAG, "RegExpr Result4 = " + HTMLSource);

            // 공백제거
            matcher = Pattern.compile("[||]+").matcher("");

            matcher.reset(HTMLSource);

            result = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(result, "||");
            }
            matcher.appendTail(result);

            HTMLSource = result.toString();
            Log.d(TAG, "RegExpr Result5 = " + HTMLSource);

            HTMLSource = result.toString().replace("## ||", "##").replace("##||", "##");

            String WebData[] = Common.tokenFn(HTMLSource, "||");

            ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
            ArrayList<HashMap<String, String>> mList2 = new ArrayList<HashMap<String, String>>();

            for (int y = 0; y < WebData.length; y++) {
                HashMap<String, String> item = new HashMap<String, String>();
                HashMap<String, String> item2 = new HashMap<String, String>();

                String z[] = Common.tokenFn(WebData[y], "##");

                if ("".equals(z[0].trim()) || z == null)
                    continue;

                if (z.length == 1) {
                    item.put("Number", "");
                    item.put("Contents", z[0].trim());
                } else if (z.length == 2) {
                    if (!"".equals(z[0].trim())) {
                        item2.put("Number", z[0].trim());
                        mList2.add(item2);
                    }

                    item.put("Number", z[0].trim());
                    item.put("Contents", z[1].trim());
                } else {
                    continue;
                }
                mList.add(item);

            }

            //SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });

            mListView.setAdapter(adapter);

            Spinner spin_verse = (Spinner) findViewById(R.id.verses);
            SimpleAdapter adapter_verse = new SimpleAdapter(getBaseContext(), mList2, android.R.layout.simple_spinner_item, new String[] { "Number" }, new int[] { android.R.id.text1 });
            adapter_verse.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spin_verse.setAdapter(adapter_verse);

            if (Prefs.getAutoSave(BibleViewer.this)) {
                dao.insert(mVersions[TargetVersion], mKVersions[TargetVersion], TargetVersion, mBook, mChapter, mListView);
            }

            mCompletecount++;

            if (mCompletecount > 1) {
                if (mVersion != mVersion2 && mVersion2 >= 0 && Prefs.getCompare(getBaseContext())) {
                    mListView.setAdapter(null);
                    loadDBContents();
                }
            }

            return true;
        }

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
            /* while (HTMLSource.indexOf("<div") >= 0 && HTMLSource.indexOf(">", HTMLSource.indexOf("<div")) >= 0) {

                 String target = HTMLSource.substring(HTMLSource.indexOf("<div"), HTMLSource.indexOf(">", HTMLSource.indexOf("<div")) + 1);
                 HTMLSource = HTMLSource.replace(target, "( ");

             }
             HTMLSource = HTMLSource.replace("</div>", " )");
             
            */
            Matcher matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher("");

            StringBuffer result = new StringBuffer();
            matcher.reset(HTMLSource);
            while (matcher.find()) {
                matcher.appendReplacement(result, "");
            }
            matcher.appendTail(result);

            HTMLSource = result.toString();

            String WebData[] = Common.tokenFn(HTMLSource, "\n");

            ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
            ArrayList<HashMap<String, String>> mList2 = new ArrayList<HashMap<String, String>>();

            for (int y = 0; y < WebData.length; y++) {
                HashMap<String, String> item = new HashMap<String, String>();
                HashMap<String, String> item2 = new HashMap<String, String>();

                String z[] = Common.tokenFn(WebData[y], "##");

                if ("".equals(z[0].trim()) || z == null)
                    continue;

                if (z.length == 1) {
                    item.put("Number", "");
                    item.put("Contents", z[0]);
                } else if (z.length == 2) {
                    if (!"".equals(z[0].trim())) {
                        item2.put("Number", z[0]);
                        mList2.add(item2);
                    }

                    item.put("Number", z[0]);
                    item.put("Contents", z[1]);
                } else {
                    continue;
                }
                mList.add(item);

            }

            //SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });

            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });

            mListView.setAdapter(adapter);

            if (Prefs.getAutoSave(BibleViewer.this)) {
                dao.insert(mVersions[mVersion], mKVersions[mVersion], mVersion, mBook, mChapter, mListView);
            }

            Spinner spin_verse = (Spinner) findViewById(R.id.verses);
            SimpleAdapter adapter_verse = new SimpleAdapter(getBaseContext(), mList2, android.R.layout.simple_spinner_item, new String[] { "Number" }, new int[] { android.R.id.text1 });
            adapter_verse.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spin_verse.setAdapter(adapter_verse);

            return true;
        }

        /*
           
               private boolean parsingC3TVBible2(String HTMLSource) {

                   HTMLSource = HTMLSource.substring(0, HTMLSource.indexOf("<object"));

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

                   String WebData[] = Common.tokenFn(HTMLSource, "||");

                   ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
                   ArrayList<HashMap<String, String>> mList2 = new ArrayList<HashMap<String, String>>();

                   for (int y = 0; y < WebData.length; y++) {
                       HashMap<String, String> item = new HashMap<String, String>();
                       HashMap<String, String> item2 = new HashMap<String, String>();

                       String z[] = Common.tokenFn(WebData[y], "##");

                       if ("".equals(z[0].trim()) || z == null)
                           continue;

                       if (z.length == 1) {
                           item.put("Number", "");
                           item.put("Contents", z[0].trim());
                       } else if (z.length == 2) {
                           if (!"".equals(z[0].trim())) {
                               item2.put("Number", z[0].trim());
                               mList2.add(item2);
                           }

                           item.put("Number", z[0].trim());
                           item.put("Contents", z[1].trim());
                       } else {
                           continue;
                       }
                       mList.add(item);

                   }

                   //SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                   SimpleAdapter adapter = null;
                   switch (Prefs.getFontSize(getBaseContext())) {
                       case 12:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item12, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 14:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item14, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 16:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item16, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 18:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item18, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 20:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item20, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 22:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item22, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 24:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item24, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 26:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item26, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       case 28:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item28, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });
                           break;
                       default:
                           adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_item, new String[] { "Number", "Contents" }, new int[] { R.id.versestr, R.id.contents });

                           break;
                   }
                   mListView.setAdapter(adapter);

                   if (Prefs.getAutoSave(BibleViewer.this)) {
                       dao.insert(mVersions[mVersion], mKVersions[mVersion], mVersion, mBook, mChapter, mListView);
                   }

                   Spinner spin_verse = (Spinner) findViewById(R.id.verses);
                   SimpleAdapter adapter_verse = new SimpleAdapter(getBaseContext(), mList2, android.R.layout.simple_spinner_item, new String[] { "Number" }, new int[] { android.R.id.text1 });
                   adapter_verse.setDropDownViewResource(android.R.layout.simple_spinner_item);
                   spin_verse.setAdapter(adapter_verse);

                   return true;
               }
        */
    }

    private void ReloadChapterList() {

        Log.e(TAG, "===============ReloadChapterList====");
        //String chapterlist[] = getResources().getStringArray(R.array.chapterlist1);

        //String chapter[] = Common.tokenFn(chapterlist[mBook], ",");

        //ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();

        //for (int y = 0; y < chapter.length; y++) {
        //HashMap<String, String> item = new HashMap<String, String>();

        //item.put("Chapter", chapter[y]);

        //mList.add(item);
        //}

        Spinner chapter_spinner = (Spinner) findViewById(R.id.chapters);
        ArrayAdapter<CharSequence> adapter_chapters = ArrayAdapter.createFromResource(this, R.array.chapterlist1 + this.mBook, android.R.layout.simple_spinner_item);
        adapter_chapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chapter_spinner.setAdapter(adapter_chapters);

        /*
        //SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, android.R.layout.simple_spinner_item, new String[] { "Chapter" }, new int[] { android.R.id.text1 });
        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, R.layout.chapter_dropdown_item, new String[] { "Chapter" }, new int[] { android.R.id.text1 });
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.chapter_dropdown_item);
        Spinner chapter_spinner = (Spinner) findViewById(R.id.chapters);
        chapter_spinner.setAdapter(adapter);
        //chapter_spinner.setScrollBarStyle(android.R.style.Widget_CompoundButton_RadioButton);
        */

        if (mChapter < 0 || mChapter >= chapter_spinner.getCount()) {
            mChapter = 0;
        }

        chapter_spinner.setSelection(mChapter);
        ReloadBibleContents();

    }

    private void ReloadBibleContents() {

        Log.d(Common.TAG, "error=version" + mVersion + ", book=" + mBook + ", chapter=" + mChapter);

        if (dao.queryExistsContents(mVersion, mBook, mChapter)) {

            loadDBContents();

        } else {
            if (Prefs.getAutoLoad(this)) {
                loadWebContents();
            } else {
                String msg = getResources().getString(R.string.not_found_msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        }

        mCheck.clear();

        Spinner verse_spinner = (Spinner) findViewById(R.id.verses);

        if (mVerse < 0)
            mVerse = 0;

        if (mVerse >= verse_spinner.getCount())
            mVerse = verse_spinner.getCount() - 1;
        verse_spinner.setSelection(mVerse);

    }

    public class BookOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (pos < 0)
                return;

            mBook = pos;

            ReloadChapterList();

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class ChapterOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (pos < 0)
                return;

            mChapter = pos;

            ReloadBibleContents();

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class VerseOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (mListView.getCount() <= 0 || pos < 0 || mListView.getCount() <= pos) {
                return;
            }

            mVerse = pos;
            mListView.setSelection(pos);
            mListView.setFocusableInTouchMode(true);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item1 = menu.add(0, MENU_ITEM_PREV, 0, R.string.menu_previous);
        item1.setIcon(android.R.drawable.ic_media_rew);

        MenuItem item2 = menu.add(0, MENU_ITEM_SAVE, 0, R.string.menu_save);
        item2.setIcon(android.R.drawable.ic_menu_save);

        MenuItem item3 = menu.add(0, MENU_ITEM_NEXT, 0, R.string.menu_next);
        item3.setIcon(android.R.drawable.ic_media_ff);

        MenuItem item4 = menu.add(0, MENU_LOAD_WEB, 0, R.string.menu_loadweb);
        item4.setIcon(R.drawable.ic_menu_refresh);

        if (dao.queryExistsContents(mVersion, mBook, mChapter)) {
            MenuItem item5 = menu.add(0, MENU_LOAD_DB, 0, R.string.menu_loaddb);
            item5.setIcon(android.R.drawable.ic_menu_upload);
        }

        MenuItem item0 = menu.add(0, MENU_ITEM_NOTELIST, 0, R.string.menu_notelist);
        item0.setIcon(android.R.drawable.ic_menu_agenda);

        MenuItem item9 = menu.add(0, MENU_ITEM_ADDFAVORITES, 0, R.string.menu_favorites);
        item9.setIcon(android.R.drawable.ic_menu_recent_history);

        MenuItem item10 = menu.add(0, MENU_ITEM_FAVORITELIST, 0, R.string.menu_favorite_list);
        item10.setIcon(android.R.drawable.ic_menu_agenda);

        MenuItem item6 = menu.add(0, MENU_ITEM_MARKLIST, 0, R.string.menu_bookmarklist);
        item6.setIcon(android.R.drawable.ic_menu_compass);

        MenuItem item7 = menu.add(0, MENU_ITEM_ADDNOTE, 0, R.string.menu_addnote);
        item7.setIcon(android.R.drawable.ic_menu_add);

        MenuItem item8 = menu.add(0, MENU_ITEM_SENDSMS, 0, R.string.menu_sendsms);
        item8.setIcon(android.R.drawable.ic_menu_send);

        MenuItem item11 = menu.add(0, MENU_ITEM_SHARE, 0, "Share");
        item11.setIcon(android.R.drawable.ic_menu_share);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (dao.queryExistsContents(mVersion, mBook, mChapter)) {
            menu.findItem(MENU_LOAD_DB).setEnabled(true);
            /*
            if (menu.findItem(MENU_LOAD_DB) == null) {
            MenuItem item5 = menu.add(0, MENU_LOAD_DB, 5, R.string.menu_loaddb);
            item5.setIcon(android.R.drawable.ic_menu_upload);
            }
            */
        } else {
            //menu.removeItem(MENU_LOAD_DB);
            menu.findItem(MENU_LOAD_DB).setEnabled(false);
        }

        // biblecontents

        // mAdView = (SmartAdView) 
        //findViewById(R.id.biblecontents).setVisibility(View.VISIBLE);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_SAVE: {

                dao.insert(mVersions[mVersion], mKVersions[mVersion], this.mVersion, this.mBook, this.mChapter, this.mListView);

                return true;
            }
            case MENU_ITEM_PREV: {
                Spinner spin = (Spinner) findViewById(R.id.books);

                int newPosition = spin.getSelectedItemPosition() - 1;
                if (spin.getCount() > newPosition && newPosition >= 0) {
                    spin.setSelection(newPosition);
                }
                return true;
            }
            case MENU_ITEM_NEXT: {
                Spinner spin = (Spinner) findViewById(R.id.books);

                int newPosition = spin.getSelectedItemPosition() + 1;
                if (spin.getCount() > newPosition && newPosition >= 0) {
                    spin.setSelection(newPosition);
                }
                return true;
            }
            case MENU_LOAD_WEB: {

                loadWebContents();

                return true;
            }
            case MENU_ITEM_ADDFAVORITES: {

                openAddFavoritesDialog();

                return true;
            }
            case MENU_ITEM_FAVORITELIST: {

                FavoritesList();

                return true;
            }
            case MENU_LOAD_DB: {

                loadDBContents();

                return true;
            }
            case MENU_ITEM_NOTELIST: {

                viewNotesList();

                return true;
            }
            case MENU_ITEM_MARKLIST: {

                Intent intent = new Intent();
                intent.setClass(this, BookmarkList.class);
                startActivity(intent);

                return true;
            }
            case MENU_ITEM_ADDNOTE: {

                addNewNote();
                return true;

            }
            case MENU_ITEM_SHARE: {

                shareVerse();
                return true;
            }
            case MENU_ITEM_SENDSMS: {

                // "online_full" 전체 화면 광고 노출을 시작
                sendSMS();
                return true;
            }
            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 
     */
    private void FavoritesList() {
        Intent intent = new Intent();
        intent.setClass(this, FavoritesList.class);
        startActivity(intent);
    }

    /**
     * 
     */
    private void loadWebContents() {
        String url = mBaseUrl.replace("$BOOK$", mBooks[mBook]).replace("$CHAP$", (mChapter + 1) + "");
        String url2 = mBaseUrl2.replace("$BOOK$", mBooks[mBook]).replace("$CHAP$", (mChapter + 1) + "");

        mCompletecount = 0;
        Log.d(TAG, "ReloadBibleContents Url = " + url);
        Log.d(TAG, "ReloadBibleContents Url2 = " + url2);
        new GrabURL().execute(url, mVersion + "");
        if (mVersion != mVersion2 && mVersion2 >= 0 && Prefs.getCompare(this)) {
            new GrabURL().execute(url2, mVersion2 + "");
        }
    }

    private void viewNotesList() throws NotFoundException {

        Intent intent = new Intent();
        intent.setClass(this, NotesList.class);
        intent.putExtra("bibleid", this.mListView.getSelectedItemId());
        intent.putExtra("version", this.mVersion);
        intent.putExtra("book", this.mBook);
        intent.putExtra("chapter", this.mChapter);
        intent.putExtra("verse", mVerse);
        startActivity(intent);
    }

    private class mySimpleAdapter extends SimpleCursorAdapter {
        private Cursor mCursor;
        private Context mContext;
        private int mLayout;

        public mySimpleAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            // TODO Auto-generated constructor stub
            mCursor = c;
            mContext = context;
            mLayout = layout;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = getLayoutInflater();//(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(mLayout, parent, false);
            }

            if (mCursor.moveToPosition(position)) {
                TextView versestr = (TextView) v.findViewById(R.id.versestr);
                TextView contents = (TextView) v.findViewById(R.id.contents);
                //TextView rowidcolon = (TextView) v.findViewById(R.id.rowidcolon);
                ImageView flag = (ImageView) v.findViewById(R.id.flag);

                contents.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getFontSize(mContext));

                if (versestr != null) {
                    versestr.setText(mCursor.getString(6));
                }
                if (contents != null) {

                    contents.setText(mCursor.getString(7));
                }
                if (flag != null) {
                    if (mCheck.contains(mCursor.getLong(0)))
                        flag.setVisibility(View.VISIBLE);
                    else
                        flag.setVisibility(View.INVISIBLE);
                }

            }

            return v;

        }
    }

    private void loadDBContents() throws NotFoundException {
        Log.d(TAG, "loadDBContents...");

        //String msg = getResources().getString(R.string.load_db_msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        Cursor contents = dao.queryContents(mVersion, mBook, mChapter, mVersion2);
        //SimpleCursorAdapter adapter = new EfficientAdapter(getBaseContext(), R.layout.chapter_item, contents, new String[] { "verse", "contents" }, new int[] { R.id.versestr, R.id.contents });

        SimpleCursorAdapter adapter = new mySimpleAdapter(getBaseContext(), R.layout.chapter_item, contents, new String[] { "verse", "contents" }, new int[] { R.id.versestr, R.id.contents });

        mListView.setAdapter(adapter);

        Log.d(TAG, "contents =" + contents.getCount());

        ArrayList<HashMap<String, String>> mList2 = new ArrayList<HashMap<String, String>>();
        if (contents.moveToFirst()) {
            do {
                HashMap<String, String> item2 = new HashMap<String, String>();
                if (contents.getInt(6) > 0) {
                    item2.put("Number", contents.getString(6).trim());
                    mList2.add(item2);
                } else {
                    continue;
                }

            } while (contents.moveToNext());

        }

        if (mList2.size() > 0) {

            Cursor c = dao.queryVerseList(mVersion, mBook, mChapter);

            Spinner spin_verse = (Spinner) findViewById(R.id.verses);
            SimpleCursorAdapter adapter_verse = new SimpleCursorAdapter(BibleViewer.this, android.R.layout.simple_spinner_item, c, new String[] { "verse" }, new int[] { android.R.id.text1 });
            //SimpleAdapter adapter_verse = new SimpleAdapter(getBaseContext(), mList2, android.R.layout.simple_spinner_item, new String[] { "Number" }, new int[] { android.R.id.text1 });
            adapter_verse.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spin_verse.setAdapter(adapter_verse);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Log.e(TAG, "AdapterContextMenuInfo=" + info.toString());
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Object obj = mListView.getItemAtPosition(info.position);
        if (obj.getClass().toString().indexOf("SQLiteCursor") >= 0) {
            Cursor c = (Cursor) obj;
            mVerse = c.getInt(6);
        } else {
            HashMap<String, String> map = (HashMap<String, String>) obj;
            String verse = map.get("Number").trim();
            if ("".equals(verse))
                mVerse = 0;
            else
                try {
                    mVerse = Integer.parseInt(verse.trim()) - 1;
                } catch (Exception e) {
                    mVerse = 0;
                }
        }

        menu.setHeaderTitle(mAllTestment[mBook] + " ( " + mEngTestment[mBook] + " )");

        MenuItem item6 = menu.add(0, MENU_ITEM_ADDMARK, 0, R.string.menu_addmark);
        item6.setIcon(android.R.drawable.ic_menu_compass);

        MenuItem item7 = menu.add(0, MENU_ITEM_ADDNOTE, 0, R.string.menu_addnote);
        item7.setIcon(android.R.drawable.ic_menu_add);

        MenuItem item8 = menu.add(0, MENU_ITEM_SENDSMS, 0, R.string.menu_sendsms);
        item8.setIcon(android.R.drawable.ic_menu_send);

        MenuItem item0 = menu.add(0, MENU_ITEM_NOTELIST, 0, R.string.menu_notelist);
        item0.setIcon(android.R.drawable.ic_menu_agenda);

        MenuItem item9 = menu.add(0, MENU_ITEM_ADDFAVORITES, 0, R.string.menu_favorites);
        item9.setIcon(android.R.drawable.ic_menu_recent_history);

        MenuItem item10 = menu.add(0, MENU_ITEM_FAVORITELIST, 0, R.string.menu_favorite_list);
        item10.setIcon(android.R.drawable.ic_menu_agenda);

        MenuItem item1 = menu.add(0, MENU_ITEM_PREV, 0, R.string.menu_previous);
        item1.setIcon(android.R.drawable.ic_media_rew);

        MenuItem item3 = menu.add(0, MENU_ITEM_NEXT, 0, R.string.menu_next);
        item3.setIcon(android.R.drawable.ic_media_ff);

        MenuItem item2 = menu.add(0, MENU_ITEM_SAVE, 0, R.string.menu_save);
        item2.setIcon(android.R.drawable.ic_menu_save);

        MenuItem item4 = menu.add(0, MENU_LOAD_WEB, 0, R.string.menu_loadweb);
        item4.setIcon(R.drawable.ic_menu_refresh);

        MenuItem item5 = menu.add(0, MENU_LOAD_DB, 0, R.string.menu_loaddb);
        item5.setIcon(android.R.drawable.ic_menu_upload);

        MenuItem item11 = menu.add(0, MENU_ITEM_SHARE, 0, "Share");
        item11.setIcon(android.R.drawable.ic_menu_share);

    }

    private void openAddFavoritesDialog() {

        Cursor c = favoritesDao.queryFavoritesGroup(0);

        new AlertDialog.Builder(this).setTitle(R.string.select_favorites_group).setCursor(c, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int group) {

                String mBibleShortName[] = getResources().getStringArray(R.array.all_testment);
                //String mEngBibleName[] = getResources().getStringArray(R.array.eng_testment);

                String versestr = mBibleShortName[mBook] + " " + (mChapter + 1) + " : " + (mVerse);
                //versestr += " (" + mEngBibleName[mBook] + ")";

                Cursor c = dao.queryBibleVerse(mVersion, mBook, mChapter, mVerse);

                String contents = "";
                if (c.moveToNext()) {
                    contents = c.getString(2);
                }
                c.close();

                if (group <= 0) {

                    Intent intent = new Intent();
                    intent.setClass(BibleViewer.this, GroupEditor.class);
                    intent.putExtra("group", group);
                    intent.putExtra("versestr", versestr);
                    intent.putExtra("contents", contents);
                    intent.putExtra("version", mVersion);
                    intent.putExtra("book", mBook);
                    intent.putExtra("chapter", mChapter);
                    intent.putExtra("verse", mVerse);
                    startActivity(intent);

                } else {

                    favoritesDao.insertFavorites(new Long(group), versestr, contents, mVersion, mBook, mChapter, mVerse);
                }
            }

        }, FavoriteGroup.GROUPNM).show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case MENU_ITEM_ADDMARK: {

                String mBibleShortName[] = getResources().getStringArray(R.array.all_testment);
                String mEngBibleName[] = getResources().getStringArray(R.array.eng_testment);

                String versestr = mBibleShortName[mBook] + " " + (mChapter + 1) + " : " + (mVerse);
                versestr += " (" + mEngBibleName[mBook] + ")";

                markDao.insert(mListView.getSelectedItemId(), mVersions[mVersion], mVersion, mBook, mChapter, mVerse, versestr);

                return true;
            }
            case MENU_ITEM_ADDFAVORITES: {

                openAddFavoritesDialog();

                return true;
            }
            case MENU_ITEM_FAVORITELIST: {

                FavoritesList();

                return true;
            }
            case MENU_ITEM_ADDNOTE: {

                addNewNote();
                return true;
            }
            case MENU_ITEM_SHARE: {

                shareVerse();
                return true;
            }
            case MENU_ITEM_SENDSMS: {

                sendSMS();
                return true;
            }
            case MENU_ITEM_SAVE: {

                dao.insert(mVersions[mVersion], mKVersions[mVersion], mVersion, mBook, mChapter, mListView);

                return true;
            }
            case MENU_ITEM_PREV: {
                Spinner spin = (Spinner) findViewById(R.id.books);

                int newPosition = mBook - 1;
                if (newPosition >= 0) {
                    spin.setSelection(newPosition);
                }
                return true;
            }
            case MENU_ITEM_NEXT: {
                Spinner spin = (Spinner) findViewById(R.id.books);

                int newPosition = mBook + 1;
                if (spin.getCount() > newPosition) {
                    spin.setSelection(newPosition);
                }
                return true;
            }
            case MENU_LOAD_WEB: {

                loadWebContents();

                return true;
            }
            case MENU_LOAD_DB: {

                loadDBContents();

                return true;
            }
            case MENU_ITEM_NOTELIST: {

                viewNotesList();

                return true;
            }
            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                return true;
            }

        }

        return super.onContextItemSelected(item);

    }

    /**
     * 
     */
    private void addNewNote() {
        Intent intent = new Intent();
        intent.setClass(this, NoteEditor.class);
        intent.putExtra("id", new Long(-1));
        intent.putExtra("bibleid", mListView.getSelectedItemId());
        intent.putExtra("version", mVersion);
        intent.putExtra("book", mBook);
        intent.putExtra("chapter", mChapter);
        intent.putExtra("verse", mVerse);
        intent.putExtra("msg", getSelectMsg());
        startActivity(intent);
    }

    private void shareVerse() {

        Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);

        picMessageIntent.setType("text/plain"); // 데이터의 종류

        //File downloadedPic =  new File(filePath, fileName);

        picMessageIntent.putExtra(Intent.EXTRA_TEXT, getSelectMsg());
        //picMessageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadedPic));   

        startActivity(Intent.createChooser(picMessageIntent, "Send BIBLE verse using:"));

    }

    private void sendSMS() {

        String msg = getSelectMsg();

        Uri smsUri = Uri.parse("tel:/010");
        Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
        intent.putExtra("sms_body", msg);
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);

        /*
        SmsManager m = SmsManager.getDefault();
        String destination = "";
        if (PhoneNumberUtils.isWellFormedSmsAddress(destination)) {
            m.sendTextMessage(destination, null, msg, null, null);
        }
        */

        /*
        String destination = "06761122334";
        if (PhoneNumberUtils.isWellFormedSmsAddress(destination)) {
             sendSMS(destination, msg);
        }
        */
    }

    /**
     * @return
     */
    public int getSelectCount() {
        int cnt = 0;
        for (int i = 0; i < mListView.getChildCount(); i++) {
            if (this.mListView.getChildAt(i).findViewById(R.id.flag).getVisibility() == View.VISIBLE) {
                cnt++;
            }
        }
        return cnt;
    }

    /*
     private void sendSMS(String phoneNumber, String message) {
         String SENT = "SMS_SENT";
         String DELIVERED = "SMS_DELIVERED";

         PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

         PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

         //---when the SMS has been sent---
         registerReceiver(new BroadcastReceiver() {
             @Override
             public void onReceive(Context arg0, Intent arg1) {
                 switch (getResultCode()) {
                     case Activity.RESULT_OK:
                         Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                         Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_NO_SERVICE:
                         Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_NULL_PDU:
                         Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_RADIO_OFF:
                         Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                         break;
                 }
             }
         }, new IntentFilter(SENT));

         //---when the SMS has been delivered---
         registerReceiver(new BroadcastReceiver() {
             @Override
             public void onReceive(Context arg0, Intent arg1) {
                 switch (getResultCode()) {
                     case Activity.RESULT_OK:
                         Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                         break;
                     case Activity.RESULT_CANCELED:
                         Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                         break;
                 }
             }
         }, new IntentFilter(DELIVERED));

         SmsManager sms = SmsManager.getDefault();
         sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
     }
    */
    private class OldEvent {
        public float x;
        public float y;

        public OldEvent(float f, float g) {
            x = f;
            y = g;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }
    }

}
