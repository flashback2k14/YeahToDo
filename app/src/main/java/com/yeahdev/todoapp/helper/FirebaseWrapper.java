package com.yeahdev.todoapp.helper;

import android.support.annotation.Nullable;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class FirebaseWrapper {

    public interface OnAuthListener {
        void onSuccess(AuthData authData);
        void onFailed(FirebaseError error);
    }
    public interface OnLoadListener {
        void onAdded(String item);
        void onRemoved(String item);
        void onCanceled(FirebaseError error);
    }
    public interface OnChangedListener {
        void onSuccess(String item);
        void onFailed(FirebaseError error);
    }

    private static HashMap<Firebase, ChildEventListener> listenerMap = new HashMap<>();
    private static ChildEventListener loadListener;

    /**
     *
     * @param baseUrl
     * @return
     */
    @Nullable
    public static String getUserId(String baseUrl) {
        AuthData authData = new Firebase(baseUrl).getAuth();
        if (authData != null) {
            return authData.getUid();
        } else {
            return null;
        }
    }

    /**
     *
     * @param baseUrl
     * @param email
     * @param password
     * @param listener
     */
    public static void authWithPassword(String baseUrl, String email, String password, final OnAuthListener listener) {
        new Firebase(baseUrl).authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                if (listener != null) {
                    listener.onSuccess(authData);
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                if (listener != null) {
                    listener.onFailed(firebaseError);
                }
            }
        });
    }

    /**
     *
     * @param baseUrl
     * @return
     */
    public static boolean logout(String baseUrl) {
        new Firebase(baseUrl).unauth();
        return true;
    }

    /**
     *
     */
    public static void removeListener() {
        if (FirebaseWrapper.loadListener != null) {
            for (Map.Entry<Firebase, ChildEventListener> entry : FirebaseWrapper.listenerMap.entrySet()) {
                Firebase ref = entry.getKey();
                ChildEventListener listener = entry.getValue();
                ref.removeEventListener(listener);
            }
            FirebaseWrapper.loadListener = null;
        }
    }

    /**
     *
     * @param baseUrl
     * @param route
     * @param listener
     */
    public static void loadData(String baseUrl, String route, String userId, final OnLoadListener listener) {
        Firebase firebase = new Firebase(baseUrl + route + "/" + userId);
        FirebaseWrapper.loadListener =  new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (listener != null) {
                        String item = (String) dataSnapshot.child("text").getValue();
                        listener.onAdded(item);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (listener != null) {
                        String item = (String) dataSnapshot.child("text").getValue();
                        listener.onRemoved(item);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (listener != null) {
                        listener.onCanceled(firebaseError);
                    }
                }
            };

        FirebaseWrapper.listenerMap.put(firebase, FirebaseWrapper.loadListener);
        firebase.addChildEventListener(FirebaseWrapper.loadListener);
    }

    /**
     *
     * @param baseUrl
     * @param route
     * @param item
     * @param listener
     */
    public static void addItem(String baseUrl, final String route, String userId, final String item, final OnChangedListener listener) {
        new Firebase(baseUrl + route + "/" + userId)
            .push()
            .child("text")
            .setValue(item, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        if (listener != null) {
                            listener.onFailed(firebaseError);
                        }
                    } else {
                        if (listener != null) {
                            listener.onSuccess("Todo: " + item + " added to " + route + "!");
                        }
                    }
                }
            });
    }

    /**
     *
     * @param baseUrl
     * @param route
     * @param item
     * @param listener
     */
    public static void removeItem(String baseUrl, final String route, String userId, final String item, final OnChangedListener listener) {
        new Firebase(baseUrl + route + "/" + userId)
            .orderByChild("text")
            .equalTo(item)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        final DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                        firstChild.getRef().removeValue(new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    if (listener != null) {
                                        listener.onFailed(firebaseError);
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onSuccess("Todo: " + item + " removed from " + route + "!");
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
    }
}
