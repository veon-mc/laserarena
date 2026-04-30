package com.laserarena.Repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseConnection")
class DatabaseConnectionTest {

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
                    () -> assertTrue(tableExiste(memory, "session")),
                    () -> assertTrue(tableExiste(memory, "billet")),
                    () -> assertTrue(tableExiste(memory, "qrcode_billet"))
            );
        } finally {
            memory.close();
        }
    }

    private boolean tableExiste(Connection conn, String tableName) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
            return rs.next();
        }
    }
}