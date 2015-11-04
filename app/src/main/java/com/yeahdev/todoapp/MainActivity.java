package com.yeahdev.todoapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String BASEURL = "https://todoapp-appengine.firebaseio.com/";
    private static final String ROUTE = "todoitems";
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

        setTheme(R.style.AppTheme);
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
                    buildSnackbar(v, "No Input to clear!");
                }
            }
        });
        fabAddToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (!TextUtils.isEmpty(etToDo.getText().toString())) {
                    new Firebase(BASEURL + ROUTE)
                        .push()
                        .child("text")
                        .setValue(etToDo.getText().toString(), new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    buildSnackbar(view, firebaseError.getMessage());
                                } else {
                                    buildSnackbar(view, "Todo: " + etToDo.getText().toString() + " added!");
                                    etToDo.setText("");
                                }
                            }
                        });
                } else {
                    buildSnackbar(view, "No Todo added!");
                }
            }
        });
        lvToDos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {

                new Firebase(BASEURL + ROUTE)
                    .orderByChild("text")
                    .equalTo((String) lvToDos.getItemAtPosition(position))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                final DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                firstChild.getRef().removeValue(new Firebase.CompletionListener() {
                                    @Override
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                        if (firebaseError != null) {
                                            buildSnackbar(lvToDos, firebaseError.getMessage());
                                        } else {
                                            buildSnackbar(lvToDos, "Todo: " + firstChild.getValue().toString() + " removed!");
                                        }
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });

                return true;
            }
        });
    }

    private void setupAdapter() {
        lvToDos.setAdapter(lvAdapter);
    }

    private void setupFirebase() {

        new Firebase(BASEURL + ROUTE)
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    lvAdapter.add((String) dataSnapshot.child("text").getValue());
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    lvAdapter.remove((String) dataSnapshot.child("text").getValue());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
    }

    private void buildSnackbar(View v, String text) {
        Snackbar
            .make(v, text, Snackbar.LENGTH_LONG)
            .show();
    }
}
