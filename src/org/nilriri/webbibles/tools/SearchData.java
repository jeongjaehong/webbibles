package org.nilriri.webbibles.tools;

import org.nilriri.webbibles.NotesList;
import org.nilriri.webbibles.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;

public class SearchData extends Activity implements OnTouchListener, OnClickListener {

    private static final String TAG = "SearchData";

    private static final String SEARCH_VERSION = "search_version";
    private static final String SEARCH_TARGET = "search_target";
    private static final String SEARCH_KEYWORD1 = "search_keyword1";
    private static final String SEARCH_KEYWORD2 = "search_keyword2";
    private static final String SEARCH_OPERATOR = "search_operator";

    private Spinner spin_version;
    private Spinner spin_testment;
    private Spinner spin_operator;

    private EditText search_keyword1;
    private EditText search_keyword2;

    // Menu item ids    
    public static final int MENU_ITEM_SAVE = Menu.FIRST;
    public static final int MENU_ITEM_PREV = Menu.FIRST + 1;
    public static final int MENU_ITEM_NEXT = Menu.FIRST + 2;
    public static final int MENU_LOAD_WEB = Menu.FIRST + 3;
    public static final int MENU_LOAD_DB = Menu.FIRST + 4;
    public static final int MENU_ITEM_NOTELIST = Menu.FIRST + 5;
    public static final int MENU_ITEM_ADDMARK = Menu.FIRST + 6;
    public static final int MENU_ITEM_MARKLIST = Menu.FIRST + 7;
    public static final int MENU_ITEM_ADDNOTE = Menu.FIRST + 8;
    public static final int MENU_ITEM_SENDSMS = Menu.FIRST + 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        setContentView(R.layout.search_box);

        spin_version = (Spinner) findViewById(R.id.spin_searchversion);
        ArrayAdapter<CharSequence> adapter_version = ArrayAdapter.createFromResource(this, R.array.site1versionkor, android.R.layout.simple_spinner_item);
        adapter_version.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_version.setAdapter(adapter_version);

        spin_testment = (Spinner) findViewById(R.id.spin_searchtestment);
        ArrayAdapter<CharSequence> adapter_testment = ArrayAdapter.createFromResource(this, R.array.search_testment, android.R.layout.simple_spinner_item);
        adapter_testment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_testment.setAdapter(adapter_testment);

        spin_operator = (Spinner) findViewById(R.id.spin_searchop);
        ArrayAdapter<CharSequence> adapter_operation = ArrayAdapter.createFromResource(this, R.array.search_operation, android.R.layout.simple_spinner_item);
        adapter_operation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_operator.setAdapter(adapter_operation);

        search_keyword1 = (EditText) findViewById(R.id.keyword1);
        search_keyword2 = (EditText) findViewById(R.id.keyword2);

        findViewById(R.id.btn_search).setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferences(MODE_PRIVATE).edit().putInt(SEARCH_VERSION, ((Spinner) findViewById(R.id.spin_searchversion)).getSelectedItemPosition()).commit();
        getPreferences(MODE_PRIVATE).edit().putInt(SEARCH_TARGET, ((Spinner) findViewById(R.id.spin_searchtestment)).getSelectedItemPosition()).commit();
        getPreferences(MODE_PRIVATE).edit().putString(SEARCH_KEYWORD1, ((EditText) findViewById(R.id.keyword1)).getText().toString()).commit();
        getPreferences(MODE_PRIVATE).edit().putString(SEARCH_KEYWORD2, ((EditText) findViewById(R.id.keyword2)).getText().toString()).commit();
        getPreferences(MODE_PRIVATE).edit().putInt(SEARCH_OPERATOR, ((Spinner) findViewById(R.id.spin_searchop)).getSelectedItemPosition()).commit();
    }

    protected void onResume() {
        super.onResume();
        ((Spinner) findViewById(R.id.spin_searchversion)).setSelection(getPreferences(MODE_PRIVATE).getInt(SEARCH_VERSION, 0));
        ((Spinner) findViewById(R.id.spin_searchtestment)).setSelection(getPreferences(MODE_PRIVATE).getInt(SEARCH_TARGET, 0));        
        ((EditText) findViewById(R.id.keyword1)).setText(getPreferences(MODE_PRIVATE).getString(SEARCH_KEYWORD1, ""));
        ((EditText) findViewById(R.id.keyword2)).setText(getPreferences(MODE_PRIVATE).getString(SEARCH_KEYWORD2, ""));
        ((Spinner) findViewById(R.id.spin_searchop)).setSelection(getPreferences(MODE_PRIVATE).getInt(SEARCH_OPERATOR, 0));
    }

    public class listOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

            if (pos < 0)
                return;

            Log.e(TAG, "onItemSelected====" + id);

        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub

        switch (view.getId()) {
            case R.id.btn_search:
                Intent intent = new Intent();

                int target = spin_testment.getSelectedItemPosition();

                switch (target) {
                    case 0: // note
                        intent.setClass(this, NotesList.class);

                        intent.putExtra("bibleid", new Long(-1));
                        intent.putExtra("version", spin_version.getSelectedItemPosition());
                        intent.putExtra("book", -1);
                        intent.putExtra("chapter", -1);
                        intent.putExtra("verse", -1);
                        intent.putExtra("SEARCH", "Y");

                        break;
                    case 1: // alltestment
                    case 2: // oldtestment
                    case 3: // newtestment
                    case 4: // singletestment
                        intent.setClass(this, BibleSearchList.class);
                        break;
                    default: // singletestment
                        intent.setClass(this, BibleSearchList.class);

                        break;
                }

                intent.putExtra("version", spin_version.getSelectedItemPosition());
                intent.putExtra("testment", spin_testment.getSelectedItemPosition());
                intent.putExtra("operator", spin_operator.getSelectedItemPosition());
                intent.putExtra("keyword1", search_keyword1.getText().toString().trim());
                intent.putExtra("keyword2", search_keyword2.getText().toString().trim());
                startActivity(intent);

                break;

        }
    }

}
