package org.nilriri.webbibles.dao;

import org.nilriri.webbibles.R;
import org.nilriri.webbibles.dao.Constants.Bibles;
import org.nilriri.webbibles.dao.Constants.Bookmark;
import org.nilriri.webbibles.dao.Constants.FavoriteGroup;
import org.nilriri.webbibles.dao.Constants.Favorites;
import org.nilriri.webbibles.dao.Constants.Notes;
import org.nilriri.webbibles.dao.Constants.Schedule;
import org.nilriri.webbibles.dao.Constants.Songs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DaoCreator {

    public void onCreate(Context context, SQLiteDatabase db) {

        Log.d("onCreate", "CurrentVersion=" + db.getVersion());
        onCreateBible(db, 0);

        onCreateNote(db, 0);

        onCreateBookmark(db, 0);

        onCreateIndex(db, 0);

        onCreateFavorites(db);

        onCreateBibles(context, db);

        onCreateSchedule(db);

        onCreateSongs(db);

        onCreateReading(db);
    }

    public void onCreateBible(SQLiteDatabase db, int start) {

        StringBuffer query = null;

        int limit = Bibles.VERSION_TABLE_NAME.length;

        for (int i = start; i < limit; i++) {
            query = new StringBuffer();
            try {
                query.append("CREATE TABLE " + Bibles.VERSION_TABLE_NAME[i] + " (");
                query.append("    " + Bibles._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
                query.append("    ," + Bibles.VERCODE + " VARCHAR ");
                query.append("    ," + Bibles.VERNAME + " VARCHAR ");
                query.append("    ," + Bibles.VERSION + " INTEGER ");
                query.append("    ," + Bibles.BOOK + " INTEGER ");
                query.append("    ," + Bibles.CHAPTER + " INTEGER ");
                query.append("    ," + Bibles.VERSE + " INTEGER ");
                query.append("    ," + Bibles.CONTENTS + " VARCHAR ");
                query.append("    ," + Bibles.STYLE + " INTEGER ");
                query.append("    ) ");

                Log.d("onCreate", "Create....Bible Database=" + query.toString());

                db.execSQL(query.toString());
            } catch (Exception e) {

            }
        }

    }

    public void onCreateNote(SQLiteDatabase db, int start) {

        StringBuffer query = null;
        try {
            query = new StringBuffer();
            query.append("CREATE TABLE " + Notes.NOTE_TABLE_NAME + " (");
            query.append("    " + Notes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
            query.append("    ," + Notes.BIBLEID + " INTEGER ");
            query.append("    ," + Notes.VERSION + " INTEGER ");
            query.append("    ," + Notes.BOOK + " INTEGER ");
            query.append("    ," + Notes.CHAPTER + " INTEGER ");
            query.append("    ," + Notes.VERSE + " INTEGER ");
            query.append("    ," + Notes.VERSESTR + " VARCHAR ");
            query.append("    ," + Notes.TITLE + " VARCHAR ");
            query.append("    ," + Notes.CONTENTS + " VARCHAR ");
            query.append("    ," + Notes.MODIFIED_DATE + " VARCHAR ");
            query.append("    ," + Notes.SCORE + " INTEGER ");
            query.append("    ) ");

            db.execSQL(query.toString());
        } catch (Exception e) {

        }
    }

    public void onCreateReading(SQLiteDatabase db) {

        StringBuffer query = null;
        try {
            query = new StringBuffer();
            query.append("CREATE TABLE reading (");
            query.append("    " + Notes._ID + " INTEGER NOT NULL ");
            query.append("    ," + Notes.VERSION + " INTEGER ");
            query.append("    ," + Notes.BOOK + " INTEGER ");
            query.append("    ," + Notes.CHAPTER + " INTEGER ");
            query.append("    ," + Notes.VERSE + " INTEGER ");
            query.append("    ," + Notes.VERSESTR + " VARCHAR ");
            query.append("    ," + Notes.TITLE + " VARCHAR ");
            query.append("    ," + Notes.CONTENTS + " VARCHAR ");
            query.append("    ," + Notes.MODIFIED_DATE + " VARCHAR ");
            query.append("    ," + Notes.SCORE + " INTEGER ");
            query.append("  ,  PRIMARY KEY (version, book, chapter )) ");

            db.execSQL(query.toString());
        } catch (Exception e) {

        }
    }

    public void onCreateBookmark(SQLiteDatabase db, int start) {

        StringBuffer query = null;
        try {
            query = new StringBuffer();
            query.append("CREATE TABLE " + Bookmark.BOOKMARK_TABLE_NAME + " (");
            query.append("    " + Bookmark._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
            query.append("    ," + Bookmark.VERCODE + " VARCHAR ");
            query.append("    ," + Bookmark.BIBLEID + " INTEGER ");
            query.append("    ," + Bookmark.VERSION + " INTEGER ");
            query.append("    ," + Bookmark.BOOK + " INTEGER ");
            query.append("    ," + Bookmark.CHAPTER + " INTEGER ");
            query.append("    ," + Bookmark.VERSE + " INTEGER ");
            query.append("    ," + Bookmark.MODIFIED_DATE + " VARCHAR ");
            query.append("    ," + Bookmark.VERSESTR + " VARCHAR ");
            query.append("    ) ");

            Log.d("onCreate", "Create....Bookmark Database=" + query.toString());

            db.execSQL(query.toString());
        } catch (Exception e) {

        }
    }

    public void onCreateFavorites(SQLiteDatabase db) {

        StringBuffer query = null;
        try {
            //db.execSQL("DROP TABLE IF EXISTS " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME);
            //db.execSQL("DROP TABLE IF EXISTS " + Favorites.FAVORITES_TABLE_NAME);

            query = new StringBuffer();
            query.append("CREATE TABLE " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " (");
            query.append("    " + Favorites._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
            query.append("    ," + Favorites.GROUPNM + " VARCHAR ");
            query.append("    ) ");

            Log.d("onCreate", "Create....Favorites Database=" + query.toString());

            db.execSQL(query.toString());
        } catch (Exception e) {

        }
        try {
            query = new StringBuffer();
            query.append("CREATE TABLE " + Favorites.FAVORITES_TABLE_NAME + " (");
            query.append("    " + Favorites._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
            query.append("    ," + Favorites.BIBLEID + " INTEGER ");
            query.append("    ," + Favorites.GROUPKEY + " INTEGER ");
            query.append("    ," + Favorites.VERSESTR + " VARCHAR ");
            query.append("    ," + Favorites.VERSION + " INTEGER ");
            query.append("    ," + Favorites.BOOK + " INTEGER ");
            query.append("    ," + Favorites.CHAPTER + " INTEGER ");
            query.append("    ," + Favorites.VERSE + " INTEGER ");
            query.append("    ," + Favorites.CONTENTS + " VARCHAR ");
            query.append("    ) ");

            Log.d("onCreate", "Create....Favorites Database=" + query.toString());

            db.execSQL(query.toString());
        } catch (Exception e) {

        }
    }

    public void onCreateSchedule(SQLiteDatabase db) {
        try {
            StringBuffer query = null;

            query = new StringBuffer();
            query.append("CREATE TABLE " + Schedule.SCHEDULE_TABLE_NAME + " (");
            query.append("    " + Schedule._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
            query.append("    ," + Schedule.TONUMBER + " VARCHAR ");
            query.append("    ," + Schedule.TONAME + " VARCHAR ");
            query.append("    ," + Schedule.STARTDAY + " VARCHAR ");
            query.append("    ," + Schedule.ENDDAY + " VARCHAR ");
            query.append("    ," + Schedule.SENDTIME + " VARCHAR ");
            query.append("    ," + Schedule.SENDGROUP + " INTEGER ");
            query.append("    ) ");

            Log.d("onCreate", "Create....Schedule Database=" + query.toString());

            //db.execSQL("DROP TABLE IF EXISTS " + Schedule.SCHEDULE_TABLE_NAME);
            db.execSQL(query.toString());
        } catch (Exception e) {

        }
    }

    public void onCreateSongs(SQLiteDatabase db) {

        StringBuffer query = null;
        try {
            //db.execSQL("DROP TABLE IF EXISTS " + Songs.SONGS_TABLE_NAME);

            query = new StringBuffer();
            query.append("CREATE TABLE " + Songs.SONGS_TABLE_NAME + " (");
            query.append("    " + Songs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
            query.append("    ," + Songs.VERSION + " INTEGER ");
            query.append("    ," + Songs.SONGID + " INTEGER ");
            query.append("    ," + Songs.SONGTEXT + " VARCHAR ");
            query.append("    ," + Songs.SUBJECT + " VARCHAR ");
            query.append("    ," + Songs.TITLE + " VARCHAR ");
            query.append("    ," + Songs.IMGURL + " VARCHAR ");
            query.append("    ) ");

            Log.d("onCreate", "Create....Songs Database=" + query.toString());

            db.execSQL(query.toString());
        } catch (Exception e) {

        }
    }

    public void onCreateIndex(SQLiteDatabase db, int start) {

        int limit = Bibles.VERSION_TABLE_NAME.length;

        for (int i = start; i < limit; i++) {
            try {
                db.execSQL("CREATE INDEX idx_" + Bibles.VERSION_TABLE_NAME[i] + "_01 ON " + Bibles.VERSION_TABLE_NAME[i] + "  (version ASC) ");
                db.execSQL("CREATE INDEX idx_" + Bibles.VERSION_TABLE_NAME[i] + "_02 ON " + Bibles.VERSION_TABLE_NAME[i] + "  (vercode ASC) ");
                db.execSQL("CREATE INDEX idx_" + Bibles.VERSION_TABLE_NAME[i] + "_03 ON " + Bibles.VERSION_TABLE_NAME[i] + "  (vercode, book, chapter ASC) ");
                db.execSQL("CREATE INDEX idx_" + Bibles.VERSION_TABLE_NAME[i] + "_04 ON " + Bibles.VERSION_TABLE_NAME[i] + "  (book, chapter ASC) ");
            } catch (Exception e) {

            }

        }

        Log.d("onCreate", "Create....Index");
    }

    public void onAlterBible(SQLiteDatabase db, int start) {

        StringBuffer query = null;

        int limit = Bibles.VERSION_TABLE_NAME.length;

        for (int i = start; i < limit; i++) {
            query = new StringBuffer();
            try {
                query.append("ALTER TABLE " + Bibles.VERSION_TABLE_NAME[i] + " ");
                query.append(" ADD " + Bibles.STYLE + " INTEGER DEFAULT 0 NOT NULL ");

                Log.d("onCreate", "ALTER....Bible Database=" + query.toString());

                db.execSQL(query.toString());
            } catch (Exception e) {

            }
        }

    }

    public void onCreateBibles(Context context, SQLiteDatabase db) {
        Log.d("onCreate", "onCreateBibles...");

        String[] bibles = context.getResources().getStringArray(R.array.short_biblenames);

        try {
            db.execSQL("drop table if exists biblenames ");

            db.execSQL("create table biblenames (_id integer, biblename varchar) ");
            for (int i = 0; i < bibles.length; i++) {
                db.execSQL("INSERT INTO biblenames (_id , biblename) values (" + i + ", '" + bibles[i] + "')");
            }
        } catch (Exception e) {

        }

    }

    public void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d("onUpgrade_Schedule", "oldVersion=" + oldVersion + ",newVersion=" + newVersion);

        switch (newVersion) {
            case 1: {
                onCreate(context, db);
                break;
            }

            default: {
                onCreateNote(db, 0);

                onCreateBookmark(db, 0);

                onCreateIndex(db, 0);

                onCreateFavorites(db);

                onCreateBibles(context, db);

                onCreateSchedule(db);

                onCreateSongs(db);

                onAlterBible(db, 0);

                onCreateReading(db);
                break;
            }
        }
    }

}
