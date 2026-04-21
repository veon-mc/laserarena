package com.laserarena.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:lasararena.db";
    private static Connection   instance;

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL);
            initialiserTables(instance);
        }
        return instance;
    }

    /** Injecte une connexion externe (SQLite en mémoire) pour les tests. */
    public static void setInstanceForTest(Connection conn) {
        instance = conn;
    }

    /** Réinitialise la connexion après les tests. */
    public static void resetInstance() {
        instance = null;
    }

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
