package com.cureshare.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditLog {

    public static class Entry {
        private final String id;
        private final LocalDateTime timestamp;
        private final String userId;
        private final String userName;
        private final String action;
        private final String detail;
        private final String category;

        public Entry(String userId, String userName, String action, String detail, String category) {
            this.id        = "AL" + System.currentTimeMillis();
            this.timestamp = LocalDateTime.now();
            this.userId    = userId;
            this.userName  = userName;
            this.action    = action;
            this.detail    = detail;
            this.category  = category;
        }

        public String getId()        { return id; }
        public String getTimestamp() { return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")); }
        public String getUserId()    { return userId; }
        public String getUserName()  { return userName; }
        public String getAction()    { return action; }
        public String getDetail()    { return detail; }
        public String getCategory()  { return category; }
    }

    private static final AuditLog instance = new AuditLog();
    private final List<Entry> entries = Collections.synchronizedList(new ArrayList<>());

    private AuditLog() {}
    public static AuditLog getInstance() { return instance; }

    public void log(String userId, String userName, String action, String detail, String category) {
        Entry e = new Entry(userId, userName, action, detail, category);
        entries.add(0, e);
        if (entries.size() > 1000) entries.subList(900, entries.size()).clear();

        // Route to the correct DB — NEVER touch MySQL when in SQLite mode
        if (DatabaseConfig.USE_SQLITE) {
            try {
                SQLiteDataStore sqlite = SQLiteDataStore.getInstance();
                if (sqlite != null && sqlite.isConnected()) {
                    sqlite.logAudit(userId, userName, action, detail, category);
                }
            } catch (Exception ex) { /* silent */ }
        } else if (DatabaseConfig.USE_MYSQL) {
            try {
                MySQLDataStore mysql = MySQLDataStore.getInstance();
                if (mysql != null && mysql.isConnected()) {
                    mysql.logAudit(userId, userName, action, detail, category);
                }
            } catch (Exception ex) { /* silent */ }
        }
    }

    public List<Entry> getAll() {
        if (DatabaseConfig.USE_SQLITE) {
            try {
                SQLiteDataStore sqlite = SQLiteDataStore.getInstance();
                if (sqlite != null && sqlite.isConnected()) return sqlite.getAuditLog(500);
            } catch (Exception ignored) {}
        } else if (DatabaseConfig.USE_MYSQL) {
            try {
                MySQLDataStore mysql = MySQLDataStore.getInstance();
                if (mysql != null && mysql.isConnected()) return mysql.getAuditLog(500);
            } catch (Exception ignored) {}
        }
        return new ArrayList<>(entries);
    }

    public int getCount() {
        if (DatabaseConfig.USE_SQLITE) {
            try {
                SQLiteDataStore sqlite = SQLiteDataStore.getInstance();
                if (sqlite != null && sqlite.isConnected()) return sqlite.getAuditCount();
            } catch (Exception ignored) {}
        } else if (DatabaseConfig.USE_MYSQL) {
            try {
                MySQLDataStore mysql = MySQLDataStore.getInstance();
                if (mysql != null && mysql.isConnected()) return mysql.getAuditCount();
            } catch (Exception ignored) {}
        }
        return entries.size();
    }

    public List<Entry> getRecent(int n) {
        return getAll().stream().limit(n).toList();
    }

    public List<Entry> getByCategory(String cat) {
        return getAll().stream().filter(e -> cat.equalsIgnoreCase(e.getCategory())).toList();
    }
}