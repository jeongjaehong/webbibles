package org.nilriri.webbibles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.SongsDao;
import org.nilriri.webbibles.dao.Constants.Songs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Song extends Activity implements OnClickListener {
    private final HttpClient Client = new DefaultHttpClient();
    private SongsDao dao;
    private TextView song_text;
    private LinearLayout mSongtext;
    private TextView edt_songid;
    private Spinner spin_version;

    public static final int MENU_AUTO_LOAD = Menu.FIRST;
    public static final int MENU_AUTO_RELOAD = Menu.FIRST + 1;
    public static final int MENU_ITEM_OPENVIEW = Menu.FIRST + 2;
    public static final int MENU_ITEM_LISTVIEW = Menu.FIRST + 3;

    String mTitleUrl[] = { "http://bible.c3tv.com/hymn/hymn_player_new.asp?hymn_idx=", "http://bible.c3tv.com/hymn/hymn_player.asp?hymn_idx=" };
    String mContentsUrl[] = { "http://bible.c3tv.com/hymn/hymn_text_new.asp?hymn_idx=", "http://bible.c3tv.com/hymn/hymn_text.asp?hymn_idx=" };
    Button btn_goto;
    Button btn_prev;
    Button btn_next;
    Button btn_img;
    ScrollView songimg;
    ImageView songview;
    int mVersion;
    int mSongid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.song_view);

        mSongtext = (LinearLayout) findViewById(R.id.songtext);
        song_text = (TextView) findViewById(R.id.song_text);

        songview = (ImageView) findViewById(R.id.songview);
        songimg = (ScrollView) findViewById(R.id.songimg);
        btn_goto = (Button) findViewById(R.id.btn_goto);
        btn_goto.setOnClickListener(this);

        btn_prev = (Button) findViewById(R.id.btn_prev);
        btn_prev.setOnClickListener(this);

        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);

        btn_img = (Button) findViewById(R.id.btn_img);
        btn_img.setOnClickListener(this);

        songimg.setOnClickListener(this);

        songview.setOnCreateContextMenuListener(this);

        Intent i = getIntent();
        //mUrl = i.getStringExtra("url");
        mVersion = i.getIntExtra("version", 0);
        mSongid = i.getIntExtra("mSongid", 0);

        edt_songid = (TextView) findViewById(R.id.song_id);

        spin_version = (Spinner) findViewById(R.id.songversion);
        spin_version.setOnItemSelectedListener(new VersionSelectedListener());

        this.setTitle(this.getResources().getString(R.string.song_title) + " " + mSongid + "장");

        spin_version.requestFocus();

    }

    @Override
    protected void onResume() {
        super.onResume();

        dao = new SongsDao(this, null, Prefs.getSDCardUse(this));
        loadContents();

        spin_version.requestFocus();

    }

    @Override
    protected void onPause() {

        super.onPause();

        if (dao != null)
            dao.CloseDatabase();

    }

    @Override
    public void onClick(View v) {
        String songtext = edt_songid.getText().toString();
        songtext = songtext == null || "".equals(songtext) ? mSongid + "" : songtext;
        try {
            mSongid = Integer.parseInt(songtext);
        } catch (Exception e) {
            mSongid = 1;
        }

        switch (v.getId()) {
            case R.id.btn_goto:
                

                Intent intent = new Intent();

                intent.setClass(getBaseContext(), SongList.class);

                //intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_text_new.asp?hymn_idx=");
                //intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_player_new.asp?hymn_idx=");
                intent.putExtra("version", mVersion);
                intent.putExtra("mSongid", mSongid);

                startActivity(intent);

                //this.setTitle(this.getResources().getString(R.string.song_title) + " " + mSongid + "장");
                //edt_songid.setText(mSongid + "");
                //loadContents();
                
                this.finish();
                
                break;
            case R.id.btn_prev:

                mSongid = mSongid > 1 ? mSongid - 1 : mSongid;
                this.setTitle(this.getResources().getString(R.string.song_title) + " " + mSongid + "장");
                edt_songid.setText(mSongid + "");
                loadContents();
                break;
            case R.id.btn_next:
                if (mVersion == 0) {
                    mSongid = mSongid < 645 ? mSongid + 1 : mSongid - 1;
                } else {
                    mSongid = mSongid < 558 ? mSongid + 1 : mSongid - 1;
                }
                this.setTitle(this.getResources().getString(R.string.song_title) + " " + mSongid + "장");
                edt_songid.setText(mSongid + "");
                loadContents();
                break;
            case R.id.btn_img:

                if (mSongtext.getVisibility() == View.VISIBLE) {
                    mSongtext.setVisibility(View.GONE);
                    songimg.setVisibility(View.VISIBLE);

                    //  File SDCardRoot = Environment.getExternalStorageDirectory();
                    //  Environment.getDataDirectory()

                    // Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");
                    // Log.d("xxx", "content:/" + Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");
                    // songview.setImageBitmap(bm);

                    //Uri uri = Uri.parse("content:/" + Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");

                    //songimg.setImageURI(uri);

                } else {
                    mSongtext.setVisibility(View.VISIBLE);
                    songimg.setVisibility(View.GONE);
                }
                break;

            case R.id.songimg:

                //song_text.setVisibility(View.VISIBLE);
                //songimg.setVisibility(View.GONE);
                break;

        }
        if (songimg.getVisibility() == View.VISIBLE) {
            String imageid = mSongid + "";
            imageid = imageid.length() == 1 ? "00" + imageid : imageid;
            imageid = imageid.length() == 2 ? "0" + imageid : imageid;

            File SDCardRoot = Environment.getExternalStorageDirectory();
            File file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

            Log.d("WebBibles", "content:/" + Environment.getExternalStorageDirectory() + "/" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");
                if (bm == null) {
                    file.delete();
                    this.loadImage(imageid);
                } else if (bm.getHeight() == 0) {
                    file.delete();
                    this.loadImage(imageid);
                } else {
                    songview.setImageBitmap(bm);
                }
            } else {
                this.loadImage(imageid);
            }
        }

    }

    public class VersionSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            //BIBLE_DB_NAME = "";
            //BIBLE_TABLE_NAME = "";

            // getPreferences(MODE_PRIVATE).edit().putInt(BIBLE_VERSION, ((Spinner) findViewById(R.id.spinbible)).getSelectedItemPosition()).commit();
            mVersion = pos;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
            mVersion = 0;
        }
    }

    private void loadContents() {

        mSongid = mVersion == 1 && mSongid > 558 ? 558 : mSongid;
        mSongid = mVersion == 0 && mSongid > 645 ? 645 : mSongid;

        String imageid = mSongid + "";
        imageid = imageid.length() == 1 ? "00" + imageid : imageid;
        imageid = imageid.length() == 2 ? "0" + imageid : imageid;

        Cursor c = dao.querySongsText(mVersion, mSongid);
        TextView songtitle = (TextView) findViewById(R.id.song_title);

        if (!c.moveToNext()) {

            loadImage(imageid);

            HttpGet httpget = new HttpGet(mTitleUrl[mVersion] + mSongid);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            try {

                String HTMLSource = Client.execute(httpget, responseHandler);

                HTMLSource = HTMLSource.substring(HTMLSource.indexOf("<body") - 1);

                HTMLSource = HTMLSource.replace("<br>", "##").replace("\t", " ");

                Matcher matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher("");

                StringBuffer result = new StringBuffer();
                matcher.reset(HTMLSource);
                while (matcher.find()) {
                    matcher.appendReplacement(result, "");
                }
                matcher.appendTail(result);

                HTMLSource = result.toString().trim();
                ///////////////////////////////////////
                matcher = Pattern.compile("[\\r|\\n|\\r\\n]").matcher("");

                matcher.reset(HTMLSource);

                result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, "");
                }
                matcher.appendTail(result);

                HTMLSource = result.toString().replace("##", "\n");

                String res = new String(HTMLSource.getBytes("8859_1"), "KSC5601");

                String song_title = res.substring(res.indexOf("[주제]") - 1);
                song_title = song_title.substring(0, song_title.indexOf("[가사]") - 1);
                song_title = song_title.replace("[제목]", "\n[제목]");
                song_title = song_title.replace("  ", "");

                songtitle.setText(song_title.trim());

                httpget = new HttpGet(mContentsUrl[mVersion] + mSongid);

                HTMLSource = Client.execute(httpget, responseHandler);

                HTMLSource = HTMLSource.substring(HTMLSource.indexOf("<body") - 1);

                HTMLSource = HTMLSource.replace("<br>", "##").replace("\t", " ");

                matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher("");

                result = new StringBuffer();
                matcher.reset(HTMLSource);
                while (matcher.find()) {
                    matcher.appendReplacement(result, "");
                }
                matcher.appendTail(result);

                HTMLSource = result.toString().trim();
                ///////////////////////////////////////
                matcher = Pattern.compile("[\\r|\\n|\\r\\n]").matcher("");

                matcher.reset(HTMLSource);

                result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, "");
                }
                matcher.appendTail(result);

                HTMLSource = result.toString().replace("##", "\n");

                //String res = new String(HTMLSource.getBytes("iso-8859-1"));
                //String res = new String(HTMLSource.getBytes("euc-kr"));
                //String res = new String(HTMLSource.getBytes("utf-8"));
                //String res = new String(HTMLSource.getBytes("euc-kr"), "UTF-8");
                //String res = new String(HTMLSource.getBytes("8859_1"), "UTF-8");
                res = new String(HTMLSource.getBytes("8859_1"), "KSC5601");
                ////////////////////////////////////////

                dao.insert(mVersion, mSongid, res, song_title.split("\n")[0], song_title.split("\n")[1], "");

                c = dao.querySongsText(mVersion, mSongid);

                if (c.moveToNext()) {
                    song_text.setText(c.getString(Songs.COL_SONGTEXT));
                    songtitle.setText(c.getString(Songs.COL_SUBJECT) + "\n " + c.getString(Songs.COL_TITLE));
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {

            song_text.setText(c.getString(Songs.COL_SONGTEXT));
            songtitle.setText(c.getString(Songs.COL_SUBJECT) + "\n " + c.getString(Songs.COL_TITLE));
        }

    }

    private void loadImage(String imageid) {

        try {

            songview.setImageDrawable(this.getResources().getDrawable(android.R.drawable.ic_popup_sync));

            File SDCardRoot = Environment.getExternalStorageDirectory();
            //create a new file, specifying the path, and the filename
            //which we want to save the file as.
            File file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

            if (!file.exists()) {

                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                URL url = new URL("http://m.holybible.or.kr/" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/HYMN_SCR/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

                //create the new connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //set up some things on the connection
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);

                //and connect!
                urlConnection.connect();

                //set the path where we want to save the file
                //in this case, going to save it on the root directory of the
                //sd card.
                SDCardRoot = Environment.getExternalStorageDirectory();
                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "");

                if (!file.exists())
                    file.mkdir();

                file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = new FileOutputStream(file);

                //this will be used in reading the data from the internet
                InputStream inputStream = urlConnection.getInputStream();

                //this is the total size of the file
                //int totalSize = urlConnection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;

                //create a buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer

                //now, read through the input buffer and write the contents to the file
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //this is where you would do something to report the prgress, like this maybe
                    //updateProgress(downloadedSize, totalSize);

                }
                //close the output stream when done
                fileOutput.close();

            }

            file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");
                // Log.d("xxx", "content:/" + Environment.getExternalStorageDirectory() + "/"+(mVersion==0?"NHYMN":"HYMN")+"/" + imageid + ""+(mVersion==0?".gif":".jpg")+"");
                songview.setImageBitmap(bm);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("찬송가 악보").setHeaderIcon(R.drawable.app_notes).add(0, MENU_ITEM_OPENVIEW, 0, "이미지 뷰어로보기");

        MenuItem item4 = menu.add(0, MENU_AUTO_LOAD, 0, "전체 다운로드");
        item4.setIcon(android.R.drawable.ic_popup_sync);

        MenuItem item5 = menu.add(0, MENU_AUTO_RELOAD, 0, "현재 악보 다시받기");
        item5.setIcon(android.R.drawable.ic_menu_save);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item4 = menu.add(0, MENU_AUTO_LOAD, 0, "전체악보받기");
        item4.setIcon(android.R.drawable.ic_popup_sync);

        MenuItem item5 = menu.add(0, MENU_AUTO_RELOAD, 0, "현재악보다시받기");
        item5.setIcon(android.R.drawable.ic_menu_save);

        MenuItem item6 = menu.add(0, MENU_ITEM_OPENVIEW, 0, "확대보기");
        item6.setIcon(android.R.drawable.ic_menu_slideshow);

        MenuItem item7 = menu.add(0, MENU_ITEM_LISTVIEW, 0, "목록보기");
        item7.setIcon(android.R.drawable.ic_menu_agenda);

        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        menuItemSelected(item);
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    public boolean menuItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_AUTO_LOAD: {
                new AutoDown().execute("");
                return true;
            }
            case MENU_ITEM_OPENVIEW: {
                String imageid = mSongid + "";
                imageid = imageid.length() == 1 ? "00" + imageid : imageid;
                imageid = imageid.length() == 2 ? "0" + imageid : imageid;

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

                Log.d("WebBibles", "Path=" + imgFile.getAbsolutePath());

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + imgFile.getAbsolutePath()), "image/*");
                startActivity(intent);

                return true;
            }
            case MENU_ITEM_LISTVIEW: {

                Intent intent = new Intent();

                intent.setClass(getBaseContext(), SongList.class);

                //intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_text_new.asp?hymn_idx=");
                //intent.putExtra("url", "http://bible.c3tv.com/hymn/hymn_player_new.asp?hymn_idx=");
                intent.putExtra("version", mVersion);
                intent.putExtra("mSongid", mSongid);

                startActivity(intent);

                return true;
            }
            case MENU_AUTO_RELOAD: {
                String imageid = mSongid + "";
                imageid = imageid.length() == 1 ? "00" + imageid : imageid;
                imageid = imageid.length() == 2 ? "0" + imageid : imageid;

                this.loadImage(imageid);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class AutoDown extends AsyncTask<String, Void, Void> {
        private final HttpClient autoClient = new DefaultHttpClient();
        private SongsDao autoDao;

        private int max;
        private String downError = null;
        private ProgressDialog Dialog = new ProgressDialog(Song.this);

        protected void onPreExecute() {
            Dialog.setMessage("Download Contents..");
            max = mVersion == 0 ? 645 : 558;

            //Dialog.setIndeterminate(true);
            Dialog.setCancelable(true);
            Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            Dialog.setMax(max);
            Dialog.show();
        }

        protected Void doInBackground(String... urls) {

            autoDao = new SongsDao(getBaseContext(), null, Prefs.getSDCardUse(getBaseContext()));

            for (mSongid = 1; mSongid <= max; mSongid++) {
                Dialog.setProgress(mSongid);
                mSongid = mVersion == 1 && mSongid > 558 ? 558 : mSongid;
                mSongid = mVersion == 0 && mSongid > 645 ? 645 : mSongid;

                String imageid = mSongid + "";
                imageid = imageid.length() == 1 ? "00" + imageid : imageid;
                imageid = imageid.length() == 2 ? "0" + imageid : imageid;

                Cursor c = autoDao.querySongsText(mVersion, mSongid);

                File SDCardRoot = Environment.getExternalStorageDirectory();
                File file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

                try {
                    if (!c.moveToNext() || !file.exists()) {
                        try {
                            //create a new file, specifying the path, and the filename
                            //which we want to save the file as.
                            //File file = new File(SDCardRoot, ""+(mVersion==0?"NHYMN":"HYMN")+"/" + imageid + ""+(mVersion==0?".gif":".jpg")+"");

                            if (!file.exists()) {

                                //set the download URL, a url that points to a file on the internet
                                //this is the file to be downloaded
                                URL url = new URL("http://m.holybible.or.kr/" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/HYMN_SCR/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

                                //create the new connection
                                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                                //set up some things on the connection
                                urlConnection.setRequestMethod("GET");
                                urlConnection.setDoOutput(true);

                                //and connect!
                                urlConnection.connect();

                                //set the path where we want to save the file
                                //in this case, going to save it on the root directory of the
                                //sd card.
                                SDCardRoot = Environment.getExternalStorageDirectory();
                                //create a new file, specifying the path, and the filename
                                //which we want to save the file as.
                                file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "");

                                if (!file.exists())
                                    file.mkdir();

                                file = new File(SDCardRoot, "" + (mVersion == 0 ? "NHYMN" : "HYMN") + "/" + imageid + "" + (mVersion == 0 ? ".gif" : ".jpg") + "");

                                //this will be used to write the downloaded data into the file we created
                                FileOutputStream fileOutput = new FileOutputStream(file);

                                //this will be used in reading the data from the internet
                                InputStream inputStream = urlConnection.getInputStream();

                                //this is the total size of the file
                                //int totalSize = urlConnection.getContentLength();
                                //variable to store total downloaded bytes
                                int downloadedSize = 0;

                                //create a buffer...
                                byte[] buffer = new byte[1024];
                                int bufferLength = 0; //used to store a temporary size of the buffer

                                //now, read through the input buffer and write the contents to the file
                                while ((bufferLength = inputStream.read(buffer)) > 0) {
                                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                                    fileOutput.write(buffer, 0, bufferLength);
                                    //add up the size so we know how much is downloaded
                                    downloadedSize += bufferLength;
                                    //this is where you would do something to report the prgress, like this maybe
                                    //updateProgress(downloadedSize, totalSize);

                                }
                                //close the output stream when done
                                fileOutput.close();
                            }

                            HttpGet httpget = new HttpGet(mTitleUrl[mVersion] + mSongid);
                            ResponseHandler<String> responseHandler = new BasicResponseHandler();

                            ///////////////////////////////

                            String HTMLSource = autoClient.execute(httpget, responseHandler);

                            HTMLSource = HTMLSource.substring(HTMLSource.indexOf("<body") - 1);

                            HTMLSource = HTMLSource.replace("<br>", "##").replace("\t", " ");

                            Matcher matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher("");

                            StringBuffer result = new StringBuffer();
                            matcher.reset(HTMLSource);
                            while (matcher.find()) {
                                matcher.appendReplacement(result, "");
                            }
                            matcher.appendTail(result);

                            HTMLSource = result.toString().trim();
                            ///////////////////////////////////////
                            matcher = Pattern.compile("[\\r|\\n|\\r\\n]").matcher("");

                            matcher.reset(HTMLSource);

                            result = new StringBuffer();
                            while (matcher.find()) {
                                matcher.appendReplacement(result, "");
                            }
                            matcher.appendTail(result);

                            HTMLSource = result.toString().replace("##", "\n");

                            String res = new String(HTMLSource.getBytes("8859_1"), "KSC5601");

                            String song_title = res.substring(res.indexOf("[주제]") - 1);
                            song_title = song_title.substring(0, song_title.indexOf("[가사]") - 1);
                            song_title = song_title.replace("[제목]", "\n[제목]");
                            song_title = song_title.replace("  ", "");

                            httpget = new HttpGet(mContentsUrl[mVersion] + mSongid);

                            HTMLSource = autoClient.execute(httpget, responseHandler);

                            HTMLSource = HTMLSource.substring(HTMLSource.indexOf("<body") - 1);

                            HTMLSource = HTMLSource.replace("<br>", "##").replace("\t", " ");

                            matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher("");

                            result = new StringBuffer();
                            matcher.reset(HTMLSource);
                            while (matcher.find()) {
                                matcher.appendReplacement(result, "");
                            }
                            matcher.appendTail(result);

                            HTMLSource = result.toString().trim();
                            ///////////////////////////////////////
                            matcher = Pattern.compile("[\\r|\\n|\\r\\n]").matcher("");

                            matcher.reset(HTMLSource);

                            result = new StringBuffer();
                            while (matcher.find()) {
                                matcher.appendReplacement(result, "");
                            }
                            matcher.appendTail(result);

                            HTMLSource = result.toString().replace("##", "\n");

                            //String res = new String(HTMLSource.getBytes("iso-8859-1"));
                            //String res = new String(HTMLSource.getBytes("euc-kr"));
                            //String res = new String(HTMLSource.getBytes("utf-8"));
                            //String res = new String(HTMLSource.getBytes("euc-kr"), "UTF-8");
                            //String res = new String(HTMLSource.getBytes("8859_1"), "UTF-8");
                            res = new String(HTMLSource.getBytes("8859_1"), "KSC5601");
                            ////////////////////////////////////////

                            autoDao.insert(mVersion, mSongid, res, song_title.split("\n")[0], song_title.split("\n")[1], "");

                        } catch (ClientProtocolException e) {
                            downError = e.getMessage();
                            cancel(true);
                        } catch (IOException e) {
                            downError = e.getMessage();
                            cancel(true);
                        }

                    }
                    c.close();
                    autoDao.CloseDatabase();
                } catch (Exception e) {

                }

            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            Dialog.dismiss();
            Toast.makeText(Song.this, downError, Toast.LENGTH_SHORT).show();

        }

    }

}
