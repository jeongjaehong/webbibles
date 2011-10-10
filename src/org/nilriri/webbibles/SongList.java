package org.nilriri.webbibles;

import java.io.File;

import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.SongsDao;
import org.nilriri.webbibles.dao.Constants.Songs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class SongList extends Activity implements OnClickListener {
	private static final String TAG = "SongList";

	// Menu item ids
	public static final int MENU_ITEM_SONGVIEW = Menu.FIRST + 1;

	private SongsDao dao;

	private ListView mListView = null;
	private Spinner mSongversion = null;
	private Spinner mSubject = null;
	private String mSubjectText = "";
	private TextView edt_songid;
	private Button btn_goto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dao = new SongsDao(this, null, Prefs.getSDCardUse(this));

		setContentView(R.layout.songlist_view);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		mSongversion = (Spinner) findViewById(R.id.songversion);
		mSubject = (Spinner) findViewById(R.id.subject);

		edt_songid = (TextView) findViewById(R.id.song_id);
		btn_goto = (Button) findViewById(R.id.btn_goto);
		btn_goto.setOnClickListener(this);

		mSongversion.setOnItemSelectedListener(new VersionSelectedListener());
		mSubject.setOnItemSelectedListener(new SubjectSelectedListener());

		Cursor cursor = dao.querySubjectList(0);
		SimpleCursorAdapter subjectAdapter = new SimpleCursorAdapter(getBaseContext(), android.R.layout.simple_spinner_item, cursor, new String[] { Songs.SUBJECT }, new int[] { android.R.id.text1 });
		subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSubject.setAdapter(subjectAdapter);

		mListView = (ListView) findViewById(R.id.ContentsListView);
		mListView.setOnCreateContextMenuListener(this);
		mListView.setOnItemClickListener(new listOnItemClickListener());

	}

	@Override
	public void onClick(View v) {
		String songtext = edt_songid.getText().toString();
		songtext = (songtext == null || "".equals(songtext) ? "1" + "" : songtext);

		switch (v.getId()) {
		case R.id.btn_goto:

			Intent intent = new Intent();
			intent.setClass(getBaseContext(), Song.class);

			intent.putExtra("version", mSongversion.getSelectedItemPosition());
			intent.putExtra("mSongid", Integer.parseInt(songtext));

			startActivity(intent);

			this.finish();

			break;

		}

	}

	public class SubjectSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

			mSubjectText = "";
			Cursor cursor = (Cursor) mSubject.getSelectedItem();
			if (cursor != null) {
				mSubjectText = cursor.getString(1);
			}
			loadSongList();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			mSubjectText = "";
		}
	}

	public class VersionSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

			Cursor cursor = dao.querySubjectList(pos);
			SimpleCursorAdapter subjectAdapter = new SimpleCursorAdapter(getBaseContext(), android.R.layout.simple_spinner_item, cursor, new String[] { Songs.SUBJECT }, new int[] { android.R.id.text1 });
			subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mSubject.setAdapter(subjectAdapter);

		}

		public void onNothingSelected(AdapterView<?> parent) {
			// mSubjectText = "";
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadSongList();

	}

	private void loadSongList() {

		Cursor cursor = dao.querySongsList(mSongversion.getSelectedItemPosition(), mSubjectText);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.song_item, cursor, new String[] { Songs.TITLE, Songs.SONGTEXT }, new int[] { R.id.title, R.id.songtext });
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
		// final Intent intent = getIntent();

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
		menu.setHeaderTitle(cursor.getString(Songs.COL_TITLE));

		// Add a menu item to delete the note
		menu.add(0, this.MENU_ITEM_SONGVIEW, 0, "찬송가 악보 보기");

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
		case MENU_ITEM_SONGVIEW: {
			Cursor cursor = (Cursor) this.mListView.getItemAtPosition(info.position);

			if (cursor != null) {
				String imageid = cursor.getString(Songs.COL_SONGID);
				imageid = imageid.length() == 1 ? "00" + imageid : imageid;
				imageid = imageid.length() == 2 ? "0" + imageid : imageid;

				File imgFile = new File(Environment.getExternalStorageDirectory() + "/" + (mSongversion.getSelectedItemPosition() == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mSongversion.getSelectedItemPosition() == 0 ? ".gif" : ".jpg") + "");

				Log.d("WebBibles", "Path=" + imgFile.getAbsolutePath());

				Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + imgFile.getAbsolutePath()), "image/*");
				startActivity(intent);
			}

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
				intent.setClass(getBaseContext(), Song.class);
				intent.putExtra("version", c.getInt(Songs.COL_VERSION));
				intent.putExtra("mSongid", c.getInt(Songs.COL_SONGID));
				startActivity(intent);

				finish();
			}

		}

	}

}
