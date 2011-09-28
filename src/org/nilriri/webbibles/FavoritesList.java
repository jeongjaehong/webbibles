package org.nilriri.webbibles;

import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.FavoritesDao;
import org.nilriri.webbibles.dao.Constants.FavoriteGroup;
import org.nilriri.webbibles.dao.Constants.Favorites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class FavoritesList extends Activity implements OnClickListener {
    private static final String TAG = "FavoritesList";

    // Menu item ids
    public static final int MENU_ITEM_DELFAVORITE = Menu.FIRST;
    public static final int MENU_ITEM_CHANGEGROUP = Menu.FIRST + 1;
    public static final int MENU_ITEM_SENDSMS = Menu.FIRST + 2;
    public static final int MENU_ITEM_SCHEDULESMS = Menu.FIRST + 3;
    public static final int MENU_ITEM_DELETEGROUP = Menu.FIRST + 4;

    private FavoritesDao dao;

    private ListView mListView = null;
    private Spinner mSpin_groups = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dao = new FavoritesDao(this, null, Prefs.getSDCardUse(this));

        setContentView(R.layout.favorite_view);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mListView = (ListView) findViewById(R.id.ContentsListView);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

        mSpin_groups = (Spinner) findViewById(R.id.groups);
        mSpin_groups.setOnItemSelectedListener(new groupSelectedListener());

    }

    public class groupSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            Cursor c = (Cursor) parent.getItemAtPosition(pos);

            if (c != null) {
                favoritesList(c.getLong(FavoriteGroup.COL_ID));
            }

            return;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

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

        Cursor cursor = dao.queryFavoriteGroupList();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, new String[] { FavoriteGroup.GROUPNM }, new int[] { android.R.id.text1 });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpin_groups.setAdapter(adapter);

        favoritesList(mSpin_groups.getSelectedItemId());
    }

    private void favoritesList(Long groupID) {

        Cursor cursor = dao.queryFavoritesList(groupID);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.favorite_item, cursor, new String[] { Favorites.GROUPNM, Favorites.VERSESTR, Favorites.CONTENTS }, new int[] { R.id.groupname, R.id.versestr, R.id.contents });
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
        menu.setHeaderTitle(cursor.getString(Favorites.COL_VERSESTR));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELFAVORITE, 0, R.string.menu_delfavorite);
        menu.add(0, MENU_ITEM_CHANGEGROUP, 0, R.string.menu_changegroup);
        menu.add(0, MENU_ITEM_DELETEGROUP, 0, R.string.menu_deletegroup);

        menu.add(0, MENU_ITEM_SENDSMS, 0, R.string.menu_sendsms);
        menu.add(0, MENU_ITEM_SCHEDULESMS, 0, R.string.menu_schedulesms);

    }

    private void openAddFavoritesDialog(final Long id, final Cursor c) {

        final Cursor groupCur = dao.queryFavNotExistsGroup(c.getInt(Favorites.COL_GROUPKEY));

        new AlertDialog.Builder(this).setTitle(R.string.select_favorites_group).setCursor(groupCur, new DialogInterface.OnClickListener() {
            private Long mFavoritesID = id;

            public void onClick(DialogInterface dialoginterface, int group) {

                if (groupCur.getInt(FavoriteGroup.COL_ID) <= 0) {
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), GroupEditor.class);
                    intent.putExtra("group", c.getLong(Favorites.COL_GROUPKEY));
                    intent.putExtra("versestr", c.getString(Favorites.COL_VERSESTR));
                    intent.putExtra("contents", c.getString(Favorites.COL_CONTENTS));
                    intent.putExtra("version", c.getLong(Favorites.COL_VERSION));
                    intent.putExtra("book", c.getLong(Favorites.COL_BOOK));
                    intent.putExtra("chapter", c.getLong(Favorites.COL_CHAPTER));
                    intent.putExtra("verse", c.getLong(Favorites.COL_VERSE));
                    startActivity(intent);
                } else {
                    dao.changeFavoriteGroup(mFavoritesID, groupCur.getInt(FavoriteGroup.COL_ID));
                }

                // 전체그룹을 다시 조회한다.
                favoritesList(new Long(-1));
            }

        }, FavoriteGroup.GROUPNM).show();

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
            case MENU_ITEM_DELFAVORITE: {
                Log.e(TAG, "MENU_ITEM_DELFAVORITE id = " + info.id);

                dao.deleteFavorite(info.id);

                favoritesList(new Long(-1));
                return true;
            }
            case MENU_ITEM_CHANGEGROUP: {
                Log.e(TAG, "MENU_ITEM_DELFAVORITE id = " + info.id);

                Cursor c = (Cursor) mListView.getItemAtPosition(info.position);

                //Toast.makeText(this, "" + info.id + "," + c.getInt(Favorites.COL_GROUPKEY), Toast.LENGTH_SHORT).show();

                openAddFavoritesDialog(info.id, c);

                return true;
            }
            case MENU_ITEM_DELETEGROUP: {
                Log.e(TAG, "MENU_ITEM_DELFAVORITE id = " + info.id);
                Log.e(TAG, "MENU_ITEM_DELFAVORITE position = " + info.position);
                Log.e(TAG, "MENU_ITEM_DELFAVORITE getItemId = " + item.getItemId());

                Cursor cursor = (Cursor) this.mListView.getItemAtPosition(info.position);

                if (cursor.getInt(Favorites.COL_GROUPKEY) == 0) {
                    Toast.makeText(this, "기본그룹은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    dao.deleteFavoritesGroup(cursor.getInt(Favorites.COL_GROUPKEY));

                    cursor = dao.queryFavoriteGroupList();
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, new String[] { FavoriteGroup.GROUPNM }, new int[] { android.R.id.text1 });
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpin_groups.setAdapter(adapter);

                    favoritesList(new Long(-1));
                }

                return true;
            }
            case MENU_ITEM_SENDSMS: {
                sendSMS(info.position);
                return true;
            }
        }
        return false;
    }

    private void sendSMS(int pos) {
        Cursor c = (Cursor) mListView.getItemAtPosition(pos);

        String msg = "";
        if (c.moveToNext()) {
            msg = c.getString(Favorites.COL_CONTENTS);
            msg += "(" + c.getString(Favorites.COL_VERSESTR) + ")";
        }
        c.close();

        Uri smsUri = Uri.parse("tel:/010");
        Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
        intent.putExtra("sms_body", msg);
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);

        /*
                SmsManager m = SmsManager.getDefault();
                String destination = "01052106848";
                if (PhoneNumberUtils.isWellFormedSmsAddress(destination)) {
                    m.sendTextMessage(destination, null, msg, null, null);
                }
          */

    }

    public class listOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            Log.e(TAG, "onListItemClick position = " + pos);
            Log.e(TAG, "onListItemClick id = " + id);

            Cursor c = (Cursor) parent.getItemAtPosition(pos);

            if (c != null) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), BibleViewer.class);
                intent.putExtra("VERSION", c.getInt(Favorites.COL_VERSION));
                intent.putExtra("BOOK", c.getInt(Favorites.COL_BOOK));
                intent.putExtra("CHAPTER", c.getInt(Favorites.COL_CHAPTER));
                intent.putExtra("VERSE", c.getInt(Favorites.COL_VERSE));

                Log.e(TAG, "VERSION = " + c.getInt(Favorites.COL_VERSION));
                Log.e(TAG, "BOOK = " + c.getInt(Favorites.COL_BOOK));
                Log.e(TAG, "CHAPTER = " + c.getInt(Favorites.COL_CHAPTER));
                Log.e(TAG, "VERSE = " + c.getInt(Favorites.COL_VERSE));

                startActivity(intent);

            }

        }

    }

}
