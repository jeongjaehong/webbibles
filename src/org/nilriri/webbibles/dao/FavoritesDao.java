package org.nilriri.webbibles.dao;

import org.nilriri.webbibles.dao.Constants.FavoriteGroup;
import org.nilriri.webbibles.dao.Constants.Favorites;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class FavoritesDao extends AbstractDao {

    public FavoritesDao(Context context, CursorFactory factory, boolean sdcarduse) {
        super(context, factory, sdcarduse);
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
        query.append(" ," + FavoriteGroup.GROUPNM);
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
        query.append(" ," + FavoriteGroup.GROUPNM);
        query.append(" FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ");

        query.append(" ORDER BY " + FavoriteGroup.GROUPNM + " ASC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryFavoritesGroup(int oldGroup) {

        StringBuffer query = new StringBuffer();

        if (oldGroup <= 0) {
            query.append(" SELECT  ");
            query.append("  0 " + FavoriteGroup._ID);
            query.append(" ,' Create New Group...' " + FavoriteGroup.GROUPNM);
            query.append(" union all  ");
        }
        query.append(" SELECT  ");
        query.append("  " + FavoriteGroup._ID);
        query.append(" ," + FavoriteGroup.GROUPNM);
        query.append(" FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ");
        if (oldGroup > 0) {
            query.append(" WHERE " + FavoriteGroup._ID + " <> " + oldGroup);
        }
        query.append(" ORDER BY " + FavoriteGroup.GROUPNM + " ASC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public void deleteFavoritesGroup(int oldGroup) {

        StringBuffer query = new StringBuffer();

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        //기존 그룹의 자료를 다른자료로 변경한다.
        query.append(" UPDATE " + Favorites.FAVORITES_TABLE_NAME + " ");
        query.append(" SET " + Favorites.COL_GROUPKEY + " = 0 ");
        query.append(" WHERE " + Favorites.COL_GROUPKEY + " = " + oldGroup);

        db.execSQL(query.toString());

        //그룹코드를 삭제한다.
        query.append(" DELETE FROM " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " ");
        query.append(" WHERE " + FavoriteGroup._ID + " = " + oldGroup);

        db.execSQL(query.toString());

        db.setTransactionSuccessful();

        db.endTransaction();

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

    public Cursor queryFavoritesList(Long group) {

        StringBuffer query = new StringBuffer();

        query.append(" SELECT  ");
        query.append("  f." + Favorites._ID);
        query.append(" ,f." + Favorites.BIBLEID);
        query.append(" ,f." + Favorites.GROUPKEY);
        query.append(" ,'' " + Favorites.GROUPNM);
        query.append(" ,f." + Favorites.VERSESTR);
        query.append(" ,f." + Favorites.VERSION);
        query.append(" ,f." + Favorites.BOOK);
        query.append(" ,f." + Favorites.CHAPTER);
        query.append(" ,f." + Favorites.VERSE);
        query.append(" ,'[' || (select g." + FavoriteGroup.GROUPNM + " from " + FavoriteGroup.FAVORITEGROUP_TABLE_NAME + " g where g." + FavoriteGroup._ID + " = f." + Favorites.GROUPKEY + ") ");
        query.append(" || ',' || f." + Favorites.VERSESTR + "||'] ' || f." + Favorites.CONTENTS + " " + Favorites.CONTENTS);
        query.append(" FROM " + Favorites.FAVORITES_TABLE_NAME + " f ");
        if (group > 0) {
            query.append(" WHERE " + Favorites.GROUPKEY + " = " + group);
        }

        query.append(" ORDER BY f." + Favorites._ID + " ASC ");

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

}
