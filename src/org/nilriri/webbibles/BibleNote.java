package org.nilriri.webbibles;

import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BookmarkDao;
import org.nilriri.webbibles.dao.Constants.Bookmark;
import org.nilriri.webbibles.tools.DataManager;
import org.nilriri.webbibles.tools.SearchData;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class BibleNote extends Activity implements OnClickListener {
    private static final String TAG = "BibleNote";
    private static final String BIBLE_VERSION = "version";

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

        dao = new BookmarkDao(this, null, Prefs.getSDCardUse(this));

        Log.d(TAG, "BibleNote.onCreate();");
        setContentView(R.layout.main);

        Spinner spin_bibles = (Spinner) findViewById(R.id.spinbible);
        ArrayAdapter<CharSequence> adp_bibles = ArrayAdapter.createFromResource(this, R.array.sitelist, android.R.layout.simple_spinner_item);
        adp_bibles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_bibles.setAdapter(adp_bibles);
        spin_bibles.setOnItemSelectedListener(new bibleRevisionSelectedListener());

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter_books = ArrayAdapter.createFromResource(this, R.array.old_testment, android.R.layout.simple_spinner_item);
        adapter_books.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter_books);
        spinner.setOnItemSelectedListener(new oldTestmentSelectedListener());

        spinner.setOnCreateContextMenuListener(this);

        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter_books2 = ArrayAdapter.createFromResource(this, R.array.new_testment, android.R.layout.simple_spinner_item);
        adapter_books2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter_books2);
        spinner2.setOnItemSelectedListener(new newTestmentSelectedListener());

        Spinner spinner3 = (Spinner) findViewById(R.id.spinbookmark);
        spinner3.setOnItemSelectedListener(new bookmarkSelectedListener());

        View aboutButton = findViewById(R.id.btn_About);
        aboutButton.setOnClickListener(this);

        View manageButton = findViewById(R.id.btn_manage);
        manageButton.setOnClickListener(this);

        View searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Spinner) findViewById(R.id.spinbible)).setSelection(getPreferences(MODE_PRIVATE).getInt(BIBLE_VERSION, 0));

        Cursor curBookmark = dao.queryBookmarkTopList(10);
        Spinner spinner3 = (Spinner) findViewById(R.id.spinbookmark);
        SimpleCursorAdapter adapter_bookmark = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, curBookmark, new String[] { Bookmark.VERSESTR }, new int[] { android.R.id.text1 });
        adapter_bookmark.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter_bookmark);

    }

    @Override
    protected void onPause() {
        super.onPause();

        getPreferences(MODE_PRIVATE).edit().putInt(BIBLE_VERSION, ((Spinner) findViewById(R.id.spinbible)).getSelectedItemPosition()).commit();
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

    public class oldTestmentSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            if (pos <= 0)
                return;

            // 역본정보
            Spinner revision = (Spinner) findViewById(R.id.spinbible);

            // 역본정보와 선택된 성경정보를 지정하여 읽기 창을 오픈한다.
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), BibleViewer.class);
            intent.putExtra("VERSION", revision.getSelectedItemPosition());
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

            // 역본정보와 선택된 성경정보를 지정하여 읽기 창을 오픈한다.
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), BibleViewer.class);
            intent.putExtra("VERSION", revision.getSelectedItemPosition());
            intent.putExtra("BOOK", 39 + (pos - 1));
            startActivity(intent);

            spinner.setSelection(0);
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

        }

    }

}
