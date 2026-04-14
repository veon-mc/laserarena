package Repository;

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

    private static void initialiserTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS session (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom             TEXT    NOT NULL,
                    type_partie     TEXT    NOT NULL,
                    duree_minutes   INTEGER NOT NULL,
                    max_joueurs     INTEGER NOT NULL,
                    statut          TEXT    NOT NULL,
                    date_expiration TEXT
                )
            """);
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