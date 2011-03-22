package org.nilriri.webbibles.tools;

import org.nilriri.webbibles.BibleViewer;
import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BibleDao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BibleSearchList extends Activity {

    private static final String TAG = "BibleView";

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

    private int searchVersion = -1;
    private int searchTestment = -1;
    private int searchOperator = -1;
    private String searchKeyword1 = "";
    private String searchKeyword2 = "";

    private Cursor contents = null;

    //private String[] mAllTestment;
    //private String[] mEngTestment;
    //private BibleInternalDao dao = new BibleInternalDao(this, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    private BibleDao dao;// = new BibleDao(this, null, Prefs.getSDCardUse(this));
    //private BookmarkDao markDao;// = new BookmarkDao(this, null, Prefs.getSDCardUse(this));

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dao = new BibleDao(this, null, Prefs.getSDCardUse(this));

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        setContentView(R.layout.searchlist_view);

        // 검색조건
        searchVersion = getIntent().getIntExtra("version", 0);
        searchTestment = getIntent().getIntExtra("testment", 0);
        searchOperator = getIntent().getIntExtra("operator", 0);
        searchKeyword1 = getIntent().getStringExtra("keyword1");
        searchKeyword2 = getIntent().getStringExtra("keyword2");

        mListView = (ListView) findViewById(R.id.ContentsListView);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

        searchContents();

    }

    public class listOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

            if (pos < 0)
                return;

            Cursor c = (Cursor) mListView.getItemAtPosition(pos);

            Log.d(TAG, "" + c.getInt(3) + "," + c.getInt(4) + "," + c.getInt(5));

            Intent intent = new Intent();
            intent.setClass(getBaseContext(), BibleViewer.class);
            intent.putExtra("VERSION", c.getInt(3));
            intent.putExtra("BOOK", c.getInt(4));
            intent.putExtra("CHAPTER", c.getInt(5));
            intent.putExtra("VERSE", c.getInt(6));
            startActivity(intent);

        }

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
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private ProgressDialog searchDialog = null;

    public void searchContents() {

        searchDialog = new ProgressDialog(this);
        searchDialog.setMessage("Please wait searching data...");
        searchDialog.setCancelable(true);
        //searchDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //searchDialog.setIndeterminate(true);
        searchDialog.show();

        Thread thread = new Thread(new Runnable() {

            public void run() {

                try {

                    //searchDialog.setProgress(1);
                    contents = dao.querySearchContents(searchVersion, searchTestment, searchOperator, searchKeyword1, searchKeyword2);

                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                } finally {
                    handler.sendEmptyMessage(0);
                }

            }

        });

        thread.start();

    }

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getBaseContext(), R.layout.chapter_item, contents, new String[] { "empty", "contents" }, new int[] { R.id.versestr, R.id.contents });

            mListView.setAdapter(adapter);

            if (mListView.getCount() == 0) {
                Toast.makeText(BibleSearchList.this, "Not Found...", Toast.LENGTH_SHORT).show();
            }

            searchDialog.dismiss();

        }

    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Log.d(TAG, "AdapterContextMenuInfo=" + info.toString());
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        //Cursor c = (Cursor) mListView.getItemAtPosition(info.position);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                return true;
            }

        }

        return super.onContextItemSelected(item);

    }

}
