package org.nilriri.webbibles.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.nilriri.webbibles.dao.Constants.FavoriteGroup;
import org.nilriri.webbibles.dao.Constants.Favorites;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class FavoritesDao extends AbstractDao {
    Context mContext;

    public FavoritesDao(Context context, CursorFactory factory, boolean sdcarduse) {
        super(context, factory, sdcarduse);
        mContext = context;
    }

    public void deleteFavorite(Long id) {

        SQLiteDatabase db = getWritableDatabase();

        String whereClause = Favorites._ID + "=?";
        String whereArgs[] = new String[] { id.toString() };

        db.delete(Favorites.FAVORITES_TABLE_NAME, whereClause, whereArgs);

    }

    public void insertFavorites(Long id, String versestr, String contents, int version, int book, int chapter, int verse) {

        ContentValues values = new ContentValues();

        values.put(Favorites.GROUPKEY, id);

        values.put(Favorites.VERSESTR, versestr);
        values.put(Favorites.CONTENTS, contents);
        values.put(Favorites.VERSION, version);
        values.put(Favorites.BOOK, book);
        values.put(Favorites.CHAPTER, chapter);
        values.put(Favorites.VERSE, verse);

        Log.d("InternalDao-insert", "insert values = " + values.toString());

        getWritableDatabase().insert(Favorites.FAVORITES_TABLE_NAME, null, values);

    }

    public Cursor queryFavoritesGroup(Long mID) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  " + FavoriteGroup._ID);
        //query.append(" ," + FavoriteGroup.GROUPNM);
        query.append(" ,ifnull(" + FavoriteGroup.GROUPNM + ", '--No Title--') " + FavoriteGroup.GROUPNM);
        query.append(" FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ");
        query.append(" WHERE " + FavoriteGroup._ID + " = " + mID.toString());

        query.append(" ORDER BY " + FavoriteGroup.GROUPNM + " ASC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryFavoriteGroupList() {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  0 " + FavoriteGroup._ID);
        query.append(" ,' All Groups...' " + FavoriteGroup.GROUPNM);
        query.append(" union all  ");

        query.append(" SELECT  ");
        query.append("  " + FavoriteGroup._ID);
        query.append(" ,ifnull(" + FavoriteGroup.GROUPNM + ", '--No Title--') " + FavoriteGroup.GROUPNM);
        //query.append(" ," + FavoriteGroup.GROUPNM);
        query.append(" FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ");

        query.append(" ORDER BY " + FavoriteGroup.GROUPNM + " ASC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryFavNotExistsGroup(int oldGroup) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  0 " + FavoriteGroup._ID);
        query.append(" ,' Create New Group...' " + FavoriteGroup.GROUPNM);
        query.append(" union all  ");
        query.append(" SELECT  ");
        query.append("  " + FavoriteGroup._ID);
        query.append(" ,ifnull(" + FavoriteGroup.GROUPNM + ", '--No Title--') " + FavoriteGroup.GROUPNM);
        query.append(" FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ");
        if (oldGroup > 0) {
            query.append(" WHERE " + FavoriteGroup._ID + " <> " + oldGroup);
        }
        query.append(" ORDER BY " + FavoriteGroup.GROUPNM + " ASC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public void deleteFavoritesGroup(int oldGroup) {

        StringBuffer query = new StringBuffer();

        //int groupkey = -1;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {

            //기존 그룹의 자료를 다른자료로 변경한다.
            query.append(" UPDATE " + Favorites.FAVORITES_TABLE_NAME + " ");
            query.append(" SET " + Favorites.GROUPKEY + " = IFNULL((SELECT MIN(" + FavoriteGroup._ID + ") FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ),-1) ");
            query.append(" WHERE " + Favorites.GROUPKEY + " = " + oldGroup);

            db.execSQL(query.toString());

            //그룹코드를 삭제한다.
            query = new StringBuffer();
            query.append(" DELETE FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ");
            query.append(" WHERE " + FavoriteGroup._ID + " = " + oldGroup);

            db.execSQL(query.toString());

            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {

        }

        db.close();

    }

    public Long insertFavoriteGroup(String groupnm) {

        ContentValues values = new ContentValues();

        values.put(FavoriteGroup.GROUPNM, groupnm);

        return getWritableDatabase().insert(FavoriteGroup.FAVORITEGROUP_TABLE_NAME, null, values);

    }

    public void updateFavoriteGroup(ContentValues values) {

        String[] args = new String[] { values.get(FavoriteGroup._ID).toString() };
        getWritableDatabase().update(FavoriteGroup.FAVORITEGROUP_TABLE_NAME, values, FavoriteGroup._ID + "=?", args);

    }

    public void changeFavoriteGroup(Long DestID, int group) {

        StringBuffer query = new StringBuffer();

        query.append(" update " + Favorites.FAVORITES_TABLE_NAME);
        query.append(" set " + Favorites.GROUPKEY + " = " + group);
        query.append(" where " + Favorites._ID + " = " + DestID.toString());

        getWritableDatabase().execSQL(query.toString());

    }

    public Cursor queryFavoritesList(Long groupID) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  f." + Favorites._ID);
        query.append(" ,f." + Favorites.BIBLEID);
        query.append(" ,f." + Favorites.GROUPKEY);
        query.append(" ,f." + Favorites.VERSESTR);
        query.append(" ,f." + Favorites.VERSION);
        query.append(" ,f." + Favorites.BOOK);
        query.append(" ,f." + Favorites.CHAPTER);
        query.append(" ,f." + Favorites.VERSE);
        //query.append(" ,'[' || ifnull((select g." + FavoriteGroup.GROUPNM + " from " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " g where g." + FavoriteGroup._ID + " = f." + Favorites.GROUPKEY + "),'Group미정') ");
        query.append(" ,f." + Favorites.CONTENTS + " " + Favorites.CONTENTS);
        query.append(" ,'[' || ifnull((select g." + FavoriteGroup.GROUPNM + " from " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " g where g." + FavoriteGroup._ID + " = f." + Favorites.GROUPKEY + "),'Group미정') ");
        query.append(" || '] ' " + Favorites.GROUPNM);
        query.append(" FROM " + Favorites.FAVORITES_TABLE_NAME + " f ");
        if (groupID > 0) {
            query.append(" WHERE " + Favorites.GROUPKEY + " = " + groupID);
        }

        query.append(" ORDER BY f." + Favorites._ID + " ASC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryAll(long p_groupkey) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  f.* ");
        query.append(" FROM " + Favorites.FAVORITES_TABLE_NAME + " f ");
        query.append(" WHERE 1=1 ");
        if (p_groupkey >= 0) {
            query.append(" AND f." + Favorites.GROUPKEY + " = " + p_groupkey);
        }
        query.append(" ORDER BY f." + Favorites.GROUPKEY + ", " + " f." + Favorites._ID + "  ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public boolean exportdata(Cursor cursor, ProgressDialog pd) {
        String path = android.os.Environment.getExternalStorageDirectory().toString() + "/";
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);

        StringBuilder backupdate = new StringBuilder(c.get(Calendar.YEAR)).append(c.get(Calendar.MONTH) + 1).append(c.get(Calendar.DAY_OF_MONTH));

        File file = new File(path + "favorites" + backupdate + ".db");
        pd.setMax(cursor.getCount() + 10);
        if (file.exists()) {
            if (file.delete()) {
                file = new File(path + "favorites" + backupdate + ".db");
            } else {
                file = new File(path + "favorites" + backupdate + ".db");
                if (file.exists()) {
                    return false;
                }
            }
        }

        pd.setProgress(10);
        try {
            FileOutputStream fos = new FileOutputStream(file);

            StringBuilder buf = new StringBuilder(Favorites._ID);
            buf.append("\t" + Favorites.BIBLEID);
            buf.append("\t" + Favorites.BOOK);
            buf.append("\t" + Favorites.CHAPTER);
            buf.append("\t" + Favorites.CONTENTS);
            buf.append("\t" + Favorites.FAVORITES_TABLE_NAME);
            buf.append("\t" + Favorites.GROUPKEY);
            buf.append("\t" + Favorites.GROUPNM);
            buf.append("\t" + Favorites.VERSE);
            buf.append("\t" + Favorites.VERSESTR);
            buf.append("\t" + Favorites.VERSION);
            buf.append("\n\r");

            cursor.moveToFirst();
            while (cursor.getCount() > 0) {
                pd.setProgress(cursor.getPosition() + 10);
                for (int col = 0; col < cursor.getColumnCount(); col++) {
                    buf.append(cursor.getString(col)).append("\t");
                }
                buf.append("\n\r");
                if (!cursor.moveToNext())
                    break;
            }
            fos.write(buf.toString().getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            Log.e("Export", e.getMessage(), e);
            return false;
        }
    }

}
