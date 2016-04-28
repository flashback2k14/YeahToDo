package com.yeahdev.todoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.firebase.client.FirebaseError;
import com.yeahdev.todoapp.R;
import com.yeahdev.todoapp.helper.Constants;
import com.yeahdev.todoapp.helper.FirebaseWrapper;
import com.yeahdev.todoapp.helper.Util;


public class MainActivity extends AppCompatActivity {
    private EditText etToDo;
    private ImageButton btnClearInput;
    private ListView lvToDos;
    private ArrayAdapter<String> lvAdapter;
    private FloatingActionButton fabAddToDo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        initCompontents();
        setupListener();
        setupAdapter();
        setupFirebase();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarTop);
        setSupportActionBar(toolbar);
    }

    private void initCompontents() {
        etToDo = (EditText) findViewById(R.id.etAddToDo);
        btnClearInput = (ImageButton) findViewById(R.id.btnClearInput);
        lvToDos = (ListView) findViewById(R.id.lvToDos);
        lvAdapter = new ArrayAdapter<>(this, R.layout.listview_item);
        fabAddToDo = (FloatingActionButton) findViewById(R.id.fabAddToDo);
    }

    private void setupListener() {
        btnClearInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etToDo.getText().toString())) {
                    etToDo.setText("");
                } else {
                    Util.buildSnackbar(v, "No Input to clear!");
                }
            }
        });
        fabAddToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String item  = etToDo.getText().toString();
                if (!TextUtils.isEmpty(item)) {
                    FirebaseWrapper.addItem(Constants.BASEURL, Constants.ROUTE, item, new FirebaseWrapper.OnChangedListener() {
                        @Override
                        public void onSuccess(String item) {
                            Util.buildSnackbar(lvToDos, item);
                            etToDo.setText("");
                        }
                        @Override
                        public void onFailed(FirebaseError error) {
                            Util.buildSnackbar(lvToDos, error.getMessage());
                        }
                    });
                } else {
                    Util.buildSnackbar(view, "No Todo added!");
                }
            }
        });
        lvToDos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                String item = (String) lvToDos.getItemAtPosition(position);
                Util.buildConfirmDialog(MainActivity.this, lvToDos, Constants.BASEURL, Constants.ROUTE, Constants.DELETEDROUTE, item);
                return true;
            }
        });
        lvToDos.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    fabAddToDo.hide();
                } else {
                    fabAddToDo.show();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void setupAdapter() {
        lvToDos.setAdapter(lvAdapter);
    }

    private void setupFirebase() {
        FirebaseWrapper.loadData(Constants.BASEURL, Constants.ROUTE, new FirebaseWrapper.OnLoadListener() {
            @Override
            public void onAdded(String item) {
                lvAdapter.add(item);
            }
            @Override
            public void onRemoved(String item) {
                lvAdapter.remove(item);
            }
            @Override
            public void onCanceled(FirebaseError error) {
                Util.buildSnackbar(lvToDos, error.getMessage());
            }
        });

        setTheme(R.style.AppTheme);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_report:
                startActivity(new Intent(MainActivity.this, ReportActivity.class));
                return true;
            case R.id.action_deleted:
                startActivity(new Intent(MainActivity.this, DeletedTodoActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
