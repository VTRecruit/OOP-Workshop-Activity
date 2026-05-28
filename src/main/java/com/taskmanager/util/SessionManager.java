package com.taskmanager.util;

import com.taskmanager.model.User;

/**
 * In-memory session store.
 * The current user is NEVER persisted to disk; a new login is required every launch.
 */
public class SessionManager {

    private static User currentUser;

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser()          { return currentUser; }
    public static boolean isLoggedIn()           { return currentUser != null; }
    public static void logout()                  { currentUser = null; }
}
