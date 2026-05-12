package com.laserarena.Repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link DatabaseConnection} singleton class.
 * <p>
 * Verifies that test injection via {@link DatabaseConnection#setInstanceForTest(Connection)}
 * works correctly, and that the private {@code initialiserTables} method creates
 * the expected database schema on a fresh in-memory SQLite database.
 * All tests follow the AAA (Arrange / Act / Assert) pattern.
 * </p>
 */
@DisplayName("DatabaseConnection")
class DatabaseConnectionTest {

    /**
     * Verifies that {@link DatabaseConnection#setInstanceForTest(Connection)} forces
     * {@link DatabaseConnection#getInstance()} to return the injected connection,
     * enabling isolated unit tests without touching the production database.
     *
     * @throws Exception if the in-memory connection cannot be created
     */
    @Test
    @DisplayName("setInstanceForTest force getInstance à retourner la connexion injectée")
    void setInstanceForTestRetourneConnexionInjectee() throws Exception {
        // Arrange
        Connection memory = DriverManager.getConnection("jdbc:sqlite::memory:");

        try {
            // Act
            DatabaseConnection.setInstanceForTest(memory);
            Connection result = DatabaseConnection.getInstance();

            // Assert
            assertSame(memory, result);
            assertFalse(result.isClosed());
        } finally {
            DatabaseConnection.resetInstance();
            memory.close();
        }
    }

    /**
     * Verifies that the private {@code initialiserTables} method creates the three
     * required tables: {@code session}, {@code billet} and {@code qrcode_billet}.
     * <p>
     * Uses reflection to access the private method since it is not part of the
     * public API of {@link DatabaseConnection}.
     * </p>
     *
     * @throws Exception if reflection fails or the in-memory database cannot be created
     */
    @Test
    @DisplayName("initialiserTables crée les trois tables principales")
    void initialiserTablesCreeTablesPrincipales() throws Exception {
        // Arrange
        Connection memory = DriverManager.getConnection("jdbc:sqlite::memory:");
        Method initialiserTables = DatabaseConnection.class.getDeclaredMethod("initialiserTables", Connection.class);
        initialiserTables.setAccessible(true);

        try {
            // Act
            initialiserTables.invoke(null, memory);

            // Assert
            assertAll(
                    () -> assertTrue(tableExiste(memory, "session"),       "La table 'session' doit exister"),
                    () -> assertTrue(tableExiste(memory, "billet"),        "La table 'billet' doit exister"),
                    () -> assertTrue(tableExiste(memory, "qrcode_billet"), "La table 'qrcode_billet' doit exister")
            );
        } finally {
            memory.close();
        }
    }

    /**
     * Helper method that checks whether a table exists in the given SQLite connection.
     *
     * @param conn      the database connection to query
     * @param tableName the name of the table to look up
     * @return {@code true} if the table exists, {@code false} otherwise
     * @throws Exception if the query fails
     */
    private boolean tableExiste(Connection conn, String tableName) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
            return rs.next();
        }
    }
}
