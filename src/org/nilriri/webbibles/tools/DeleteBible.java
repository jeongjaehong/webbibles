package org.nilriri.webbibles.tools;

import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BibleDao;
import org.nilriri.webbibles.dao.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DeleteBible extends Activity implements OnClickListener {
    private static final String TAG = "DataManager";

    //private static final int FROM_DATE_DIALOG_ID = 1;
    //private static final int TO_DATE_DIALOG_ID = 2;

    // Menu item ids
    public static final int MENU_ITEM_DELNOTE = Menu.FIRST;
    public static final int MENU_ITEM_ADDNOTE = Menu.FIRST + 1;
    public static final int MENU_ITEM_ALLLIST = Menu.FIRST + 2;
    public static final int MENU_ITEM_BOOKLIST = Menu.FIRST + 3;
    public static final int MENU_ITEM_BIBLEVIEW = Menu.FIRST + 4;
    public static final int MENU_ITEM_EDITNOTE = Menu.FIRST + 5;
    public static final int MENU_ITEM_DAYSLIST = Menu.FIRST + 6;

    public static final int LOAD_ALL = 0;
    public static final int LOAD_BOOK = 1;
    public static final int LOAD_CHAPTER = 2;
    public static final int LOAD_DAYS = 3;

   
    private String Error = "";

    private ListView mListView = null;
    private int mVersion = -1;

    private int mWorkMode = -1;

    private BibleDao daoTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.datamanager_target);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mVersion = getIntent().getIntExtra("VERSION", 0);
        mWorkMode = getIntent().getIntExtra("WORK_MODE", Constants.DATAMANAGE_BACKUP);

        if (Constants.DATAMANAGE_DELINTERNAL == mWorkMode) {
            this.setTitle(getResources().getString(R.string.title_deletebible));
            daoTarget = new BibleDao(DeleteBible.this, null, false);
        } else if (Constants.DATAMANAGE_DELEXTERNAL == mWorkMode) {
            this.setTitle(getResources().getString(R.string.title_deletebible));
            daoTarget = new BibleDao(DeleteBible.this, null, true);
        }
        mListView = (ListView) findViewById(R.id.ContentsListView);

        mListView.setOnCreateContextMenuListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.all_testment, android.R.layout.simple_list_item_multiple_choice);
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

                StartDelete();
                break;

        }
    }

    private ProgressDialog pd;

    public void StartDelete() {

        pd = new ProgressDialog(this);
        pd.setTitle("Delete!");
        pd.setMessage("Delete from internal storage...");
        pd.setCancelable(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();

        Thread thread = new Thread(new Runnable() {

            public void run() {

                try {
                    int prog = 0;
                    pd.setMax(mListView.getCheckedItemPositions().size());
                    for (int book = 0; book < mListView.getCount(); book++) {
                        if (mListView.isItemChecked(book)) {

                            prog++;
                            //복사되는 장별로 진행상태표시
                            //Dialog.setMax(chapter.length);
                            pd.setProgress(prog);
                            daoTarget.delete(mVersion, book);

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
                Toast.makeText(DeleteBible.this, Error, Toast.LENGTH_LONG).show();
            }

        }

    };

}
