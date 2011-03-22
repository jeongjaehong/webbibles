package org.nilriri.webbibles.tools;

import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class VersionSelector extends Activity {
    private static final String TAG = "VersionSelector";

    private ListView mListView = null;
    private int mWorkingMode = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.versionselector);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mListView = (ListView) findViewById(R.id.ContentsListView);

        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new listOnItemClickListener());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sitelist, android.R.layout.simple_list_item_1);
        mListView.setAdapter(adapter);

        mWorkingMode = getIntent().getIntExtra("DATAMANAGE_WORK", -1);

    }

    @Override
    protected void onResume() {
        super.onResume();

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

            Intent intent = new Intent();
            switch (mWorkingMode) {
                case Constants.DATAMANAGE_BACKUP:
                    intent.putExtra("WORK_MODE", mWorkingMode);
                    intent.setClass(getBaseContext(), CopyBible.class);
                    break;
                case Constants.DATAMANAGE_RESTORE:
                    intent.putExtra("WORK_MODE", mWorkingMode);
                    intent.setClass(getBaseContext(), CopyBible.class);
                    break;
                case Constants.DATAMANAGE_DELINTERNAL:
                    intent.putExtra("WORK_MODE", mWorkingMode);
                    intent.setClass(getBaseContext(), DeleteBible.class);
                    break;
                case Constants.DATAMANAGE_DELEXTERNAL:
                    intent.putExtra("WORK_MODE", mWorkingMode);
                    intent.setClass(getBaseContext(), DeleteBible.class);
                    break;
                case Constants.DATAMANAGE_DOWNLOAD:
                    intent.setClass(getBaseContext(), DownloadBible.class);
                    break;
                default:
                    return;

            }
            intent.putExtra("VERSION", pos);
            startActivity(intent);

            finish();

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
            this.finish();
    }

}
