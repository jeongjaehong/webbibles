package org.nilriri.webbibles.dao;

import java.util.HashMap;

import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.Constants.Bibles;
import org.nilriri.webbibles.dao.Constants.Notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import android.widget.ListView;

public class BibleDao extends AbstractDao {

    private Context mContext;

    public BibleDao(Context context, CursorFactory factory, boolean sdcarduse) {

        super(context, factory, sdcarduse);
        mContext = context;
    }

    public void delete(int mVersion, Long id) {
        String sql = "DELETE FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " WHERE " + Bibles._ID + "=" + id;

        getWritableDatabase().execSQL(sql);
    }

    public void delete(int mVersion, int book) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM " + Bibles.VERSION_TABLE_NAME[mVersion]);
        sql.append(" WHERE " + Bibles.VERSION + "=" + mVersion);
        sql.append(" AND " + Bibles.BOOK + "=" + book);

        getWritableDatabase().execSQL(sql.toString());
    }

    public void delete(int version, int book, int chapter) {
        StringBuffer query = new StringBuffer();

        query.append("DELETE FROM " + Bibles.VERSION_TABLE_NAME[version]);
        query.append(" WHERE " + Bibles.VERSION + "=" + version);
        query.append(" AND " + Bibles.BOOK + "=" + book);
        query.append(" AND " + Bibles.CHAPTER + "=" + chapter);

        getWritableDatabase().execSQL(query.toString());

    }

    public void deleteAll(int mVersion) {
        String sql = "DELETE FROM " + Bibles.VERSION_TABLE_NAME[mVersion];
        getWritableDatabase().execSQL(sql);

    }

    @SuppressWarnings("unchecked")
    public void insert(String versioncode, String versionname, int mVersion, int mBook, int mChapter, ListView contents) {

        ContentValues val = null;

        int limit = contents.getCount();

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        db.execSQL("DELETE FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " WHERE BOOK=" + mBook + " AND CHAPTER=" + mChapter + " ");

        for (int row = 0; row < limit; row++) {
            val = new ContentValues();
            val.put("vercode", versioncode);
            val.put("vername", versionname);
            val.put("version", mVersion);
            val.put("book", mBook);
            val.put("chapter", mChapter);

            Object obj = contents.getItemAtPosition(row);

            //Log.d("InternalDao-insert", "class type = " + obj.getClass().toString());

            if (obj.getClass().toString().indexOf("SQLiteCursor") >= 0) {

                Cursor c = (Cursor) obj;
                val.put("verse", c.getInt(6) == 0 ? null : c.getInt(6));
                val.put("contents", c.getString(7));

            } else {
                HashMap<String, String> map = (HashMap<String, String>) obj;
                val.put("verse", map.get("Number"));
                val.put("contents", map.get("Contents"));
            }

            //Log.d("InternalDao-insert", "val=" + val.toString());

            db.insert(Bibles.VERSION_TABLE_NAME[mVersion], null, val);
        }

        db.setTransactionSuccessful();

        db.endTransaction();

        //String msg = getContext().getResources().getString(R.string.save_successful);
        //Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();

    }

    public void insert(String versioncode, String versionname, int mVersion, int mBook, int mChapter, String[] contents) {

        ContentValues val = null;

        //SQLiteDatabase db = getWritableDatabase();

        //db.beginTransaction();

        //db.execSQL("DELETE FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " WHERE BOOK=" + mBook + " AND CHAPTER=" + mChapter + " ");
        getWritableDatabase().execSQL("DELETE FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " WHERE BOOK=" + mBook + " AND CHAPTER=" + mChapter + " ");

        for (int row = 0; row < contents.length; row++) {
            val = new ContentValues();
            val.put("vercode", versioncode);
            val.put("vername", versionname);
            val.put("version", mVersion);
            val.put("book", mBook);
            val.put("chapter", mChapter);

            String z[] = Common.tokenFn(contents[row], "##");

            if ("".equals(z[0].trim()) || z == null)
                continue;

            if (z.length == 1) {
                val.put("verse", 0);
                val.put("contents", z[0]);
            } else if (z.length == 2) {
                val.put("verse", z[0]);
                val.put("contents", z[1]);
            } else {
                continue;
            }

            //db.insert(Bibles.VERSION_TABLE_NAME[mVersion], null, val);
            getWritableDatabase().insert(Bibles.VERSION_TABLE_NAME[mVersion], null, val);
        }

        //db.setTransactionSuccessful();

        //db.endTransaction();

    }

    public void backup(Cursor cursor) {

        ContentValues val = null;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        boolean isFirst = true;

        while (cursor.moveToNext()) {

            if (isFirst) {
                StringBuffer query = new StringBuffer();
                query.append("DELETE FROM " + Bibles.VERSION_TABLE_NAME[cursor.getInt(Bibles.COL_VERSION)]);
                query.append(" WHERE " + Bibles.VERSION + "=" + cursor.getInt(Bibles.COL_VERSION));
                query.append(" AND " + Bibles.BOOK + "=" + cursor.getInt(Bibles.COL_BOOK));
                query.append(" AND " + Bibles.CHAPTER + "=" + cursor.getInt(Bibles.COL_CHAPTER));
                db.execSQL(query.toString());
            }

            val = new ContentValues();
            val.put("vercode", cursor.getString(Bibles.COL_VERCODE));
            val.put("vername", cursor.getString(Bibles.COL_VERNAME));
            val.put("version", cursor.getInt(Bibles.COL_VERSION));
            val.put("book", cursor.getInt(Bibles.COL_BOOK));
            val.put("chapter", cursor.getInt(Bibles.COL_CHAPTER));
            val.put("verse", cursor.getInt(Bibles.COL_VERSE));
            val.put("contents", cursor.getString(Bibles.COL_CONTENTS));

            db.insert(Bibles.VERSION_TABLE_NAME[cursor.getInt(Bibles.COL_VERSION)], null, val);

            isFirst = false;
        }

        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public boolean queryExistsContents(int mVersion, int mBook, int mChapter) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        boolean result = false;

        query.append("SELECT  ");
        query.append("    COUNT(*) CNT  ");
        query.append("FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " ");
        query.append("WHERE " + Bibles.VERSION + " = ? ");
        query.append("AND " + Bibles.BOOK + " = ? ");
        query.append("AND " + Bibles.CHAPTER + " = ? ");

        String selectionArgs[] = new String[] { mVersion + "", mBook + "", mChapter + "" };

        Log.d("InternalDao-queryExistsContents", "query=" + query.toString());

        Cursor cursor = db.rawQuery(query.toString(), selectionArgs);

        if (cursor.moveToFirst()) {
            result = (cursor.getInt(0) > 0);
        }
        cursor.close();
        return result;

    }

    public Cursor queryRandomFavorites() {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT T.* FROM ( ");
        //query.append(" SELECT (ABS(RANDOM()) % 9) NO,  ");
        query.append(" SELECT ABS(RANDOM()) NO,  ");
        query.append(" A.VERSESTR,  ");
        query.append(" A.CONTENTS,  ");
        query.append(" A.VERSION,  ");
        query.append(" A.BOOK,  ");
        query.append(" A.CHAPTER,  ");
        query.append(" A.VERSE ");
        query.append(" FROM FAVORITES A ");
        query.append(" ) T ");
        query.append(" ORDER BY T.NO ASC ");
        query.append(" LIMIT 1 ");

        Log.d("InternalDao-queryRandomFavorites", "query = " + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryBibleVerse(int p_version, int p_book, int p_chapter, int p_verse) {

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

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryBibleVerse(int p_version, int p_book, int p_chapter, String p_from, String p_to) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append(" " + Bibles._ID);
        query.append(" ," + Bibles.VERNAME);
        query.append(" ," + Bibles.CONTENTS);
        query.append(" FROM " + Bibles.VERSION_TABLE_NAME[p_version] + " ");
        query.append(" WHERE " + Bibles.VERSION + " = " + p_version);
        query.append(" AND " + Bibles.BOOK + " = " + p_book);
        query.append(" AND " + Bibles.CHAPTER + " = " + p_chapter);
        query.append(" AND cast(" + Bibles.VERSE + " as integer) between " + p_from + " and " + p_to);

        Log.d("InternalDao-queryBibleVerse", "query = " + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryOriginalContents(int mVersion, int mBook, int mChapter) {

        //SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append("SELECT  ");
        query.append("  _id, vercode, vername, version, book, chapter, verse, contents  ");
        query.append("FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " ");
        query.append("WHERE " + Bibles.VERSION + " = ? ");
        query.append("AND " + Bibles.BOOK + " = ? ");
        query.append("AND " + Bibles.CHAPTER + " = ? ");
        query.append("ORDER BY _ID ASC ");

        String selectionArgs[] = new String[] { mVersion + "", mBook + "", mChapter + "" };

        //Log.d("InternalDao-queryExistsSchedule", "query=" + query.toString());

        //Cursor cursor = 

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryContents(int mVersion, int mBook, int mChapter, int mVersion2) {
        if (mVersion != mVersion2 && mVersion2 >= 0 && Prefs.getCompare(mContext)) {
            return queryCompareContents(mVersion, mBook, mChapter, mVersion2);
        } else {
            return queryContents(mVersion, mBook, mChapter);

        }

    }

    public Cursor queryVerseList(int mVersion, int mBook, int mChapter) {

        //SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append("SELECT  ");
        query.append("  distinct a.verse as _id, a.verse + 1 as verse   ");
        query.append("FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " a ");
        query.append("WHERE a." + Bibles.VERSION + " = ? ");
        query.append("AND a." + Bibles.BOOK + " = ? ");
        query.append("AND a." + Bibles.CHAPTER + " = ? ");

        String selectionArgs[] = new String[] { mVersion + "", mBook + "", mChapter + "" };

        //Log.d("InternalDao-queryExistsSchedule", "query=" + query.toString());

        //Cursor cursor = 

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryContents(int mVersion, int mBook, int mChapter) {

        //SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append("SELECT  ");
        query.append("  a._id, a.vercode, a.vername, a.version, a.book, a.chapter, (case when a.verse > 0 then a.verse else null end) verse  ");
        query.append("  , a.contents|| (select case when count(*) > 0 then ' ★' else '' end from biblenote n where n.book = a.book and n.chapter = a.chapter and n.verse = cast(a.verse as integer)) as contents ");
        query.append("FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " a ");
        query.append("WHERE a." + Bibles.VERSION + " = ? ");
        query.append("AND a." + Bibles.BOOK + " = ? ");
        query.append("AND a." + Bibles.CHAPTER + " = ? ");

        String selectionArgs[] = new String[] { mVersion + "", mBook + "", mChapter + "" };

        //Log.d("InternalDao-queryExistsSchedule", "query=" + query.toString());

        //Cursor cursor = 

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryCompareContents(int mVersion, int mBook, int mChapter, int mVersion2) {

        //SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  a._id as _id, a.vercode, a.vername, a.version, a.book, a.chapter, (case when a.verse > 0 then a.verse else null end) verse  ");
        query.append("  , a.contents|| (select case when count(*) > 0 then ' ★' else '' end from biblenote n where n.book = a.book and n.chapter = a.chapter and n.verse = cast(a.verse as integer)) as contents ");
        query.append("  , cast(a.verse as integer) as sort ");
        query.append(" FROM " + Bibles.VERSION_TABLE_NAME[mVersion] + " a ");
        query.append(" WHERE a." + Bibles.VERSION + " = " + mVersion);
        query.append(" AND a." + Bibles.BOOK + " = " + mBook);
        query.append(" AND a." + Bibles.CHAPTER + " = " + mChapter);
        query.append(" AND cast(a." + Bibles.VERSE + " as integer) > 0 ");
        query.append(" UNION ALL  ");
        query.append(" SELECT  ");
        query.append("  a._id, a.vercode, a.vername, a.version, a.book, a.chapter, null verse  ");
        query.append("  , a.contents|| (select case when count(*) > 0 then ' ★' else '' end from biblenote n where n.book = a.book and n.chapter = a.chapter and n.verse = cast(a.verse as integer)) as contents ");
        query.append("  , cast(a.verse as integer) as sort ");
        query.append(" FROM " + Bibles.VERSION_TABLE_NAME[mVersion2] + " a ");
        query.append(" WHERE a." + Bibles.VERSION + " = " + mVersion2);
        query.append(" AND a." + Bibles.BOOK + " = " + mBook);
        query.append(" AND a." + Bibles.CHAPTER + " = " + mChapter);
        query.append(" AND cast(a." + Bibles.VERSE + " as integer) > 0 ");
        query.append(" ORDER BY 9 ");

        //String selectionArgs[] = new String[] { mVersion + "", mBook + "", mChapter + "", mVersion2 + "", mBook + "", mChapter + "" };

        Log.d("InternalDao-queryExistsSchedule", "query=" + query.toString());

        //Cursor cursor = 

        //return getReadableDatabase().rawQuery(query.toString(), selectionArgs);
        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor querySearchSummary(int searchVersion, int searchTestment, int searchOperator, String searchKeyword1, String searchKeyword2) {

        //SQLiteDatabase db = getReadableDatabase();

        Log.d("InternalDao-querySearchContents", "searchVersion=" + searchVersion);
        StringBuffer query = new StringBuffer();

        if (searchTestment >= 1) {
            query.append("SELECT  ");
            query.append("  count(distinct c.book) ||'권의 성경 '|| count(distinct c.book || '-' || c.chapter) || '개의 구절 찾음.' result ");
            query.append("FROM " + Bibles.VERSION_TABLE_NAME[searchVersion] + " c ");
        } else {
            query.append("SELECT  ");
            query.append("  '전체 ' || count(*) ||'개의 자료찾음.' result ");
            query.append("FROM " + Notes.NOTE_TABLE_NAME + " c ");
        }

        query.append("WHERE 1 = 1 ");

        switch (searchTestment) {
            case -1: // null
            case 0: // note
                break;
            case 1: // subject
                query.append("  AND cast(c." + Bibles.VERSE + " as integer) <= 0 ");
                break;
            case 2: // all
                query.append("  AND c." + Bibles.BOOK + " >= 0 ");
                break;
            case 3: // old
                query.append("  AND c." + Bibles.BOOK + " < 39 ");
                query.append("  AND cast(c." + Bibles.VERSE + " integer) > 0 ");
                break;
            case 4: // new
                query.append("  AND c." + Bibles.BOOK + " >= 39 ");
                break;
            default: // single testment
                query.append("  AND c." + Bibles.BOOK + " = " + (searchTestment - 5));
                break;
        }

        switch (searchOperator) {
            case 0: // and
                if (!"".equals(searchKeyword1))
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                if (!"".equals(searchKeyword2))
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword2 + "%'");
                break;
            case 1: // or
                if (!"".equals(searchKeyword1) && !"".equals(searchKeyword2)) {
                    query.append("  AND (c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  OR c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword2 + "%')");
                } else if (!"".equals(searchKeyword1) && "".equals(searchKeyword2)) {
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                }
                break;
            case 2: // not
                if (!"".equals(searchKeyword1) && !"".equals(searchKeyword2)) {
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  AND c." + Bibles.CONTENTS + " NOT LIKE " + "'%" + searchKeyword2 + "%'");
                } else if (!"".equals(searchKeyword1) && "".equals(searchKeyword2)) {
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                }
                break;

        }

        Log.d("InternalDao-querySearchContents", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor querySearchContents(int searchVersion, int searchTestment, int searchOperator, String searchKeyword1, String searchKeyword2, int totalcount) {

        //SQLiteDatabase db = getReadableDatabase();

        Log.d("InternalDao-querySearchContents", "searchVersion=" + searchVersion);
        StringBuffer query = new StringBuffer();

        query.append("SELECT  ");
        query.append("  c._id, c.vercode, c.vername, c.version, c.book, c.chapter, c.verse verse, ");
        query.append("  (case when c.verse > 0 then '['||b.biblename || ' ' || (c.chapter + 1) || ':' || c.verse||'] ' else '['||b.biblename || ' ' || (c.chapter + 1) || '] ' end)  ");
        //query.append("  || c.contents as contents, '' empty ");
       query.append("  || replace(replace(c.contents, '"+searchKeyword1+"', '<"+searchKeyword1+">'), '"+searchKeyword2+"', '<"+searchKeyword2+">') as contents, '' empty ");

        if (searchTestment >= 1) {
            query.append("FROM " + Bibles.VERSION_TABLE_NAME[searchVersion] + " c ");
        } else {
            query.append("FROM " + Notes.NOTE_TABLE_NAME + " c ");
            //note..
        }

        query.append("    INNER JOIN biblenames b on c.book = b._id ");
        query.append("WHERE 1 = 1 ");

        switch (searchTestment) {
            case -1: // null
            case 0: // note
                break;
            case 1: // subject
                query.append("  AND cast(c." + Bibles.VERSE + " as integer) <= 0 ");
                break;
            case 2: // all
                query.append("  AND c." + Bibles.BOOK + " >= 0 ");
                break;
            case 3: // old
                query.append("  AND c." + Bibles.BOOK + " < 39 ");
                query.append("  AND cast(c." + Bibles.VERSE + " integer) > 0 ");
                break;
            case 4: // new
                query.append("  AND c." + Bibles.BOOK + " >= 39 ");
                break;
            default: // single testment
                query.append("  AND c." + Bibles.BOOK + " = " + (searchTestment - 5));
                break;
        }

        switch (searchOperator) {
            case 0: // and
                if (!"".equals(searchKeyword1))
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                if (!"".equals(searchKeyword2))
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword2 + "%'");
                break;
            case 1: // or
                if (!"".equals(searchKeyword1) && !"".equals(searchKeyword2)) {
                    query.append("  AND (c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  OR c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword2 + "%')");
                } else if (!"".equals(searchKeyword1) && "".equals(searchKeyword2)) {
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                }
                break;
            case 2: // not
                if (!"".equals(searchKeyword1) && !"".equals(searchKeyword2)) {
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                    query.append("  AND c." + Bibles.CONTENTS + " NOT LIKE " + "'%" + searchKeyword2 + "%'");
                } else if (!"".equals(searchKeyword1) && "".equals(searchKeyword2)) {
                    query.append("  AND c." + Bibles.CONTENTS + " LIKE " + "'%" + searchKeyword1 + "%'");
                }
                break;

        }

        query.append(" ORDER BY 1 ");
        query.append(" limit 500  ");

        Log.d("InternalDao-querySearchContents", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public void update(BibleBean bibleBean) {

        String[] args = new String[] { bibleBean._id + "" };
        ContentValues val = new ContentValues();

        val.put(Bibles._ID, bibleBean._id);
        val.put(Bibles.VERCODE, bibleBean.vercode);
        val.put(Bibles.VERNAME, bibleBean.vername);
        val.put(Bibles.VERSION, bibleBean.version);
        val.put(Bibles.BOOK, bibleBean.book);
        val.put(Bibles.CHAPTER, bibleBean.chapter);
        val.put(Bibles.VERSE, bibleBean.verse);
        val.put(Bibles.CONTENTS, bibleBean.contents);

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        db.update(Bibles.VERSION_TABLE_NAME[bibleBean.version], val, Bibles._ID + "=?", args);

        db.setTransactionSuccessful();
        db.endTransaction();

    }
}
