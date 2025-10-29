package com.cosplay.util;

import com.cosplay.model.User;

/** Simple in-memory session holder for the current logged-in user. */
public class Session {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
