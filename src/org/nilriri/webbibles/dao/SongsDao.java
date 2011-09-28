package org.nilriri.webbibles.dao;

import org.nilriri.webbibles.dao.Constants.Songs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SongsDao extends AbstractDao {

    public SongsDao(Context context, CursorFactory factory, boolean sdcarduse) {
        super(context, factory, sdcarduse);
    }

    public void delete(Long id) {

        String whereClause = Songs._ID + "=?";
        String whereArgs[] = new String[] { id.toString() };

        getWritableDatabase().delete(Songs.SONGS_TABLE_NAME, whereClause, whereArgs);

    }

    public void insert(int version, int songid, String songtext, String subject, String title, String imgurl) {

        ContentValues values = new ContentValues();

        values.put(Songs.VERSION, version);
        values.put(Songs.SONGID, songid);
        values.put(Songs.SONGTEXT, songtext);
        values.put(Songs.SUBJECT, subject);
        values.put(Songs.TITLE, title);
        values.put(Songs.IMGURL, imgurl);

        SQLiteDatabase db = getWritableDatabase();

        StringBuffer query = new StringBuffer();
        query.append(" SELECT COUNT(*) ");
        query.append(" FROM " + Songs.SONGS_TABLE_NAME + " ");
        query.append(" WHERE " + Songs.VERSION + " = " + version);
        query.append(" AND " + Songs.SONGID + " = " + songid);

        Cursor c = db.rawQuery(query.toString(), null);

        int cnt = 0;
        if (c.moveToNext()) {
            cnt = c.getInt(0);
        }

        if (cnt > 0) {
            StringBuffer where = new StringBuffer();
            where.append(" WHERE " + Songs.VERSION + " = " + version);
            where.append(" AND " + Songs.SONGID + " = " + songid);

            db.update(Songs.SONGS_TABLE_NAME, values, where.toString(), null);
        } else {
            db.insert(Songs.SONGS_TABLE_NAME, null, values);
        }

    }

    public Cursor querySongsText(int version, int songid) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  " + Songs._ID);
        query.append(" ," + Songs.VERSION);
        query.append(" ," + Songs.SONGID);
        query.append(" ," + Songs.SONGTEXT);
        query.append(" ," + Songs.SUBJECT);
        query.append(" ," + Songs.TITLE);

        query.append(" FROM " + Songs.SONGS_TABLE_NAME + " ");
        query.append(" WHERE " + Songs.VERSION + " = " + version);
        query.append(" AND " + Songs.SONGID + " = " + songid);

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor querySubjectList(int version) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT 0 _id, '' subject ");
        query.append(" UNION ALL ");
        query.append(" SELECT ");
        query.append(" MIN(" + Songs._ID + ") _id ");
        query.append(" ,replace(" + Songs.SUBJECT + ", '[林力]', '') " + Songs.SUBJECT);
        query.append(" FROM " + Songs.SONGS_TABLE_NAME + " ");
        query.append(" WHERE " + Songs.VERSION + " = " + version);
        query.append(" GROUP BY " + Songs.SUBJECT + "  ");

        query.append(" ORDER BY 1 ");

        Cursor cursor = db.rawQuery(query.toString(), null);

        return cursor;

    }

    public Cursor querySongsList(int version, String subject) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT DISTINCT ");
        query.append("  MAX(" + Songs._ID + ") " + Songs._ID);
        query.append(" ," + Songs.VERSION);
        query.append(" ," + Songs.SONGID);
        query.append(" ,MAX(substr(replace(" + Songs.SONGTEXT + ", '\n', ' '), 1, 30)) " + Songs.SONGTEXT);
        query.append(" ,MAX(replace(" + Songs.SUBJECT + ",'[林力]','')) " + Songs.SUBJECT);
        query.append(" ,MAX(replace(" + Songs.TITLE + ",'[力格]','')) " + Songs.TITLE);
        query.append(" FROM " + Songs.SONGS_TABLE_NAME + " ");
        query.append(" WHERE " + Songs.VERSION + " = " + version);
        if (!"".equals(subject)) {
            query.append(" AND " + Songs.SUBJECT + " = '" + subject + "'");
        }
        query.append(" GROUP BY " + Songs.VERSION + ", " + Songs.SONGID + " ");
        query.append(" ORDER BY " + Songs.VERSION + "," + Songs.SONGID + "  ");

        Cursor cursor = db.rawQuery(query.toString(), null);

        return cursor;

    }

}
