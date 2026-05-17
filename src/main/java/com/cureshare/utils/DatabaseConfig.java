package com.cureshare.utils;

/**
 * DatabaseConfig — flip ONE line to switch between demo, SQLite, or MySQL.
 *
 * ─────────────────────────────────────────────────────────────────
 *  OPTION 1 — Demo mode (no database at all):
 *      public static final String MODE = "demo";
 *
 *  OPTION 2 — SQLite (RECOMMENDED for student viva):
 *      public static final String MODE = "sqlite";
 *      → Creates cureshare.db file automatically. Nothing else needed!
 *
 *  OPTION 3 — MySQL (advanced, needs a server):
 *      public static final String MODE = "mysql";
 *      → Also set HOST, USERNAME, PASSWORD below
 * ─────────────────────────────────────────────────────────────────
 */
public class DatabaseConfig {

    // ── CHANGE THIS LINE TO SWITCH MODES ─────────────────────────
    public static final String MODE = "sqlite";   // "demo" | "sqlite" | "mysql"
    // ─────────────────────────────────────────────────────────────

    // Helper flags (don't change these)
    public static final boolean USE_DATABASE = !MODE.equals("demo");
    public static final boolean USE_SQLITE   =  MODE.equals("sqlite");
    public static final boolean USE_MYSQL    =  MODE.equals("mysql");

    // SQLite settings (no config needed — it's just a file)
    public static final String SQLITE_FILE = "cureshare.db";

    // MySQL settings — only used when MODE = "mysql"
    public static final String HOST     = "localhost";
    public static final int    PORT     = 3306;
    public static final String DATABASE = "cureshare";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "your_mysql_password_here";

    // Built JDBC URL for MySQL
    public static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private DatabaseConfig() {} // prevent instantiation
}
