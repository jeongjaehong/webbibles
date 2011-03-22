package org.nilriri.webbibles.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class InternalStorage extends SQLiteOpenHelper implements StorageSelector {

    private Context mContext;

    public InternalStorage(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }
  
    @Override
    public Context getContext() {
        // TODO Auto-generated method stub
        return mContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        new DaoCreator().onCreate(getContext(), db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        
        new DaoCreator().onUpgrade(this.mContext, db, oldVersion, newVersion);
        
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        // TODO Auto-generated method stub
        return super.getReadableDatabase();
      
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        // TODO Auto-generated method stub
        return super.getWritableDatabase();
    }


}
