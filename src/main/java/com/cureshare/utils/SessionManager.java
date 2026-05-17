package com.cureshare.utils;

import com.cureshare.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(User u) {
        this.currentUser = u;
        AuditLog.getInstance().log(u.getId(), u.getName(), "LOGIN",
            "User logged in as " + u.getRole(), "AUTH");
    }

    public void logout() {
        if (currentUser != null) {
            AuditLog.getInstance().log(currentUser.getId(), currentUser.getName(),
                "LOGOUT", "User logged out", "AUTH");
        }
        this.currentUser = null;
    }

    public User   getCurrentUser() { return currentUser; }
    public boolean isLoggedIn()    { return currentUser != null; }
    public String  getRole()       { return currentUser != null ? currentUser.getRole() : ""; }
}
