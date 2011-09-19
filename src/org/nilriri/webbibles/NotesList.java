package org.nilriri.webbibles;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.NoteDao;
import org.nilriri.webbibles.dao.Constants.Notes;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class NotesList extends Activity implements OnClickListener {
    private static final String TAG = "NotesList";

    private static final int FROM_DATE_DIALOG_ID = 1;
    private static final int TO_DATE_DIALOG_ID = 2;

    // Menu item ids
    public static final int MENU_ITEM_DELNOTE = Menu.FIRST;
    public static final int MENU_ITEM_ADDNOTE = Menu.FIRST + 1;
    public static final int MENU_ITEM_ALLLIST = Menu.FIRST + 2;
    public static final int MENU_ITEM_BOOKLIST = Menu.FIRST + 3;
    public static final int MENU_ITEM_BIBLEVIEW = Menu.FIRST + 4;
    public static final int MENU_ITEM_EDITNOTE = Menu.FIRST + 5;
    public static final int MENU_ITEM_DAYSLIST = Menu.FIRST + 6;

    public static final int LOAD_ALL = 0;
    public static final int LOAD_BOOK = 1;
    public static final int LOAD_CHAPTER = 2;
    public static final int LOAD_DAYS = 3;

    private String searchYN = "N";
    private int searchVersion = -1;
    private int searchTestment = -1;
    private int searchOperator = -1;
    private String searchKeyword1 = "";
    private String searchKeyword2 = "";

    private NoteDao dao;

    private ListView mListView = null;
    private Long mBibleID = new Long(-1);
    private int mVersion = -1;
    private int mBook = -1;
    private int mChapter = -1;
    private int mVerse = -1;
    private int mLoadType = LOAD_CHAPTER;
    private Calendar fromCalendar;
    private Calendar toCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dao = new NoteDao(this, null, Prefs.getSDCardUse(this));

        setContentView(R.layout.notelist_view);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mBibleID = getIntent().getLongExtra("bibleid", new Long(-1));
        mVersion = getIntent().getIntExtra("version", -1);
        mBook = getIntent().getIntExtra("book", -1);
        mChapter = getIntent().getIntExtra("chapter", -1);
        mVerse = getIntent().getIntExtra("verse", -1);

        // 검색조건
        if ("Y".equals(getIntent().getStringExtra("SEARCH"))) {
            searchYN = getIntent().getStringExtra("SEARCH");
            searchVersion = getIntent().getIntExtra("version", 0);
            searchTestment = getIntent().getIntExtra("testment", 0);
            searchOperator = getIntent().getIntExtra("operator", 0);
            searchKeyword1 = getIntent().getStringExtra("keyword1");
            searchKeyword2 = getIntent().getStringExtra("keyword2");
        }

        toCalendar = Calendar.getInstance();
        toCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
        fromCalendar = Calendar.getInstance();
        fromCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
        fromCalendar.add(Calendar.MONTH, -1);
        ((EditText) findViewById(R.id.from_date)).setText(Common.fmtDate(fromCalendar));
        ((EditText) findViewById(R.id.to_date)).setText(Common.fmtDate(toCalendar));

        mListView = (ListView) findViewById(R.id.ContentsListView);
        mListView.setOnCreateContextMenuListener(this);
        //mListView.setOnTouchListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

        Spinner chapter_spinner = (Spinner) findViewById(R.id.chapters);
        chapter_spinner.setOnItemSelectedListener(new ChapterOnItemSelectedListener());
        //chapter_spinner.setOnCreateContextMenuListener(this);

        Spinner spin_books = (Spinner) findViewById(R.id.books);
        ArrayAdapter<CharSequence> adapter_books = ArrayAdapter.createFromResource(this, R.array.all_testment, android.R.layout.simple_spinner_item);
        adapter_books.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_books.setAdapter(adapter_books);

        spin_books.setOnItemSelectedListener(new BookOnItemSelectedListener());
        spin_books.setSelection(mBook);

        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.from_date).setOnClickListener(this);
        findViewById(R.id.to_date).setOnClickListener(this);

    }

    public class ChapterOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (pos < 0)
                return;

            mChapter = pos - 1;
            ReloadBibleContents();

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

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

    private void ReloadChapterList() {

        String chapterlist[] = getResources().getStringArray(R.array.chapterlist);

        String chapter[] = Common.tokenFn("-1," + chapterlist[mBook], ",");

        ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();

        for (int y = 0; y < chapter.length; y++) {
            HashMap<String, String> item = new HashMap<String, String>();

            if (y == 0) {
                item.put("Chapter", "ALL");
            } else {
                item.put("Chapter", chapter[y]);
            }

            mList.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mList, android.R.layout.simple_spinner_item, new String[] { "Chapter" }, new int[] { android.R.id.text1 });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        Spinner chapter_spinner = (Spinner) findViewById(R.id.chapters);
        chapter_spinner.setAdapter(adapter);

        mChapter = 0;
        chapter_spinner.setSelection(mChapter);
        ReloadBibleContents();

    }

    private void ReloadBibleContents() {
        if (mChapter == 0) {
            mLoadType = LOAD_BOOK;
        } else {
            mLoadType = LOAD_CHAPTER;
        }
        loadNoteList();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_prev:

                fromCalendar.add(Calendar.MONTH, -1);
                toCalendar.add(Calendar.MONTH, -1);
                ((EditText) findViewById(R.id.from_date)).setText(Common.fmtDate(fromCalendar));
                ((EditText) findViewById(R.id.to_date)).setText(Common.fmtDate(toCalendar));
                mLoadType = LOAD_DAYS;
                loadNoteList();

                break;
            case R.id.btn_next:

                fromCalendar.add(Calendar.MONTH, 1);
                toCalendar.add(Calendar.MONTH, 1);
                ((EditText) findViewById(R.id.from_date)).setText(Common.fmtDate(fromCalendar));
                ((EditText) findViewById(R.id.to_date)).setText(Common.fmtDate(toCalendar));
                mLoadType = LOAD_DAYS;
                loadNoteList();
                break;
            case R.id.from_date:

                showDialog(NotesList.FROM_DATE_DIALOG_ID);
                break;
            case R.id.to_date:

                showDialog(NotesList.TO_DATE_DIALOG_ID);
                break;

        }

    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case FROM_DATE_DIALOG_ID:
                return new DatePickerDialog(this, fromDateSetListener, fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH));
            case TO_DATE_DIALOG_ID:
                return new DatePickerDialog(this, toDateSetListener, toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH));
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case FROM_DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH));
                break;
            case TO_DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH));
                break;
        }
    }

    private DatePickerDialog.OnDateSetListener fromDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            fromCalendar.set(year, monthOfYear, dayOfMonth);
            mLoadType = LOAD_DAYS;

            ((EditText) findViewById(R.id.from_date)).setText(Common.fmtDate(fromCalendar));
            ((EditText) findViewById(R.id.to_date)).setText(Common.fmtDate(toCalendar));

            if (fromCalendar.getTimeInMillis() > toCalendar.getTimeInMillis()) {
                toCalendar = fromCalendar;
                toCalendar.add(Calendar.MONTH, 1);
            }

            loadNoteList();
        }
    };
    private DatePickerDialog.OnDateSetListener toDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            toCalendar.set(year, monthOfYear, dayOfMonth);
            mLoadType = LOAD_DAYS;

            ((EditText) findViewById(R.id.from_date)).setText(Common.fmtDate(fromCalendar));
            ((EditText) findViewById(R.id.to_date)).setText(Common.fmtDate(toCalendar));

            if (fromCalendar.getTimeInMillis() > toCalendar.getTimeInMillis()) {
                fromCalendar = toCalendar;
                fromCalendar.add(Calendar.MONTH, -1);
            }

            loadNoteList();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (mBibleID == -1) {
            mLoadType = LOAD_CHAPTER;
        }

        loadNoteList();

    }

    private void loadNoteList() {

        findViewById(R.id.daysviewcondition).setVisibility(View.GONE);
        findViewById(R.id.biblecontents).setVisibility(View.GONE);

        Cursor cursor;

        if ("Y".equals(searchYN)) {
            cursor = dao.querySearchContents(searchVersion, searchTestment, searchOperator, searchKeyword1, searchKeyword2);

        } else {

            switch (mLoadType) {
                case LOAD_ALL:
                    cursor = dao.queryNoteList(-1, -1);
                    break;
                case LOAD_BOOK:
                    findViewById(R.id.biblecontents).setVisibility(View.VISIBLE);

                    cursor = dao.queryNoteList(mBook, -1);
                    break;
                case LOAD_CHAPTER:
                    findViewById(R.id.biblecontents).setVisibility(View.VISIBLE);

                    cursor = dao.queryNoteList(mBook, mChapter);
                    break;
                case LOAD_DAYS:
                    findViewById(R.id.daysviewcondition).setVisibility(View.VISIBLE);

                    cursor = dao.queryNoteListbyDays(fromCalendar, toCalendar);
                    break;
                default:
                    cursor = dao.queryNoteList(0, 0);
                    break;
            }
        }
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.noteslist_item, cursor, new String[] { Notes.VERSESTR, Notes.MODIFIED_DATE, Notes.TITLE }, new int[] { R.id.versestr, R.id.modified_date, R.id.notetitle });
        //setListAdapter(adapter);
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // book이 지정되어있지 않으면 신규 추가는 불가능 하도록 한다.
        if (mBook >= 0) {
            menu.add(0, MENU_ITEM_ADDNOTE, 0, R.string.menu_addnote).setIcon(android.R.drawable.ic_menu_add);
        }
        menu.add(0, MENU_ITEM_ALLLIST, 0, R.string.menu_allnote).setIcon(android.R.drawable.ic_menu_agenda);
        menu.add(0, MENU_ITEM_BOOKLIST, 0, R.string.menu_booksnote).setIcon(android.R.drawable.ic_menu_agenda);

        menu.add(0, MENU_ITEM_DAYSLIST, 0, R.string.menu_daysnote).setIcon(android.R.drawable.ic_menu_agenda);

        MenuItem item1 = menu.add(0, MENU_ITEM_BIBLEVIEW, 0, R.string.menu_bibleview);
        item1.setIntent(new Intent(getBaseContext(), BibleViewer.class));
        item1.setIcon(R.drawable.app_notes);
        /*
                MenuItem item2 = menu.add(0, MENU_ITEM_EDITNOTE, 0, R.string.menu_editnote);
                item2.setIntent(new Intent(getBaseContext(), NoteEditor.class));
                item2.setIcon(android.R.drawable.ic_menu_edit);

                MenuItem item3 = menu.add(0, MENU_ITEM_DELNOTE, 0, R.string.menu_deletenote);
                item3.setIntent(new Intent(getBaseContext(), NoteEditor.class));
                item3.setIcon(android.R.drawable.ic_menu_delete);
        */
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (mListView != null) {
            final boolean haveItems = mListView.getCount() > 0;

            if (menu.findItem(MENU_ITEM_BIBLEVIEW) != null)
                menu.findItem(MENU_ITEM_BIBLEVIEW).setVisible(haveItems);
            if (menu.findItem(MENU_ITEM_EDITNOTE) != null)
                menu.findItem(MENU_ITEM_EDITNOTE).setVisible(haveItems);
            if (menu.findItem(MENU_ITEM_DELNOTE) != null)
                menu.findItem(MENU_ITEM_DELNOTE).setVisible(haveItems);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_ADDNOTE:

                Intent intent = new Intent();
                intent.setClass(this, NoteEditor.class);
                intent.putExtra("id", new Long(-1));
                intent.putExtra("bibleid", mBibleID);
                intent.putExtra("version", mVersion);
                intent.putExtra("book", mBook);
                intent.putExtra("chapter", mChapter);
                intent.putExtra("verse", mVerse);
                startActivity(intent);

                break;
            case MENU_ITEM_ALLLIST:

                mLoadType = LOAD_ALL;
                loadNoteList();

                break;
            case MENU_ITEM_BOOKLIST:
                mLoadType = LOAD_BOOK;
                loadNoteList();

                break;
            case MENU_ITEM_DAYSLIST:
                mLoadType = LOAD_DAYS;
                loadNoteList();

                break;
        }

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
        menu.setHeaderTitle(cursor.getString(Notes.COL_TITLE));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELNOTE, 0, R.string.menu_deletenote);

        /*
        Cursor cur_index = managedQuery(bindexuri, PROJECTION_BINDEX, null, null, Notes.BINDEX_SORT);

        if (cur_index.moveToNext()) {
            Uri uri = ContentUris.withAppendedId(Bible.CONTENTS_URI, cur_index.getInt(COLUMN_INDEX_BOOK));
            uri = ContentUris.withAppendedId(uri, cur_index.getInt(COLUMN_INDEX_CHAPTER));
            uri = ContentUris.withAppendedId(uri, cur_index.getInt(COLUMN_INDEX_VERSE));

            MenuItem item = menu.add(0, MENU_ITEM_SONGVIEW, 0, cur_index.getString(COLUMN_INDEX_VERSESTR));

            item.setIntent(new Intent(Intent.ACTION_VIEW, uri));

        }
        */

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
            case MENU_ITEM_DELNOTE: {
                Log.e(TAG, "MENU_ITEM_DELFAVORITE id = " + info.id);

                dao.delete(info.id);

                loadNoteList();
                return true;
            }
            case MENU_ITEM_BIBLEVIEW: {
                Log.e(TAG, "MENU_ITEM_SONGVIEW uri = " + item.getIntent().getData().toString());
                startActivity(item.getIntent());
                return true;
            }
        }
        return false;
    }

    public class listOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            Log.e(TAG, "onListItemClick position = " + pos);
            Log.e(TAG, "onListItemClick id = " + id);

            Intent intent = new Intent();
            intent.setClass(getBaseContext(), NoteEditor.class);
            intent.putExtra("id", id);
            intent.putExtra("bibleid", mBibleID);
            intent.putExtra("version", mVersion);
            intent.putExtra("book", mBook);
            intent.putExtra("chapter", mChapter);
            intent.putExtra("verse", mVerse);
            startActivity(intent);

        }

    }

}
