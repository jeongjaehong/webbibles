package org.nilriri.webbibles;

import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.NoteDao;
import org.nilriri.webbibles.dao.Constants.Notes;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TitleEditor extends Activity implements View.OnClickListener {

	private Long mID = new Long(-1);
	private Cursor mCursor;

	private NoteDao dao;

	private EditText mText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dao = new NoteDao(this, null, Prefs.getSDCardUse(this));

		setContentView(R.layout.title_editor);

		Intent intent = getIntent();
		mID = intent.getLongExtra("id", new Long(-1));
		mCursor = dao.queryContents(mID);

		mText = (EditText) this.findViewById(R.id.title);
		mText.setOnClickListener(this);

		Button b = (Button) findViewById(R.id.ok);
		b.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mCursor != null) {
			if (mCursor.moveToFirst()) {
				mText.setText(mCursor.getString(Notes.COL_TITLE));
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mCursor != null) {
			// Write the title back to the note
			ContentValues values = new ContentValues();
			values.put(Notes._ID, mID);
			values.put(Notes.TITLE, mText.getText().toString());
			dao.update(values);

		}
	}

	public void onClick(View v) {

		if (v.getId() == R.id.ok)
			finish();
	}
}
