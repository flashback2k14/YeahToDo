package com.yeahdev.todoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.FirebaseError;
import com.yeahdev.todoapp.R;
import com.yeahdev.todoapp.helper.Constants;
import com.yeahdev.todoapp.helper.FirebaseWrapper;
import com.yeahdev.todoapp.helper.Util;


public class DeletedTodoActivity extends AppCompatActivity {

    private ListView lvDeletedToDos;
    private ArrayAdapter<String> lvDeletedAdapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_todo);

        initToolbar();
        initComponents();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString(Constants.USERID);
            if (userId != null) {
                setupAdapter();
                setupListener();
                setupFirebase();
            } else {
               goBack();
            }
        } else {
            goBack();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initComponents() {
        lvDeletedToDos = (ListView) findViewById(R.id.lvDeletedToDos);
        lvDeletedToDos.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvDeletedAdapter = new ArrayAdapter<>(this, R.layout.listview_item);
    }

    private void setupAdapter() {
        lvDeletedToDos.setAdapter(lvDeletedAdapter);
    }

    private void setupListener() {
        lvDeletedToDos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) lvDeletedToDos.getItemAtPosition(position);
                Util.buildConfirmDialog(DeletedTodoActivity.this, lvDeletedToDos, Constants.BASEURL, Constants.DELETEDROUTE, Constants.ROUTE, userId, item);
            }
        });

        lvDeletedToDos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                String item = (String) lvDeletedToDos.getItemAtPosition(position);
                Util.buildConfirmDialogOnlyRemove(DeletedTodoActivity.this, lvDeletedToDos, Constants.BASEURL, Constants.DELETEDROUTE, userId, item);
                return true;
            }
        });
    }

    private void setupFirebase() {
        FirebaseWrapper.loadData(Constants.BASEURL, Constants.DELETEDROUTE, userId, new FirebaseWrapper.OnLoadListener() {
            @Override
            public void onAdded(String item) {
                lvDeletedAdapter.add(item);
            }

            @Override
            public void onRemoved(String item) {
                lvDeletedAdapter.remove(item);
            }

            @Override
            public void onCanceled(FirebaseError error) {
                Util.buildSnackbar(lvDeletedToDos, error.getMessage());
            }
        });
    }

    private void goBack() {
        Toast.makeText(DeletedTodoActivity.this, "User not logged in!", Toast.LENGTH_LONG).show();
        startActivity(new Intent(DeletedTodoActivity.this, MainActivity.class));
    }
}
