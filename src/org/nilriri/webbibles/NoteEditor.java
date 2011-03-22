package org.nilriri.webbibles;

import java.util.Calendar;

import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.NoteDao;
import org.nilriri.webbibles.dao.Constants.Notes;
import org.nilriri.webbibles.tools.SendMail;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A generic activity for editing a note in a database.  This can be used
 * either to simply view a note {@link Intent#ACTION_VIEW}, view and edit a note
 * {@link Intent#ACTION_EDIT}, or create a new note {@link Intent#ACTION_INSERT}.  
 */
public class NoteEditor extends Activity implements OnClickListener {
    private static final String TAG = "Notes";

    // This is our state data that is stored when freezing.
    private static final String ORIGINAL_CONTENT = "original";

    // Identifiers for our menu items.
    private static final int REVERT_ID = Menu.FIRST;
    private static final int DISCARD_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    public static final int BIBLEVIEW = Menu.FIRST + 3;
    public static final int BIBLECOPY = Menu.FIRST + 4;
    public static final int SENDSMS = Menu.FIRST + 5;
    public static final int SENDMAIL = Menu.FIRST + 6;
    public static final int NOTESHARE = Menu.FIRST + 7;

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int mState;
    private Long mBibleID = new Long(-1);
    private Long mID = new Long(-1);
    private int mVersion;
    private int mBook;
    private int mChapter;
    private int mVerse;

    private boolean mNoteOnly = false;
    private Cursor mCursor;
    private EditText mText;
    private TextView mVerseStr;
    private TextView mNoteTitle;
    private TextView mModifiedDate;
    private String mOriginalContent;

    private String[] mBibleShortName;

    private NoteDao dao;

    /**
     * A custom EditText that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // we need this constructor for LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);

            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = mRect;
            Paint paint = mPaint;

            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, r);

                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            super.onDraw(canvas);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dao = new NoteDao(this, null, Prefs.getSDCardUse(this));

        Log.e(TAG, " onCreate intent = ");

        final Intent intent = getIntent();

        mID = intent.getLongExtra("id", new Long(-1));
        String multiMsg = "";
        if (mID >= 0) {
            mState = STATE_EDIT;
        } else {
            mState = STATE_INSERT;

            mBibleID = intent.getLongExtra("bibleid", new Long(-1));
            mVersion = intent.getIntExtra("version", -1);
            mBook = intent.getIntExtra("book", -1);
            mChapter = intent.getIntExtra("chapter", -1);
            mVerse = intent.getIntExtra("verse", 1);

            multiMsg = "\"" + intent.getStringExtra("msg") + "\"";

        }

        setContentView(R.layout.note_editor);

        mBibleShortName = getResources().getStringArray(R.array.short_biblenames);

        // The text view for our note, identified by its ID in the XML file.
        mText = (EditText) findViewById(R.id.note);
        mVerseStr = (TextView) findViewById(R.id.versestr);
        mNoteTitle = (TextView) findViewById(R.id.notetitle);
        mModifiedDate = (TextView) findViewById(R.id.modified_date);

        if (mID > 0) {
            mNoteTitle.setOnClickListener(this);
        } else {
            mNoteTitle.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }

        mText.setText(multiMsg);
        //BibleVerseCopy();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.notetitle:

                Intent intent = new Intent();
                intent.setClass(this, TitleEditor.class);
                intent.putExtra("id", mID);
                startActivity(intent);
                break;

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mState == STATE_EDIT) {
            mCursor = dao.queryContents(mID);
            displayCursorData();

        } else {
            //TODO:성경구절과 내용을 기본값으로??
            setTitle(getText(R.string.title_newnote));

            mVerseStr.setText(mBibleShortName[mBook] + " " + (mChapter + 1) + ":" + (mVerse));
            mNoteTitle.setText(getText(R.string.new_init_title));
            mModifiedDate.setText(Common.fmtDate(Calendar.getInstance()));

        }

    }

    /**
     * 
     */
    private void displayCursorData() {
        if (mCursor.moveToNext()) {

            setTitle(getText(R.string.title_editnote));

            String note = mCursor.getString(Notes.COL_CONTENTS);
            String notetitle = mCursor.getString(Notes.COL_TITLE);
            String versestr = mCursor.getString(Notes.COL_VERSESTR);
            String modifieddate = mCursor.getString(Notes.COL_MODIFIED_DATE);

            mBibleID = mCursor.getLong(Notes.COL_BIBLEID);
            mVersion = mCursor.getInt(Notes.COL_VERSION);
            mBook = mCursor.getInt(Notes.COL_BOOK);
            mChapter = mCursor.getInt(Notes.COL_CHAPTER);
            mVerse = mCursor.getInt(Notes.COL_VERSE);

            mVerseStr.setTextKeepState(versestr);
            mNoteTitle.setText(notetitle);
            mText.setText(note);
            mModifiedDate.setText(modifieddate);

            Log.e(TAG, " EditText set!!! ");

            if (mOriginalContent == null) {
                mOriginalContent = note;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ContentValues values = new ContentValues();

        String modified_date = Common.fmtDate(Calendar.getInstance());
        //DateFormat.getDateInstance(DateFormat.FULL, Locale.KOREA).format(System.currentTimeMillis());

        values.put(Notes.BIBLEID, this.mBibleID);
        values.put(Notes.VERSION, this.mVersion);
        values.put(Notes.BOOK, this.mBook);
        values.put(Notes.CHAPTER, this.mChapter);
        values.put(Notes.VERSE, this.mVerse);
        values.put(Notes.VERSESTR, mBibleShortName[mBook] + " " + (mChapter + 1) + ":" + (mVerse));
        values.put(Notes.MODIFIED_DATE, modified_date);

        String title = (String) mNoteTitle.getText();
        String text = mText.getText().toString().trim();
        if ("".equals(text)) {
            setResult(RESULT_CANCELED);
        } else {
            if ("".equals(title.trim())) {
                title = text.substring(0, Math.min(30, text.length()));
                values.put(Notes.TITLE, title);
            } else {
                values.put(Notes.TITLE, title);
            }

            values.put(Notes.CONTENTS, text);

            if (mState == STATE_EDIT) {
                values.put(Notes._ID, this.mID);
                dao.update(values);
            } else {
                values.remove(Notes._ID);
                dao.insert(values);

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (mState == STATE_EDIT) {
            menu.add(0, REVERT_ID, 0, R.string.menu_revertnote).setShortcut('0', 'r').setIcon(android.R.drawable.ic_menu_revert);
            if (!mNoteOnly) {
                menu.add(0, DELETE_ID, 0, R.string.menu_deletenote).setShortcut('1', 'd').setIcon(android.R.drawable.ic_menu_delete);
            }
        } else {
            menu.add(0, DISCARD_ID, 0, R.string.menu_discard).setShortcut('2', 'c').setIcon(android.R.drawable.ic_menu_delete);
        }

        menu.add(0, BIBLECOPY, 0, R.string.menu_copybible).setShortcut('3', 'p').setIcon(android.R.drawable.ic_menu_crop);
        menu.add(0, NOTESHARE, 0, "Share").setShortcut('4', 't').setIcon(android.R.drawable.ic_menu_share);
        menu.add(0, SENDSMS, 0, R.string.menu_sendsms).setShortcut('5', 's').setIcon(android.R.drawable.ic_menu_send);
        menu.add(0, SENDMAIL, 0, R.string.menu_sendmail).setShortcut('6', 'm').setIcon(android.R.drawable.ic_menu_send);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case DELETE_ID:
                deleteNote();
                finish();
                break;
            case DISCARD_ID:
                cancelNote();
                break;
            case REVERT_ID:
                displayCursorData();
                break;
            case BIBLEVIEW:
                startActivity(item.getIntent());
                return true;
            case BIBLECOPY:
                BibleVerseCopy();

                return true;

            case NOTESHARE:
                //BibleVerseCopy();

                Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);

                picMessageIntent.setType("text/plain"); // 데이터의 종류

                //File downloadedPic =  new File(filePath, fileName);

                picMessageIntent.putExtra(Intent.EXTRA_TEXT, mText.getText().toString());
                //picMessageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadedPic));   

                startActivity(Intent.createChooser(picMessageIntent, "Send your note using:"));

                return true;
            case SENDSMS:
                Uri smsUri = Uri.parse("tel:/010");
                Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
                intent.putExtra("sms_body", mText.getText().toString());
                intent.setType("vnd.android-dir/mms-sms");
                startActivity(intent);
                return true;
            case SENDMAIL:

                if (Prefs.getGCalendarSync(this)) {
                    Intent gmailintent = new Intent();
                    gmailintent.setClass(this, SendMail.class);
                    gmailintent.putExtra("msgbody", mText.getText().toString().trim());
                    startActivity(gmailintent);

                } else {
                    /* Create the Intent */
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                    /* Fill it with Data */
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "somebody@gmail.com" });
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mNoteTitle.getText().toString());
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mText.getText().toString());

                    /* Send it off to the Activity-Chooser */
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 
     */
    public void BibleVerseCopy() {
        Cursor c = dao.queryBibleVerse(mVersion, mBook, mChapter, mVerse);

        if (c.moveToNext()) {
            String txt = this.mText.getText().toString();
            txt += "\"" + c.getString(2) + "\"";
            this.mText.setText(txt);
        }

        this.mText.selectAll();//.setSelection((int) mText.getTextSize() * 2);
    }

    /**
     * Take care of canceling work on a note.  Deletes the note if we
     * had created it, otherwise reverts to the original text.
     */
    private final void cancelNote() {
        setResult(RESULT_CANCELED);
        mText.setText("");
        finish();
    }

    /**
     * Take care of deleting a note.  Simply deletes the entry.
     */
    private final void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            dao.delete(mID);
            mText.setText("");
            finish();
        }
    }
}
