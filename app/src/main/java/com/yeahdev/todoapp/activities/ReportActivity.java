package com.yeahdev.todoapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yeahdev.todoapp.R;
import com.yeahdev.todoapp.helper.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ReportActivity extends AppCompatActivity {

    private TextView tvLogMessage;
    private String logMessage;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initToolbar();
        initComponents();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString(Constants.USERID);
            if (userId != null) {
                registerFab();
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
        tvLogMessage = (TextView) findViewById(R.id.tvLogMessage);
        logMessage = "";
    }

    private void registerFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabCreateReport);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Simulate Get Messsage", Snackbar.LENGTH_SHORT).show();
                simulateGetMethod();
            }
        });
    }

    private void simulateGetMethod() {
        setLog("BEGIN TODOREPORT\n");
        setLog("Cron Job started!");
        setLog("Fetching Data from Firebase!");

        final Firebase firebase = new Firebase(Constants.BASEURL + Constants.ROUTE + "/" + userId);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setLog("\nBuild Email!");

                final StringBuilder newItemMessage = new StringBuilder();
                newItemMessage.append("Good Morning!\n\n\nYou have the following todo items:\n\n");

                for (DataSnapshot todoItem : dataSnapshot.getChildren()) {
                    for (DataSnapshot field : todoItem.getChildren()) {

                        newItemMessage
                                .append("- ")
                                .append(field.getKey())
                                .append(": ")
                                .append(field.getValue().toString())
                                .append("\n");
                    }
                }
                setLog("Send Email!");
                sendReportEmail(newItemMessage.toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                setLog("ERROR: Firebase onCancelled: " + firebaseError.getMessage());
            }
        });

        setLog("Cron Job completed!");
        setLog("\nEND TODOREPORT");
    }

    private boolean sendReportEmail(String message) {
        try {
            String date = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).getTime());
            String[] TO = {"yeahdev@gmail.com"};

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder - ToDo Items - " + date);
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);

            startActivityForResult(emailIntent, Constants.EMAIL_REQUEST_CODE);
            return true;

        } catch (Exception e) {
            setLog("Send Email failed! " + e.getMessage());
            return false;
        }
    }

    private void setLog(String lm) {
        logMessage += lm + "\n";
        tvLogMessage.setText(logMessage);
    }

    private void goBack() {
        Toast.makeText(ReportActivity.this, "User not logged in!", Toast.LENGTH_LONG).show();
        startActivity(new Intent(ReportActivity.this, MainActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == Constants.EMAIL_REQUEST_CODE) {
           setLog("\nSend Email successful!");
       }
    }
}
