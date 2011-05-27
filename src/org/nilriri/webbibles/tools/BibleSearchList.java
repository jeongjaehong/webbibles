package org.nilriri.webbibles.tools;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nilriri.webbibles.BibleViewer;
import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BibleDao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
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
    private int totalcount = -1;
    private String searchKeyword1 = "";
    private String searchKeyword2 = "";

    private Cursor contents = null;
    private Cursor contents2 = null;

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
        mListView.setFastScrollEnabled(true);

        contents2 = dao.querySearchSummary(searchVersion, searchTestment, searchOperator, searchKeyword1, searchKeyword2);
        totalcount = contents2.getCount();
        if (contents2.moveToFirst()) {
            this.setTitle("결과 : " + contents2.getString(0));
        }

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
                    contents = dao.querySearchContents(searchVersion, searchTestment, searchOperator, searchKeyword1, searchKeyword2, totalcount);

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
            // SimpleCursorAdapter adapter = new SimpleCursorAdapter(getBaseContext(), R.layout.chapter_item, contents, new String[] { "empty", "contents" }, new int[] { R.id.versestr, R.id.contents });
            SimpleCursorAdapter adapter = new mySimpleAdapter(getBaseContext(), R.layout.chapter_searchresult, contents, new String[] { "empty", "contents" }, new int[] { R.id.versestr, R.id.contents });

            mListView.setAdapter(adapter);

            if (mListView.getCount() == 0) {
                Toast.makeText(BibleSearchList.this, "Not Found...", Toast.LENGTH_SHORT).show();
            }

            searchDialog.dismiss();

        }

    };

    private ArrayList<Hyperlink> listOfLinks;

    // A Listener Class for generally sending the Clicks to the one which requires it
    TextLinkClickListener mListener;

    // Pattern for gathering @usernames from the Text
    //Pattern screenNamePattern = Pattern.compile("(@[a-zA-Z0-9_]+)");

    // Pattern for gathering #hasttags from the Text
    //Pattern hashTagsPattern = Pattern.compile("(#[a-zA-Z0-9_-]+)");
    Pattern hashTagsPattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");

    // Pattern hashTagsPattern = Pattern.compile(searchKeyword1);
    //  Pattern hashTagsPattern2 = Pattern.compile(searchKeyword2);

    // Pattern for gathering http:// links from the Text
    //Pattern hyperLinksPattern = Pattern.compile("([Hh][tT][tT][pP][sS]?:\\/\\/[^ ,'\">\\]\\)]*[^\\. ,'\">\\]\\)])");

    private final void gatherLinks(ArrayList<Hyperlink> links, Spannable s, Pattern pattern) {
        // Matcher matching the pattern
        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            /*
            *  Hyperlink is basically used like a structure for storing the information about
            *  where the link was found.
            */
            Hyperlink spec = new Hyperlink();

            spec.textSpan = s.subSequence(start, end);
            spec.span = new InternalURLSpan(spec.textSpan.toString());
            spec.start = start;
            spec.end = end;

            links.add(spec);
        }
    }

    public void setOnTextLinkClickListener(TextLinkClickListener newListener) {
        mListener = newListener;
    }

    /*
    * This is class which gives us the clicks on the links which we then can use.
    */
    public class InternalURLSpan extends ClickableSpan

    {
        private String clickedSpan;

        public InternalURLSpan(String clickedString) {
            clickedSpan = clickedString;
        }

        @Override
        public void onClick(View textView) {
            mListener.onTextLinkClick(textView, clickedSpan);
        }
    }

    /*
    * Class for storing the information about the Link Location
    */

    class Hyperlink {
        CharSequence textSpan;
        InternalURLSpan span;
        int start;
        int end;

    }

    public interface TextLinkClickListener {

        /*
         *  This method is called when the TextLink is clicked from LinkEnabledTextView
         */
        public void onTextLinkClick(View textView, String clickedString);
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

                contents.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getFontSize(mContext));

                if (versestr != null) {
                 //   versestr.setText(mCursor.getString(6));
                }
                if (contents != null) {

                    SpannableString linkableText = new SpannableString(mCursor.getString(7));

                    listOfLinks = new ArrayList<Hyperlink>();

                    /*
                     *  gatherLinks basically collects the Links depending upon the Pattern that we supply
                     *  and add the links to the ArrayList of the links
                     */
                    //  gatherLinks(listOfLinks, linkableText, screenNamePattern);
                    gatherLinks(listOfLinks, linkableText, hashTagsPattern);
                    //  gatherLinks(listOfLinks, linkableText, hyperLinksPattern);
                    try {
                        for (int i = 0; i < listOfLinks.size(); i++) {
                            Hyperlink linkSpec = listOfLinks.get(i);
                            Log.v("vvvvv :: ", "===>" + mCursor.getString(6));
                            Log.v("listOfLinks :: " + linkSpec.textSpan, "start :: " + linkSpec.start + ", end :: " + linkSpec.end);
                            /*
                             * this process here makes the Clickable Links from the text
                             */
                            linkableText.setSpan(linkSpec.span, linkSpec.start, linkSpec.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } catch (Exception e) {

                    }
                    /*
                     * sets the text for the TextView with enabled links
                     */
                    contents.setText(linkableText);

                }

            }
            return v;

        }
    }

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
