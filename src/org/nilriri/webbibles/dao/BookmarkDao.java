package org.nilriri.webbibles.dao;

import java.util.Calendar;

import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.dao.Constants.Bibles;
import org.nilriri.webbibles.dao.Constants.Bookmark;
import org.nilriri.webbibles.dao.Constants.Favorites;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class BookmarkDao extends AbstractDao {

    public BookmarkDao(Context context, CursorFactory factory, boolean sdcarduse) {
        super(context, factory, sdcarduse);
    }

    public void delete(Long id) {

        SQLiteDatabase db = getWritableDatabase();

        String whereClause = Bookmark._ID + "=?";
        String whereArgs[] = new String[] { id.toString() };

        db.delete(Bookmark.BOOKMARK_TABLE_NAME, whereClause, whereArgs);

    }

    public void insert(long bibleid, String vercode, int version, int book, int chapter, int verse, String versestr) {

        ContentValues values = new ContentValues();

        values.put(Bookmark.VERCODE, vercode);
        values.put(Bookmark.BIBLEID, bibleid);
        values.put(Bookmark.VERSION, version);
        values.put(Bookmark.BOOK, book);
        values.put(Bookmark.CHAPTER, chapter);
        values.put(Bookmark.VERSE, verse);
        values.put(Bookmark.MODIFIED_DATE, Common.fmtDate(Calendar.getInstance()));
        values.put(Bookmark.VERSESTR, versestr);

        Log.d("InternalDao-insert", "insert values = " + values.toString());

        SQLiteDatabase db = getWritableDatabase();

        db.insert(Bookmark.BOOKMARK_TABLE_NAME, null, values);

    }

    public Cursor queryBibleVerse(int p_version, int p_book, int p_chapter, int p_verse) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append(" " + Bibles._ID);
        query.append(" ," + Bibles.VERNAME);
        query.append(" ," + Bibles.CONTENTS);
        query.append(" FROM " + Bibles.VERSION_TABLE_NAME[p_version] + " ");
        query.append(" WHERE " + Bibles.VERSION + " = " + p_version);
        query.append(" AND " + Bibles.BOOK + " = " + p_book);
        query.append(" AND " + Bibles.CHAPTER + " = " + p_chapter);
        query.append(" AND cast(" + Bibles.VERSE + " as integer) = " + p_verse);

        Log.d("InternalDao-queryBibleVerse", "query = " + query.toString());

        Cursor cursor = db.rawQuery(query.toString(), null);

        return cursor;

    }

    public Cursor queryBookmarkTopList(int limit) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  0 " + Bookmark._ID);
        query.append(" ,0 " + Bookmark.VERCODE);
        query.append(" ,0 " + Bookmark.BIBLEID);
        query.append(" ,0 " + Bookmark.VERSION);
        query.append(" ,0 " + Bookmark.BOOK);
        query.append(" ,0 " + Bookmark.CHAPTER);
        query.append(" ,0 " + Bookmark.VERSE);
        query.append(" ,strftime('%Y-%m-%d','now','localtime') " + Bookmark.MODIFIED_DATE);
        query.append(" ,'Bookmark' " + Bookmark.VERSESTR);
        query.append(" UNION ALL  ");
        query.append(" SELECT  ");
        query.append("  " + Bookmark._ID);
        query.append(" ," + Bookmark.VERCODE);
        query.append(" ," + Bookmark.BIBLEID);
        query.append(" ," + Bookmark.VERSION);
        query.append(" ," + Bookmark.BOOK);
        query.append(" ," + Bookmark.CHAPTER);
        query.append(" ," + Bookmark.VERSE);
        query.append(" ," + Bookmark.MODIFIED_DATE);
        query.append(" ," + Bookmark.VERSESTR);
        query.append(" FROM " + Bookmark.BOOKMARK_TABLE_NAME + " ");

        query.append(" ORDER BY " + Bookmark.MODIFIED_DATE + " DESC, _id ASC ");
        if (limit > 0) {
            query.append(" LIMIT " + limit);
        }

        Cursor cursor = db.rawQuery(query.toString(), null);

        return cursor;

    }

    public Cursor queryFavoritesTopList(int limit) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  9999999 " + Favorites._ID);
        query.append(" ,0 " + Favorites.BIBLEID);
        query.append(" ,0 " + Favorites.GROUPKEY);
        query.append(" ,'Favorites' " + Favorites.VERSESTR);
        query.append(" ,0 " + Favorites.VERSION);
        query.append(" ,0 " + Favorites.BOOK);
        query.append(" ,0 " + Favorites.CHAPTER);
        query.append(" ,0 " + Favorites.VERSE);
        query.append(" ,0 " + Favorites.CONTENTS);
        query.append(" UNION ALL  ");
        query.append(" SELECT  ");
        query.append("  9999998 " + Favorites._ID);
        query.append(" ,0 " + Favorites.BIBLEID);
        query.append(" ,0 " + Favorites.GROUPKEY);
        query.append(" ,'Favorites ¸ñ·Ï' " + Favorites.VERSESTR);
        query.append(" ,0 " + Favorites.VERSION);
        query.append(" ,0 " + Favorites.BOOK);
        query.append(" ,0 " + Favorites.CHAPTER);
        query.append(" ,0 " + Favorites.VERSE);
        query.append(" ,0 " + Favorites.CONTENTS);
        query.append(" UNION ALL  ");
        query.append(" SELECT  ");
        query.append("  " + Favorites._ID);
        query.append(" ," + Favorites.BIBLEID);
        query.append(" ," + Favorites.GROUPKEY);
        query.append(" ," + Favorites.VERSESTR);
        query.append(" ," + Favorites.VERSION);
        query.append(" ," + Favorites.BOOK);
        query.append(" ," + Favorites.CHAPTER);
        query.append(" ," + Favorites.VERSE);
        query.append(" , " + Favorites.CONTENTS);
        query.append(" FROM " + Favorites.FAVORITES_TABLE_NAME + " ");

        query.append(" ORDER BY " + Favorites._ID + " DESC ");
        if (limit > 0) {
            query.append(" LIMIT " + limit);
        }

        Cursor cursor = db.rawQuery(query.toString(), null);

        return cursor;

    }

    public Cursor queryBookmarkList(int limit) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  " + Bookmark._ID);
        query.append(" ," + Bookmark.VERCODE);
        query.append(" ," + Bookmark.BIBLEID);
        query.append(" ," + Bookmark.VERSION);
        query.append(" ," + Bookmark.BOOK);
        query.append(" ," + Bookmark.CHAPTER);
        query.append(" ," + Bookmark.VERSE);
        query.append(" ," + Bookmark.MODIFIED_DATE);
        query.append(" ," + Bookmark.VERSESTR);
        query.append(" FROM " + Bookmark.BOOKMARK_TABLE_NAME + " ");

        query.append(" ORDER BY " + Bookmark.MODIFIED_DATE + " DESC ");
        if (limit > 0) {
            query.append(" LIMIT " + limit);
        }

        Cursor cursor = db.rawQuery(query.toString(), null);

        return cursor;

    }

}
