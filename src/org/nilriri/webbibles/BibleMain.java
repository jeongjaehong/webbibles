package org.nilriri.webbibles;

import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BookmarkDao;
import org.nilriri.webbibles.dao.Constants.Bookmark;
import org.nilriri.webbibles.dao.Constants.Favorites;
import org.nilriri.webbibles.tools.DataManager;
import org.nilriri.webbibles.tools.SearchData;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class BibleMain extends Activity implements OnClickListener {
    //protected SmartAdView mAdView;

    private static final String TAG = "BibleMain";
    private static final String BIBLE_VERSION = "version";
    private static final String BIBLE_VERSION2 = "version2";

    private static final String BOOK = "book";
    private static final String CHAPTER = "chapter";
    private static final String VERSE = "verse";

    private BookmarkDao dao;

    public static String BIBLE_DB_NAME = "revision.sqlite";
    public static int BIBLE_DB_VERSION = 1;
    public static String BIBLE_TABLE_NAME = "bible";

    public static final int MENU_ITEM_DELETE = Menu.FIRST;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
    public static final int MENU_ITEM_NOTELIST = Menu.FIRST + 2;
    public static final int MENU_ITEM_BOOKMARKLIST = Menu.FIRST + 3;
    public static final int MENU_ITEM_DATAMANAGER = Menu.FIRST + 4;
    public static final int MENU_ITEM_DATASEARCH = Menu.FIRST + 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        dao = new BookmarkDao(this, null, Prefs.getSDCardUse(this));

        Log.d(TAG, "BibleMain.onCreate();");
        setContentView(R.layout.main);

        //mAdView = (SmartAdView) findViewById(R.id.ad_view);

        Spinner spin_bibles = (Spinner) findViewById(R.id.spinbible);
        ArrayAdapter<CharSequence> adp_bibles = ArrayAdapter.createFromResource(this, R.array.sitelist, android.R.layout.simple_spinner_item);
        adp_bibles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_bibles.setAdapter(adp_bibles);
        spin_bibles.setOnItemSelectedListener(new bibleRevisionSelectedListener());

        //비교 역본
        Spinner spin_bibles2 = (Spinner) findViewById(R.id.spinbible2);
        ArrayAdapter<CharSequence> adp_bibles2 = ArrayAdapter.createFromResource(this, R.array.sitelist, android.R.layout.simple_spinner_item);
        adp_bibles2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_bibles2.setAdapter(adp_bibles2);
        spin_bibles2.setOnItemSelectedListener(new bible2RevisionSelectedListener());

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter_books = ArrayAdapter.createFromResource(this, R.array.old_testment, android.R.layout.simple_spinner_item);
        //ArrayAdapter<CharSequence> adapter_books = ArrayAdapter.createFromResource(this, R.array.newsongs, android.R.layout.simple_spinner_item);
        adapter_books.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter_books);
        spinner.setOnItemSelectedListener(new oldTestmentSelectedListener());

        spinner.setOnCreateContextMenuListener(this);

        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter_books2 = ArrayAdapter.createFromResource(this, R.array.new_testment, android.R.layout.simple_spinner_item);
        adapter_books2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter_books2);
        spinner2.setOnItemSelectedListener(new newTestmentSelectedListener());
        /*
                Spinner spinsong = (Spinner) findViewById(R.id.spinsong);
                ArrayList<HashMap<String, String>> mList2 = new ArrayList<HashMap<String, String>>();

                for (int i = 0; i < 645; i++) {
                    HashMap<String, String> item2 = new HashMap<String, String>();

                    if (i == 0) {
                        item2.put("id", i + "");
                        item2.put("song", "새 찬송가");
                    } else {
                        item2.put("id", i + "");
                        item2.put("song", i + "장");
                    }
                    mList2.add(item2);
                }

                SimpleAdapter adapter_song = new SimpleAdapter(getBaseContext(), mList2, android.R.layout.simple_spinner_item, new String[] { "song" }, new int[] { android.R.id.text1 });
                adapter_song.setDropDownViewResource(android.R.layout.simple_spinner_item);

                spinsong.setAdapter(adapter_song);
                spinsong.setOnItemSelectedListener(new songSelectedListener());
                */
        /////////////////
        /*
        Spinner spinsong2 = (Spinner) findViewById(R.id.spinsong2);
        ArrayList<HashMap<String, String>> mList22 = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < 558; i++) {
            HashMap<String, String> item22 = new HashMap<String, String>();

            if (i == 0) {
                item22.put("song", "구 찬송가");
            } else {
                item22.put("song", i + "장");
            }
            mList22.add(item22);
        }

        SimpleAdapter adapter_song2 = new SimpleAdapter(getBaseContext(), mList22, android.R.layout.simple_spinner_item, new String[] { "song" }, new int[] { android.R.id.text1 });
        adapter_song2.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinsong2.setAdapter(adapter_song2);
        spinsong2.setOnItemSelectedListener(new songSelectedListener());
        */
        /////////////////
        Spinner spinner3 = (Spinner) findViewById(R.id.spinbookmark);
        spinner3.setOnItemSelectedListener(new bookmarkSelectedListener());

        Spinner spinner4 = (Spinner) findViewById(R.id.spinfavo);
        spinner4.setOnItemSelectedListener(new favoSelectedListener());

        View aboutButton = findViewById(R.id.btn_About);
        aboutButton.setOnClickListener(this);

        View manageButton = findViewById(R.id.btn_manage);
        manageButton.setOnClickListener(this);

        View searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(this);

        View gotoButton = findViewById(R.id.btn_goto);
        gotoButton.setOnClickListener(this);

        View songButton = findViewById(R.id.btn_song);
        songButton.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        // "online_idle" 일부 화면 광고 노출을 시작
        // "online_full" 전체 화면 광고 노출을 시작
        //mAdView.onStart("online_idle");
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        // 광고 노출을 중단한다.
        //mAdView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Spinner) findViewById(R.id.spinbible)).setSelection(getPreferences(MODE_PRIVATE).getInt(BIBLE_VERSION, 0));
        ((Spinner) findViewById(R.id.spinbible2)).setSelection(getPreferences(MODE_PRIVATE).getInt(BIBLE_VERSION2, 0));

        Cursor curBookmark = dao.queryBookmarkTopList(10);
        Spinner spinner3 = (Spinner) findViewById(R.id.spinbookmark);
        SimpleCursorAdapter adapter_bookmark = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, curBookmark, new String[] { Bookmark.VERSESTR }, new int[] { android.R.id.text1 });
        adapter_bookmark.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter_bookmark);

        Cursor curFavorites = dao.queryFavoritesTopList(50);
        Spinner spinner4 = (Spinner) findViewById(R.id.spinfavo);
        SimpleCursorAdapter adapter_favorites = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, curFavorites, new String[] { Favorites.VERSESTR }, new int[] { android.R.id.text1 });
        adapter_favorites.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner4.setAdapter(adapter_favorites);

        if (Prefs.getAutoRead(this)) {
            View gotoButton = findViewById(R.id.btn_goto);
            gotoButton.setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        getPreferences(MODE_PRIVATE).edit().putInt(BIBLE_VERSION, ((Spinner) findViewById(R.id.spinbible)).getSelectedItemPosition()).commit();
        //getPreferences(MODE_PRIVATE).edit().putInt(BIBLE_VERSION, ((Spinner) findViewById(R.id.spinbible)).getSelectedItemPosition()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item0 = menu.add(0, MENU_ITEM_NOTELIST, 0, R.string.menu_notelist);
        item0.setIcon(android.R.drawable.ic_menu_agenda);

        MenuItem item1 = menu.add(0, MENU_ITEM_BOOKMARKLIST, 0, R.string.menu_bookmarklist);
        item1.setIcon(android.R.drawable.ic_menu_compass);

        MenuItem item2 = menu.add(0, MENU_ITEM_DATAMANAGER, 0, R.string.menu_datamanager);
        item2.setIcon(android.R.drawable.ic_menu_save);

        MenuItem item3 = menu.add(0, MENU_ITEM_DATASEARCH, 0, R.string.menu_datasearch);
        item3.setIcon(android.R.drawable.ic_menu_search);

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
            case MENU_ITEM_NOTELIST: {
                Intent intent = new Intent();
                intent.setClass(this, NotesList.class);
                intent.putExtra("bibleid", -1);
                intent.putExtra("version", -1);
                intent.putExtra("book", -1);
                intent.putExtra("chapter", -1);
                intent.putExtra("verse", -1);
                startActivity(intent);
                break;
            }
            case MENU_ITEM_BOOKMARKLIST: {
                Intent intent = new Intent();
                intent.setClass(this, BookmarkList.class);
                startActivity(intent);
                break;
            }
            case MENU_ITEM_DATAMANAGER: {
                Intent intent = new Intent();
                intent.setClass(this, DataManager.class);
                startActivity(intent);
                break;
            }
            case MENU_ITEM_DATASEARCH: {
                Intent intent = new Intent();
                intent.setClass(this, SearchData.class);
                startActivity(intent);
                break;
            }
            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public class bibleRevisionSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            //BIBLE_DB_NAME = "";
            //BIBLE_TABLE_NAME = "";

            getPreferences(MODE_PRIVATE).edit().putInt(BIBLE_VERSION, ((Spinner) findViewById(R.id.spinbible)).getSelectedItemPosition()).commit();

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class bible2RevisionSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            //BIBLE_DB_NAME = "";
            //BIBLE_TABLE_NAME = "";

            getPreferences(MODE_PRIVATE).edit().putInt(BIBLE_VERSION2, ((Spinner) findViewById(R.id.spinbible2)).getSelectedItemPosition()).commit();

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class oldTestmentSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            if (pos <= 0)
                return;

            // 역본정보
            Spinner revision = (Spinner) findViewById(R.id.spinbible);
            Spinner revision2 = (Spinner) findViewById(R.id.spinbible2);

            // 역본정보와 선택된 성경정보를 지정하여 읽기 창을 오픈한다.
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), BibleViewer.class);
            intent.putExtra("VERSION", revision.getSelectedItemPosition());
            intent.putExtra("VERSION2", revision2.getSelectedItemPosition());
            intent.putExtra("BOOK", pos - 1);
            startActivity(intent);

            spinner.setSelection(0);
            return;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class newTestmentSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner2);

            if (pos <= 0)
                return;

            // 역본정보
            Spinner revision = (Spinner) findViewById(R.id.spinbible);
            Spinner revision2 = (Spinner) findViewById(R.id.spinbible2);

            // 역본정보와 선택된 성경정보를 지정하여 읽기 창을 오픈한다.
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), BibleViewer.class);
            intent.putExtra("VERSION", revision.getSelectedItemPosition());
            intent.putExtra("VERSION2", revision2.getSelectedItemPosition());
            intent.putExtra("BOOK", 39 + (pos - 1));
            startActivity(intent);

            spinner.setSelection(0);
            return;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class songSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (pos <= 0)
                return;

            Intent intent = new Intent();
            intent.setClass(BibleMain.this, Song.class);

            if (view.getId() == R.id.spinsong) {

                intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_text_new.asp?hymn_idx=" + (pos));
                intent.putExtra("version", 1);
                intent.putExtra("songid", pos);

            } else {
                intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_text.asp?hymn_idx=" + (pos));
                intent.putExtra("version", 2);
                intent.putExtra("songid", pos);
            }

            startActivity(intent);
            return;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class bookmarkSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (pos <= 0)
                return;

            Cursor c = (Cursor) parent.getItemAtPosition(pos);

            if (c != null) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), BibleViewer.class);
                intent.putExtra("VERSION", c.getInt(Bookmark.COL_VERSION));
                intent.putExtra("BOOK", c.getInt(Bookmark.COL_BOOK));
                intent.putExtra("CHAPTER", c.getInt(Bookmark.COL_CHAPTER));
                intent.putExtra("VERSE", c.getInt(Bookmark.COL_VERSE));
                startActivity(intent);
            }

            return;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public class favoSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (pos <= 0)
                return;

            Cursor c = (Cursor) parent.getItemAtPosition(pos);

            if (c != null) {
                if (c.getInt(Favorites.COL_ID) == 9999998) {
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), FavoritesList.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), BibleViewer.class);
                    intent.putExtra("VERSION", c.getInt(Favorites.COL_VERSION));
                    intent.putExtra("BOOK", c.getInt(Favorites.COL_BOOK));
                    intent.putExtra("CHAPTER", c.getInt(Favorites.COL_CHAPTER));
                    intent.putExtra("VERSE", c.getInt(Favorites.COL_VERSE));
                    startActivity(intent);
                }
            }

            return;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_About:

                startActivity(new Intent(this, About.class));
                break;

            case R.id.btn_manage:

                intent.setClass(this, DataManager.class);
                startActivity(intent);
                break;
            case R.id.btn_search:

                intent.setClass(this, SearchData.class);
                startActivity(intent);
                break;

            case R.id.btn_goto:

                try {

                    if (Prefs.getCalendar(this)) {
                        intent.setAction("org.nilriri.lunarcalendar.MAIN");
                        intent.setType("vnd.org.nilriri/lunarcalendar");
                        intent.putExtra("BIBLEPLAN", true);
                        
                    } else {

                        //intent.setClass(getBaseContext(), BibleViewer.class);
                        intent.setAction(Common.ACTION_BIBLEVIEW);
                        intent.setType("vnd.org.nilriri/web-bible");

                        intent.putExtra("VERSION", getPreferences(MODE_PRIVATE).getInt(BIBLE_VERSION, 0));
                        intent.putExtra("VERSION2", getPreferences(MODE_PRIVATE).getInt(BIBLE_VERSION2, 0));
                        intent.putExtra("BOOK", PreferenceManager.getDefaultSharedPreferences(this).getInt(BOOK, 0));
                        intent.putExtra("CHAPTER", PreferenceManager.getDefaultSharedPreferences(this).getInt(CHAPTER, 0));
                        intent.putExtra("VERSE", PreferenceManager.getDefaultSharedPreferences(this).getInt(VERSE, 0));
                    }

                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "LunarCalendar 음력달력 앱이 설치되어있지 않거나 최신버젼이 아닙니다.", Toast.LENGTH_LONG).show();
                    intent = new Intent();
                    intent.setAction(Common.ACTION_BIBLEVIEW);
                    intent.setType("vnd.org.nilriri/web-bible");

                    intent.putExtra("VERSION", getPreferences(MODE_PRIVATE).getInt(BIBLE_VERSION, 0));
                    intent.putExtra("VERSION2", getPreferences(MODE_PRIVATE).getInt(BIBLE_VERSION2, 0));
                    intent.putExtra("BOOK", PreferenceManager.getDefaultSharedPreferences(this).getInt(BOOK, 0));
                    intent.putExtra("CHAPTER", PreferenceManager.getDefaultSharedPreferences(this).getInt(CHAPTER, 0));
                    intent.putExtra("VERSE", PreferenceManager.getDefaultSharedPreferences(this).getInt(VERSE, 0));
                    startActivity(intent);
                }

                break;

            case R.id.btn_song:

                intent.setClass(getBaseContext(), Song.class);

                //intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_text_new.asp?hymn_idx=");
                //intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_player_new.asp?hymn_idx=");
                intent.putExtra("version", 0);
                intent.putExtra("songid", 1);

                startActivity(intent);
                break;

        }

    }

}
