package org.nilriri.webbibles;

import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BookmarkDao;
import org.nilriri.webbibles.dao.Constants.Bookmark;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class BookmarkList extends Activity implements OnClickListener {
    private static final String TAG = "NotesList";

    // Menu item ids
    public static final int MENU_ITEM_DELMARK = Menu.FIRST;
    public static final int MENU_ITEM_ADDNOTE = Menu.FIRST + 1;
    public static final int MENU_ITEM_ALLLIST = Menu.FIRST + 2;
    public static final int MENU_ITEM_BOOKLIST = Menu.FIRST + 3;
    public static final int MENU_ITEM_BIBLEVIEW = Menu.FIRST + 4;
    public static final int MENU_ITEM_EDITNOTE = Menu.FIRST + 5;
    public static final int MENU_ITEM_DAYSLIST = Menu.FIRST + 6;

    private BookmarkDao dao;

    private ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dao = new BookmarkDao(this, null, Prefs.getSDCardUse(this));
        
        setContentView(R.layout.bookmark_view);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mListView = (ListView) findViewById(R.id.ContentsListView);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_prev:

            case R.id.btn_next:

                break;
            case R.id.from_date:

                break;
            case R.id.to_date:

                break;

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        loadBookmarkList();

    }

    private void loadBookmarkList() {

        Cursor cursor = dao.queryBookmarkList(0);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.bookmark_item, cursor, new String[] { Bookmark.MODIFIED_DATE, Bookmark.VERSESTR }, new int[] { R.id.modified_date, R.id.versestr });
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        //final Intent intent = getIntent();

        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) this.mListView.getItemAtPosition(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(Bookmark.COL_VERSESTR));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELMARK, 0, R.string.menu_deletemark);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case MENU_ITEM_DELMARK: {
                Log.e(TAG, "MENU_ITEM_DELFAVORITE id = " + info.id);

                dao.delete(info.id);

                loadBookmarkList();
                return true;
            }
        }
        return false;
    }

    public class listOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            Log.e(TAG, "onListItemClick position = " + pos);
            Log.e(TAG, "onListItemClick id = " + id);

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

        }

    }

}
