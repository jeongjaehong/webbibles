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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Song extends Activity implements OnClickListener {
    private final HttpClient Client = new DefaultHttpClient();
    private SongsDao dao;
    TextView txtview;
    TextView edt_songid;

    public static final int MENU_AUTO_LOAD = Menu.FIRST;

    String mTitleUrl[] = { "http://bible.c3tv.com/hymn/hymn_player_new.asp?hymn_idx=", "http://bible.c3tv.com/hymn/hymn_player.asp?hymn_idx=" };
    String mContentsUrl[] = { "http://bible.c3tv.com/hymn/hymn_text_new.asp?hymn_idx=", "http://bible.c3tv.com/hymn/hymn_text.asp?hymn_idx=" };
    Button btn_goto;
    Button btn_prev;
    Button btn_next;
    Button btn_img;
    ScrollView imgview;
    ImageView songview;
    int mVersion;
    int songid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.song_view);

        txtview = (TextView) findViewById(R.id.song_text);
        songview = (ImageView) findViewById(R.id.songview);
        imgview = (ScrollView) findViewById(R.id.songimg);
        btn_goto = (Button) findViewById(R.id.btn_goto);
        btn_goto.setOnClickListener(this);

        btn_prev = (Button) findViewById(R.id.btn_prev);
        btn_prev.setOnClickListener(this);

        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);

        btn_img = (Button) findViewById(R.id.btn_img);
        btn_img.setOnClickListener(this);

        imgview.setOnClickListener(this);

        Intent i = getIntent();
        //mUrl = i.getStringExtra("url");
        mVersion = i.getIntExtra("version", 0);
        songid = i.getIntExtra("songid", 0);

        edt_songid = (TextView) findViewById(R.id.song_id);

        Spinner spin_version = (Spinner) findViewById(R.id.songversion);
        spin_version.setOnItemSelectedListener(new VersionSelectedListener());

        this.setTitle(this.getResources().getString(R.string.song_title) + " " + songid + "厘");
        loadContents();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        String songtext = edt_songid.getText().toString();
        songtext = songtext == null || "".equals(songtext) ? songid + "" : songtext;
        songid = Integer.parseInt(songtext);

        switch (v.getId()) {
            case R.id.btn_goto:

                this.setTitle(this.getResources().getString(R.string.song_title) + " " + songid + "厘");
                edt_songid.setText(songid + "");
                loadContents();
                break;
            case R.id.btn_prev:

                songid = songid > 1 ? songid - 1 : songid;
                this.setTitle(this.getResources().getString(R.string.song_title) + " " + songid + "厘");
                edt_songid.setText(songid + "");
                loadContents();
                break;
            case R.id.btn_next:
                if (mVersion == 0) {
                    songid = songid < 645 ? songid + 1 : songid - 1;
                } else {
                    songid = songid < 558 ? songid + 1 : songid - 1;
                }
                this.setTitle(this.getResources().getString(R.string.song_title) + " " + songid + "厘");
                edt_songid.setText(songid + "");
                loadContents();
                break;
            case R.id.btn_img:

                if (txtview.getVisibility() == View.VISIBLE) {
                    txtview.setVisibility(View.GONE);
                    imgview.setVisibility(View.VISIBLE);

                    //  File SDCardRoot = Environment.getExternalStorageDirectory();
                    //  Environment.getDataDirectory()

                    // Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");
                    // Log.d("xxx", "content:/" + Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");
                    // songview.setImageBitmap(bm);

                    //Uri uri = Uri.parse("content:/" + Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");

                    //imgview.setImageURI(uri);

                } else {
                    txtview.setVisibility(View.VISIBLE);
                    imgview.setVisibility(View.GONE);
                }
                break;

            case R.id.songimg:

                //txtview.setVisibility(View.VISIBLE);
                //imgview.setVisibility(View.GONE);
                break;

        }
        if (imgview.getVisibility() == View.VISIBLE) {
            String imageid = songid + "";
            imageid = imageid.length() == 1 ? "00" + imageid : imageid;
            imageid = imageid.length() == 2 ? "0" + imageid : imageid;

            Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");
            // Log.d("xxx", "content:/" + Environment.getExternalStorageDirectory() + "/NHYMN/" + imageid + ".gif");
            songview.setImageBitmap(bm);
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

        songid = mVersion == 1 && songid > 558 ? 558 : songid;
        songid = mVersion == 0 && songid > 645 ? 645 : songid;

        String imageid = songid + "";
        imageid = imageid.length() == 1 ? "00" + imageid : imageid;
        imageid = imageid.length() == 2 ? "0" + imageid : imageid;

        dao = new SongsDao(this, null, Prefs.getSDCardUse(this));
        Cursor c = dao.querySongsText(mVersion, songid);
        TextView songtitle = (TextView) findViewById(R.id.song_title);

        if (!c.moveToNext()) {

            loadImage(imageid);

            HttpGet httpget = new HttpGet(mTitleUrl[mVersion] + songid);
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

                String song_title = res.substring(res.indexOf("[林力]") - 1);
                song_title = song_title.substring(0, song_title.indexOf("[啊荤]") - 1);
                song_title = song_title.replace("[力格]", "\n[力格]");
                song_title = song_title.replace("  ", "");

                songtitle.setText(song_title.trim());

                httpget = new HttpGet(mContentsUrl[mVersion] + songid);

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

                dao.insert(mVersion, songid, res, song_title.split("\n")[0], song_title.split("\n")[1], "");

                c = dao.querySongsText(mVersion, songid);

                if (c.moveToNext()) {
                    txtview.setText(c.getString(Songs.COL_SONGTEXT));
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

            txtview.setText(c.getString(Songs.COL_SONGTEXT));
            songtitle.setText(c.getString(Songs.COL_SUBJECT) + "\n " + c.getString(Songs.COL_TITLE));
        }

    }

    private void loadImage(String imageid) {

        try {

            File SDCardRoot = Environment.getExternalStorageDirectory();
            //create a new file, specifying the path, and the filename
            //which we want to save the file as.
            File file = new File(SDCardRoot, "NHYMN/" + imageid + ".gif");

            if (!file.exists()) {

                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                URL url = new URL("http://m.holybible.or.kr/NHYMN/HYMN_SCR/" + imageid + ".gif");

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
                file = new File(SDCardRoot, "NHYMN");

                if (!file.exists())
                    file.mkdir();

                file = new File(SDCardRoot, "NHYMN/" + imageid + ".gif");

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
            //catch some possible errors...
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item4 = menu.add(0, MENU_AUTO_LOAD, 0, R.string.menu_auto_load);
        item4.setIcon(R.drawable.ic_menu_refresh);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    ProgressDialog mDialog;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_AUTO_LOAD: {

                new AutoDown().execute("");
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private class AutoDown extends AsyncTask<String, Void, Void> {
        private final HttpClient autoClient = new DefaultHttpClient();
        private SongsDao autoDao;

        //private String autoContent;
        //private int autoTargetVersion;
        private int max;
        private String downError = null;
        private ProgressDialog Dialog = new ProgressDialog(Song.this);

        protected void onPreExecute() {
            Dialog.setMessage("Download Contents..");
            max = mVersion == 0 ? 645 : 558;

            Dialog.setIndeterminate(true);
            Dialog.setCancelable(true);
            Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            Dialog.setMax(max);
            Dialog.show();
        }

        protected Void doInBackground(String... urls) {

            autoDao = new SongsDao(getBaseContext(), null, Prefs.getSDCardUse(getBaseContext()));

            for (songid = 1; songid <= max; songid++) {
                Dialog.setProgress(songid);
                songid = mVersion == 1 && songid > 558 ? 558 : songid;
                songid = mVersion == 0 && songid > 645 ? 645 : songid;

                String imageid = songid + "";
                imageid = imageid.length() == 1 ? "00" + imageid : imageid;
                imageid = imageid.length() == 2 ? "0" + imageid : imageid;

                Cursor c = autoDao.querySongsText(mVersion, songid);

                if (!c.moveToNext()) {
                    try {
                        File SDCardRoot = Environment.getExternalStorageDirectory();
                        //create a new file, specifying the path, and the filename
                        //which we want to save the file as.
                        File file = new File(SDCardRoot, "NHYMN/" + imageid + ".gif");

                        if (!file.exists()) {

                            //set the download URL, a url that points to a file on the internet
                            //this is the file to be downloaded
                            URL url = new URL("http://m.holybible.or.kr/NHYMN/HYMN_SCR/" + imageid + ".gif");

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
                            file = new File(SDCardRoot, "NHYMN");

                            if (!file.exists())
                                file.mkdir();

                            file = new File(SDCardRoot, "NHYMN/" + imageid + ".gif");

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

                        HttpGet httpget = new HttpGet(mTitleUrl[mVersion] + songid);
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

                        String song_title = res.substring(res.indexOf("[林力]") - 1);
                        song_title = song_title.substring(0, song_title.indexOf("[啊荤]") - 1);
                        song_title = song_title.replace("[力格]", "\n[力格]");
                        song_title = song_title.replace("  ", "");

                        httpget = new HttpGet(mContentsUrl[mVersion] + songid);

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

                        autoDao.insert(mVersion, songid, res, song_title.split("\n")[0], song_title.split("\n")[1], "");

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

            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            Dialog.dismiss();
            Toast.makeText(Song.this, downError, Toast.LENGTH_SHORT).show();

        }

    }

}
