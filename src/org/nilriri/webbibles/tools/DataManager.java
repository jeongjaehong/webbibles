package org.nilriri.webbibles.tools;

import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.Constants;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DataManager extends Activity implements OnClickListener {
    private static final String TAG = "DataManager";

    //private static final int FROM_DATE_DIALOG_ID = 1;
    //private static final int TO_DATE_DIALOG_ID = 2;

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

    private ListView mListView = null;

    //private Long mBibleID = new Long(-1);
    //private int mVersion = -1;
    //private int mBook = -1;
    //private int mChapter = -1;
    //private int mVerse = -1;
    //private int mLoadType = LOAD_CHAPTER;
    //private Calendar fromCalendar;
    //private Calendar toCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.datamanager);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mListView = (ListView) findViewById(R.id.ContentsListView);

        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

        //ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.datamanagelist, android.R.layout.simple_list_item_multiple_choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.datamanagelist, android.R.layout.simple_list_item_1);

        mListView.setAdapter(adapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //findViewById(R.id.btn_prev).setOnClickListener(this);

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

            Intent intent = new Intent();
            switch (pos) {
                case 0:
                    intent.setClass(getBaseContext(), VersionSelector.class);
                    intent.putExtra("DATAMANAGE_WORK", Constants.DATAMANAGE_BACKUP);

                    if (Prefs.getSDCardUse(getBaseContext())) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(DataManager.this, "외부저장소 저장 불가.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    break;
                case 1:
                    intent.setClass(getBaseContext(), VersionSelector.class);
                    intent.putExtra("DATAMANAGE_WORK", Constants.DATAMANAGE_RESTORE);
                    startActivity(intent);
                    break;
                case 2:
                    intent.setClass(getBaseContext(), VersionSelector.class);
                    intent.putExtra("DATAMANAGE_WORK", Constants.DATAMANAGE_DELINTERNAL);
                    startActivity(intent);
                    break;
                case 3:
                    intent.setClass(getBaseContext(), VersionSelector.class);
                    intent.putExtra("DATAMANAGE_WORK", Constants.DATAMANAGE_DELEXTERNAL);

                    if (Prefs.getSDCardUse(getBaseContext())) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(DataManager.this, "외부저장소 접근 불가.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    startActivity(intent);
                    break;
                case 4:
                    intent.setClass(getBaseContext(), VersionSelector.class);
                    intent.putExtra("DATAMANAGE_WORK", Constants.DATAMANAGE_DOWNLOAD);
                    startActivity(intent);
                    break;
                case 5:
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);

                    /* Fill it with Data */
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "jeongjaehong@gmail.com" });
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Internet Bible]");
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "[Internet Bible]");

                    /* Send it off to the Activity-Chooser */
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));

                    break;
                case 6:
                    Intent browserIntent = new Intent(android.content.Intent.ACTION_VIEW);

                    browserIntent.setAction(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse("http://nilriri.blogspot.com/search/label/온라인성경"));

                    startActivity(browserIntent);

                    break;

            }

        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        /*
        switch (v.getId()) {
            case R.id.btn_prev:
        */

    }

}
