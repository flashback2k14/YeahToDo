package com.yeahdev.todoapp.helper;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class FirebaseWrapper {

    public interface OnLoadListener {
        void onAdded(String item);
        void onRemoved(String item);
        void onCanceled(FirebaseError error);
    }

    public interface OnChangedListener {
        void onSuccess(String item);
        void onFailed(FirebaseError error);
    }

    public interface OnFullListener {
        public void onAdded(String item);
        public void onRemoved(String item);
        public void onSuccess(String item);
        public void onFailed(FirebaseError error);
        public void onCanceled(FirebaseError error);
    }

    /**
     *
     * @param baseUrl
     * @param route
     * @param listener
     */
    public static void loadData(String baseUrl, String route, final OnLoadListener listener) {
        new Firebase(baseUrl + route)
            .addChildEventListener(new ChildEventListener() {
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
            });
    }

    /**
     *
     * @param baseUrl
     * @param route
     * @param item
     * @param listener
     */
    public static void addItem(String baseUrl, final String route, final String item, final OnChangedListener listener) {
        new Firebase(baseUrl + route)
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
    public static void removeItem(String baseUrl, final String route, final String item, final OnChangedListener listener) {
        new Firebase(baseUrl + route)
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
