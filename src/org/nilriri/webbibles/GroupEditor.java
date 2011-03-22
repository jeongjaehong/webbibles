package org.nilriri.webbibles;

import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.FavoritesDao;
import org.nilriri.webbibles.dao.Constants.FavoriteGroup;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GroupEditor extends Activity implements View.OnClickListener {

    private Long mID = new Long(-1);

    //private int mGroup ;= -1;
    private String mVerseStr = "";
    private String mContents = "";
    private int mVersion = -1;
    private int mBook = -1;
    private int mChapter = -1;
    private int mVerse = -1;

    private Cursor mCursor;

    private FavoritesDao dao;

    private EditText mText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dao = new FavoritesDao(this, null, Prefs.getSDCardUse(this));

        setContentView(R.layout.title_editor);

        Intent intent = getIntent();
        mID = intent.getLongExtra("id", new Long(-1));

        //mGroup = intent.getIntExtra("group", 0);
        mVerseStr = intent.getStringExtra("versestr");
        mContents = intent.getStringExtra("contents");
        mVersion = intent.getIntExtra("version", -1);
        mBook = intent.getIntExtra("book", -1);
        mChapter = intent.getIntExtra("chapter", -1);
        mVerse = intent.getIntExtra("verse", -1);

        mCursor = dao.queryFavoritesGroup(mID);

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
                mText.setText(mCursor.getString(FavoriteGroup.COL_GROUPNM));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCursor != null) {
            // Write the title back to the note 
            ContentValues values = new ContentValues();
            values.put(FavoriteGroup._ID, mID);
            values.put(FavoriteGroup.GROUPNM, mText.getText().toString());

            if (mID <= 0) {
                Long id = dao.insertFavoriteGroup(mText.getText().toString());

                dao.insertFavorites(id, mVerseStr, mContents, mVersion, mBook, mChapter, mVerse);
            } else {
                dao.updateFavoriteGroup(values);
            }

        }
    }

    public void onClick(View v) {
        finish();
    }
}
