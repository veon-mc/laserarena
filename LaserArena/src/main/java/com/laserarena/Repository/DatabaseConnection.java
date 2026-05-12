package com.laserarena.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton managing the SQLite database connection for the application.
 * <p>
 * Provides a single shared {@link Connection} instance and automatically
 * creates the required tables ({@code session}, {@code billet}, {@code qrcode_billet})
 * on first connection. Also supports injecting a test connection (e.g. in-memory SQLite)
 * via {@link #setInstanceForTest(Connection)}.
 * </p>
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:lasararena.db";
    private static Connection   instance;

    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseConnection() {}

    /**
     * Returns the singleton database connection, creating it if necessary.
     * On first connection, initializes the database tables.
     *
     * @return the shared {@link Connection} instance
     * @throws RuntimeException if the connection cannot be established
     */
    public static Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(URL);
                initialiserTables(instance);
            }
            return instance;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    /**
     * Injects an external connection (e.g. in-memory SQLite) for testing purposes.
     *
     * @param conn the test connection to use
     */
    public static void setInstanceForTest(Connection conn) {
        instance = conn;
    }

    /**
     * Resets the singleton connection to {@code null}.
     * Should be called after tests to restore normal behavior.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Creates the required database tables if they do not already exist.
     * <p>
     * Also attempts to add team-related columns to the {@code session} table
     * for backward compatibility with older schemas. These {@code ALTER TABLE}
     * statements are silently ignored if the columns already exist.
     * </p>
     *
     * @param conn the database connection
     * @throws SQLException if a table creation statement fails
     */
    private static void initialiserTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS session (
                    id                INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom               TEXT    NOT NULL,
                    type_partie       TEXT    NOT NULL,
                    duree_minutes     INTEGER NOT NULL,
                    max_joueurs       INTEGER NOT NULL,
                    statut            TEXT    NOT NULL,
                    date_expiration   TEXT,
                    nombre_equipes    INTEGER DEFAULT 0,
                    joueurs_par_equipe INTEGER DEFAULT 0,
                    noms_equipes      TEXT    DEFAULT ''
                )
            """);
            try { stmt.executeUpdate("ALTER TABLE session ADD COLUMN nombre_equipes INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE session ADD COLUMN joueurs_par_equipe INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE session ADD COLUMN noms_equipes TEXT DEFAULT ''"); } catch (Exception ignored) {}
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS billet (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    code            TEXT    NOT NULL UNIQUE,
                    statut          TEXT    NOT NULL,
                    date_expiration TEXT    NOT NULL,
                    session_id      INTEGER NOT NULL,
                    FOREIGN KEY (session_id) REFERENCES session(id)
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS qrcode_billet (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    image_bytes BLOB    NOT NULL,
                    billet_id   INTEGER NOT NULL UNIQUE,
                    FOREIGN KEY (billet_id) REFERENCES billet(id)
                )
            """);
        }
    }
}