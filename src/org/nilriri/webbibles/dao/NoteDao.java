package org.nilriri.webbibles.dao;

import java.util.Calendar;

import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.dao.Constants.Bibles;
import org.nilriri.webbibles.dao.Constants.Notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class NoteDao extends AbstractDao {

    public NoteDao(Context context, CursorFactory factory, boolean sdcarduse) {
        super(context, factory, sdcarduse);
    }

    public void delete(Long id) {

        SQLiteDatabase db = getWritableDatabase();

        String whereClause = Notes._ID + "=?";
        String whereArgs[] = new String[] { id.toString() };

        db.delete(Notes.NOTE_TABLE_NAME, whereClause, whereArgs);

    }

    public void insert(ContentValues values) {
        Log.d("InternalDao-insert", "insert values = " + values.toString());

        SQLiteDatabase db = getWritableDatabase();

        db.insert(Notes.NOTE_TABLE_NAME, null, values);

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

    public Cursor queryNoteList(int p_book, int p_chapter) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  " + Notes._ID);
        query.append(" ," + Notes.BIBLEID);
        query.append(" ," + Notes.VERSION);
        query.append(" ," + Notes.BOOK);
        query.append(" ," + Notes.CHAPTER);
        query.append(" ," + Notes.VERSE);
        query.append(" ," + Notes.VERSESTR);
        query.append(" ," + Notes.TITLE);
        query.append(" ," + Notes.CONTENTS);
        query.append(" ," + Notes.MODIFIED_DATE);
        query.append(" ," + Notes.SCORE);
        query.append(" FROM " + Notes.NOTE_TABLE_NAME + " ");

        if (p_book >= 0)
            query.append(" WHERE " + Notes.BOOK + " = " + p_book);

        if (p_chapter >= 0)
            query.append(" AND " + Notes.CHAPTER + " = " + p_chapter);

        query.append(" ORDER BY " + Notes.MODIFIED_DATE + " DESC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor querySearchContents(int searchVersion, int searchTestment, int searchOperator, String searchKeyword1, String searchKeyword2) {

        Log.d("InternalDao-querySearchContents", "searchVersion=" + searchVersion);
        StringBuffer query = new StringBuffer();

        query.append("SELECT  ");
        query.append("  c." + Notes._ID);
        query.append(" ,c." + Notes.BIBLEID);
        query.append(" ,c." + Notes.VERSION);
        query.append(" ,c." + Notes.BOOK);
        query.append(" ,c." + Notes.CHAPTER);
        query.append(" ,c." + Notes.VERSE);
        query.append(" ,c." + Notes.VERSESTR);
        query.append(" ,c." + Notes.TITLE);
        query.append(" ,c." + Notes.CONTENTS);
        query.append(" ,c." + Notes.MODIFIED_DATE);
        query.append(" ,c." + Notes.SCORE);
        query.append(" FROM " + Notes.NOTE_TABLE_NAME + " c ");
        query.append(" WHERE 1 = 1 ");

        switch (searchOperator) {
            case 0: // and
                if (!"".equals(searchKeyword1)) {
                    query.append("  AND (c." + Notes.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  OR c." + Notes.TITLE + " LIKE " + "'%" + searchKeyword1 + "%')");
                }
                if (!"".equals(searchKeyword2)) {
                    query.append("  AND (c." + Notes.CONTENTS + " LIKE " + "'%" + searchKeyword2 + "%'");
                    query.append("  OR c." + Notes.TITLE + " LIKE " + "'%" + searchKeyword2 + "%')");
                }
                break;
            case 1: // or
                if (!"".equals(searchKeyword1) && !"".equals(searchKeyword2)) {
                    query.append("  AND (c." + Notes.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  OR c." + Notes.CONTENTS + " LIKE " + "'%" + searchKeyword2 + "%'");
                    query.append("  OR c." + Notes.TITLE + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  OR c." + Notes.TITLE + " LIKE " + "'%" + searchKeyword2 + "%')");
                } else if (!"".equals(searchKeyword1) && "".equals(searchKeyword2)) {
                    query.append("  AND (c." + Notes.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  OR c." + Notes.TITLE + " LIKE " + "'%" + searchKeyword1 + "%')");
                }
                break;
            case 2: // not
                if (!"".equals(searchKeyword1) && !"".equals(searchKeyword2)) {
                    query.append("  AND c." + Notes.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  AND c." + Notes.CONTENTS + " NOT LIKE " + "'%" + searchKeyword2 + "%'");
                } else if (!"".equals(searchKeyword1) && "".equals(searchKeyword2)) {
                    query.append("  AND c." + Notes.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                }
                break;

        }
        query.append(" ORDER BY " + Notes.MODIFIED_DATE + " DESC ");
        query.append("  LIMIT 100 ");

        Log.d("InternalDao-querySearchContents", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryNoteListbyDays(Calendar from, Calendar to) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  " + Notes._ID);
        query.append(" ," + Notes.BIBLEID);
        query.append(" ," + Notes.VERSION);
        query.append(" ," + Notes.BOOK);
        query.append(" ," + Notes.CHAPTER);
        query.append(" ," + Notes.VERSE);
        query.append(" ," + Notes.VERSESTR);
        query.append(" ," + Notes.TITLE);
        query.append(" ," + Notes.CONTENTS);
        query.append(" ," + Notes.MODIFIED_DATE);
        query.append(" ," + Notes.SCORE);
        query.append(" FROM " + Notes.NOTE_TABLE_NAME + " ");

        query.append(" WHERE " + Notes.MODIFIED_DATE);
        query.append(" BETWEEN strftime('%Y-%m-%d', '" + Common.fmtDate(from) + "', 'localtime') ");
        query.append(" AND strftime('%Y-%m-%d', '" + Common.fmtDate(to) + "', 'localtime') ");

        query.append(" ORDER BY " + Notes.MODIFIED_DATE + " DESC ");

        Cursor cursor = db.rawQuery(query.toString(), null);

        return cursor;

    }

    public Cursor queryContents(Long id) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  " + Notes._ID);
        query.append(" ," + Notes.BIBLEID);
        query.append(" ," + Notes.VERSION);
        query.append(" ," + Notes.BOOK);
        query.append(" ," + Notes.CHAPTER);
        query.append(" ," + Notes.VERSE);
        query.append(" ," + Notes.VERSESTR);
        query.append(" ," + Notes.TITLE);
        query.append(" ," + Notes.CONTENTS);
        query.append(" ," + Notes.MODIFIED_DATE);
        query.append(" ," + Notes.SCORE);
        query.append(" FROM " + Notes.NOTE_TABLE_NAME + " ");
        query.append(" WHERE " + Notes._ID + " = ? ");

        Log.d("InternalDao-queryContents", "query = " + query.toString());
        Log.d("InternalDao-queryContents", "id = " + id.toString());

        String selectionArgs[] = new String[] { id.toString() };

        Cursor cursor = db.rawQuery(query.toString(), selectionArgs);

        return cursor;
    }

    public void update(ContentValues values) {

        Log.d("InternalDao-update", "values = " + values.toString());

        String[] args = new String[] { values.get(Notes._ID).toString() };
        SQLiteDatabase db = getWritableDatabase();
        db.update(Notes.NOTE_TABLE_NAME, values, Notes._ID + "=?", args);

    }
}
