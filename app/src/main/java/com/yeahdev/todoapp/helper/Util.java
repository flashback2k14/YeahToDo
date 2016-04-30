package com.yeahdev.todoapp.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.firebase.client.FirebaseError;


public class Util {

    /**
     *
     * @param v
     * @param text
     */
    public static void buildSnackbar(View v, String text) {
        Snackbar
            .make(v, text, Snackbar.LENGTH_LONG)
            .show();
    }

    /**
     *
     * @param activity
     * @param view
     * @param baseUrl
     * @param route
     * @param deleteRoute
     * @param item
     */
    public static void buildConfirmDialog(Activity activity, final View view, final String baseUrl, final String route, final String deleteRoute, final String userId, final String item) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("Remove Item");
        alertDialogBuilder
                .setMessage("Are you sure to remove this Item?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        FirebaseWrapper.addItem(baseUrl, deleteRoute, userId, item, new FirebaseWrapper.OnChangedListener() {
                            @Override
                            public void onSuccess(String item) {
                                Util.buildSnackbar(view, item);
                            }
                            @Override
                            public void onFailed(FirebaseError error) {
                                Util.buildSnackbar(view, error.getMessage());
                            }
                        });

                        FirebaseWrapper.removeItem(baseUrl, route, userId, item, new FirebaseWrapper.OnChangedListener() {
                            @Override
                            public void onSuccess(String item) {
                                Util.buildSnackbar(view, item);
                            }
                            @Override
                            public void onFailed(FirebaseError error) {
                                Util.buildSnackbar(view, error.getMessage());
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     *
     * @param activity
     * @param view
     * @param baseUrl
     * @param deleteRoute
     * @param item
     */
    public static void buildConfirmDialogOnlyRemove(Activity activity, final View view, final String baseUrl, final String deleteRoute, final String userId, final String item) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("Remove Item");
        alertDialogBuilder
                .setMessage("Are you sure to remove this Item?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        FirebaseWrapper.removeItem(baseUrl, deleteRoute, userId, item, new FirebaseWrapper.OnChangedListener() {
                            @Override
                            public void onSuccess(String item) {
                                Util.buildSnackbar(view, item);
                            }
                            @Override
                            public void onFailed(FirebaseError error) {
                                Util.buildSnackbar(view, error.getMessage());
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
