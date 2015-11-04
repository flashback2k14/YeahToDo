/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.yeahdev.todoapp.backend;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.*;


public class TodoReportServlet extends HttpServlet {

    static Logger log = Logger.getLogger(TodoReportServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        log.warning("Got cron message, constructing email!");

        final Firebase firebase = new Firebase("https://todoapp-appengine.firebaseio.com/todoitems");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                log.warning("Build Email!");

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

                log.warning("Send Email!");

                Properties properties = new Properties();
                Session session = Session.getDefaultInstance(properties, null);

                try {
                    String date = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).getTime());
                    Message msg = new MimeMessage(session);

                    msg.setFrom(new InternetAddress("reminder@midyear-task-111217.appspotmail.com", "ToDo Reminder"));
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress("yeahdev@gmail.com", "Recipient"));
                    msg.setSubject("Reminder - ToDo Items - " + date);
                    msg.setText(newItemMessage.toString());

                    Transport.send(msg);

                    log.warning("Send Email successful!");

                } catch (MessagingException | UnsupportedEncodingException e) {
                    log.warning("Send Email failed! " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                log.warning("ERROR: Firebase onCancelled: " + firebaseError.getMessage());
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req,resp);
    }
}
