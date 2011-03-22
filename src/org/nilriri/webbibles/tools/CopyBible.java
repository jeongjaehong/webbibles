package org.nilriri.webbibles.tools;

import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BibleDao;
import org.nilriri.webbibles.dao.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CopyBible extends Activity implements OnClickListener {
    private static final String TAG = "CopyBible";

    public static final int MENU_ITEM_DELNOTE = Menu.FIRST;

    private int mVersion = -1;
    private int mWorkMode = -1;
    private ListView mListView = null;

    private String Error = "";

    private BibleDao daoSource;
    private BibleDao daoTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.datamanager_target);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mVersion = getIntent().getIntExtra("VERSION", 0);
        mWorkMode = getIntent().getIntExtra("WORK_MODE", Constants.DATAMANAGE_BACKUP);

        if (Constants.DATAMANAGE_BACKUP == mWorkMode) {
            this.setTitle(getResources().getString(R.string.title_databackup));
            daoSource = new BibleDao(CopyBible.this, null, false);
            daoTarget = new BibleDao(CopyBible.this, null, true);
        } else if (Constants.DATAMANAGE_RESTORE == mWorkMode) {
            this.setTitle(getResources().getString(R.string.title_datarestore));
            daoSource = new BibleDao(CopyBible.this, null, true);
            daoTarget = new BibleDao(CopyBible.this, null, false);
        }

        mListView = (ListView) findViewById(R.id.ContentsListView);

        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.all_testment, R.layout.multiple_choice);
        mListView.setAdapter(adapter);

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        findViewById(R.id.btn_allcheck).setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        this.setResult(Activity.RESULT_OK);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public class listOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            Log.e(TAG, "onListItemClick position = " + pos);
            Log.e(TAG, "onListItemClick id = " + id);

        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.btn_allcheck:

                for (int i = 0; i < this.mListView.getCount(); i++) {
                    mListView.setItemChecked(i, !mListView.isItemChecked(i));
                }

                break;
            case R.id.btn_start:

                StartCopy();
                break;

        }
    }

    private ProgressDialog pd;

    public void StartCopy() {

        pd = new ProgressDialog(this);
        if (Constants.DATAMANAGE_BACKUP == mWorkMode) {
            pd.setTitle("Backup!");
            pd.setMessage("Backup to external storage...");
        } else {
            pd.setTitle("Restore!");
            pd.setMessage("Restore from external storage...");
        }
        pd.setCancelable(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //pd.setIndeterminate(true);
        pd.show();

        Thread thread = new Thread(new Runnable() {

            public void run() {

                try {

                    String chapterlist[] = getResources().getStringArray(R.array.chapterlist);
                    String chapter[] = null;
                    Cursor cursor = null;

                    int prog = 0;

                    pd.setMax(mListView.getCheckedItemPositions().size());

                    for (int book = 0; book < mListView.getCount(); book++) {
                        if (mListView.isItemChecked(book)) {

                            chapter = Common.tokenFn(chapterlist[book], ",");
                            prog++;
                            //복사되는 장별로 진행상태표시
                            //Dialog.setMax(chapter.length);
                            pd.setProgress(prog);

                            for (int c = 0; c < chapter.length; c++) {

                                //복사되는 장별로 진행상태표시
                                //Dialog.setProgress(c + 1);
                                pd.setSecondaryProgress(c);
                                cursor = daoSource.queryOriginalContents(mVersion, book, c);
                                if (cursor.getCount() > 0) {
                                    daoTarget.backup(cursor);
                                }

                                cursor.close();
                            }

                        }

                    }

                    handler.sendEmptyMessage(0);

                } catch (Exception e) {

                    Error = e.getMessage();

                }

            }

        });

        thread.start();

    }

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            pd.dismiss();

            for (int book = 0; book < mListView.getCount(); book++) {
                mListView.setSelection(book);
                mListView.setItemChecked(book, false);
            }
            mListView.invalidate();
            if (!"".equals(Error)) {
                Log.e(TAG, "Error = " + Error);
                Toast.makeText(CopyBible.this, Error, Toast.LENGTH_LONG).show();
            }

        }

    };

}
