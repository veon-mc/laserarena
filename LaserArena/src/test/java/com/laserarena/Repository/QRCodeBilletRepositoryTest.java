package com.laserarena.Repository;

import com.laserarena.Model.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QRCodeBilletRepository")
class QRCodeBilletRepositoryTest {

    private static Connection             memoryConn;
    private        QRCodeBilletRepository qrCodeRepo;
    private        BilletRepository       billetRepo;
    private        SessionRepository      sessionRepo;
    private        Session                sessionTest;
    private        Billet                 billetTest;

    // ------------------------------------------------------------------ //
    //  Setup: in-memory SQLite database                                    //
    // ------------------------------------------------------------------ //

    @BeforeAll
    static void initDb() throws Exception {
        memoryConn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = memoryConn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS session (
                    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom                TEXT    NOT NULL,
                    type_partie        TEXT    NOT NULL,
                    duree_minutes      INTEGER NOT NULL,
                    max_joueurs        INTEGER NOT NULL,
                    statut             TEXT    NOT NULL,
                    date_expiration    TEXT,
                    nombre_equipes     INTEGER DEFAULT 0,
                    joueurs_par_equipe INTEGER DEFAULT 0,
                    noms_equipes       TEXT    DEFAULT ''
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
        DatabaseConnection.setInstanceForTest(memoryConn);
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Statement stmt = memoryConn.createStatement()) {
            stmt.executeUpdate("DELETE FROM qrcode_billet");
            stmt.executeUpdate("DELETE FROM billet");
            stmt.executeUpdate("DELETE FROM session");
        }
        qrCodeRepo  = new QRCodeBilletRepository();
        billetRepo  = new BilletRepository();
        sessionRepo = new SessionRepository();

        sessionTest = new Session("Test session", TypePartie.ELIMINATION, 30, 10, StatutSession.OUVERTE);
        sessionTest.setDateExpiration(LocalDate.of(2026, 12, 31));
        sessionRepo.save(sessionTest);

        billetTest = new Billet("qr-test-code", StatutBillet.VALIDE, LocalDate.of(2026, 12, 31), sessionTest);
        billetRepo.save(billetTest);
    }

    @AfterAll
    static void tearDown() throws Exception {
        DatabaseConnection.resetInstance();
        if (memoryConn != null && !memoryConn.isClosed()) memoryConn.close();
    }

    // ------------------------------------------------------------------ //
    //  save                                                                //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Should assign a generated id")
        void shouldAssignGeneratedId() {
            // Arrange
            byte[] img = {1, 2, 3, 4, 5};
            QRCodeBillet qr = new QRCodeBillet(img, billetTest);

            // Act
            qrCodeRepo.save(qr);

            // Assert
            assertTrue(qr.getId() > 0);
        }

        @Test
        @DisplayName("Should persist image bytes correctly")
        void shouldPersistImageBytes() {
            // Arrange
            byte[] img = {10, 20, 30, 40, 50};
            QRCodeBillet qr = new QRCodeBillet(img, billetTest);

            // Act
            qrCodeRepo.save(qr);
            byte[] result = qrCodeRepo.findImageByBillet(billetTest);

            // Assert
            assertNotNull(result);
            assertArrayEquals(img, result);
        }

        @Test
        @DisplayName("Should persist association with the correct ticket")
        void shouldPersistAssociationWithTicket() {
            // Arrange
            byte[] img = {1, 2, 3};
            QRCodeBillet qr = new QRCodeBillet(img, billetTest);

            // Act
            qrCodeRepo.save(qr);
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(billetTest.getId(), result.get().getBillet().getId());
        }

        @Test
        @DisplayName("Should throw on duplicate billet_id")
        void shouldThrowOnDuplicateBilletId() {
            // Arrange
            qrCodeRepo.save(new QRCodeBillet(new byte[]{1}, billetTest));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> qrCodeRepo.save(new QRCodeBillet(new byte[]{2}, billetTest)));
        }
    }

    // ------------------------------------------------------------------ //
    //  findImageByBillet                                                    //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("findImageByBillet")
    class FindImageByBilletTests {

        @Test
        @DisplayName("Should return image bytes for existing QR code")
        void shouldReturnImageBytes() {
            // Arrange
            byte[] img = {99, 88, 77};
            qrCodeRepo.save(new QRCodeBillet(img, billetTest));

            // Act
            byte[] result = qrCodeRepo.findImageByBillet(billetTest);

            // Assert
            assertNotNull(result);
            assertArrayEquals(img, result);
        }

        @Test
        @DisplayName("Should return null when no QR code exists for ticket")
        void shouldReturnNullWhenNoQRCode() {
            // Act
            byte[] result = qrCodeRepo.findImageByBillet(billetTest);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should return correct image for the right ticket")
        void shouldReturnCorrectImageForRightTicket() {
            // Arrange
            Billet billet2 = new Billet("qr-test-2", StatutBillet.VALIDE, LocalDate.of(2026, 12, 31), sessionTest);
            billetRepo.save(billet2);

            byte[] img1 = {1, 1, 1};
            byte[] img2 = {2, 2, 2};
            qrCodeRepo.save(new QRCodeBillet(img1, billetTest));
            qrCodeRepo.save(new QRCodeBillet(img2, billet2));

            // Act
            byte[] result1 = qrCodeRepo.findImageByBillet(billetTest);
            byte[] result2 = qrCodeRepo.findImageByBillet(billet2);

            // Assert
            assertArrayEquals(img1, result1);
            assertArrayEquals(img2, result2);
        }
    }

    // ------------------------------------------------------------------ //
    //  findByBillet                                                         //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("findByBillet")
    class FindByBilletTests {

        @Test
        @DisplayName("Should return QRCodeBillet for existing entry")
        void shouldReturnQRCodeBillet() {
            // Arrange
            byte[] img = {5, 6, 7};
            qrCodeRepo.save(new QRCodeBillet(img, billetTest));

            // Act
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isPresent());
            assertArrayEquals(img, result.get().getImageBytes());
            assertEquals(billetTest.getId(), result.get().getBillet().getId());
        }

        @Test
        @DisplayName("Should return empty when no QR code exists")
        void shouldReturnEmptyWhenNoQRCode() {
            // Act
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should set the id on the returned QRCodeBillet")
        void shouldSetIdOnReturnedEntity() {
            // Arrange
            QRCodeBillet qr = new QRCodeBillet(new byte[]{1}, billetTest);
            qrCodeRepo.save(qr);

            // Act
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isPresent());
            assertTrue(result.get().getId() > 0);
            assertEquals(qr.getId(), result.get().getId());
        }
    }

    // ------------------------------------------------------------------ //
    //  generateAndSave                                                     //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("generateAndSave")
    class GenerateAndSaveTests {

        @Test
        @DisplayName("Should generate and persist a QR code for the ticket")
        void shouldGenerateAndPersist() {
            // Act
            qrCodeRepo.generateAndSave(billetTest);

            // Assert
            byte[] image = qrCodeRepo.findImageByBillet(billetTest);
            assertNotNull(image);
            assertTrue(image.length > 0);
        }

        @Test
        @DisplayName("Generated image should be a valid PNG")
        void generatedImageShouldBeValidPng() {
            // Act
            qrCodeRepo.generateAndSave(billetTest);
            byte[] image = qrCodeRepo.findImageByBillet(billetTest);

            // Assert — PNG magic bytes: 0x89 0x50 0x4E 0x47
            assertNotNull(image);
            assertTrue(image.length > 4);
            assertEquals((byte) 0x89, image[0]);
            assertEquals((byte) 0x50, image[1]); // P
            assertEquals((byte) 0x4E, image[2]); // N
            assertEquals((byte) 0x47, image[3]); // G
        }

        @Test
        @DisplayName("Should be retrievable via findByBillet after generation")
        void shouldBeRetrievableViaFindByBillet() {
            // Act
            qrCodeRepo.generateAndSave(billetTest);
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isPresent());
            assertNotNull(result.get().getImageBytes());
            assertTrue(result.get().getImageBytes().length > 0);
            assertSame(billetTest, result.get().getBillet());
        }

        @Test
        @DisplayName("Different tickets should produce different QR code images")
        void differentTicketsProduceDifferentImages() {
            // Arrange
            Billet billet2 = new Billet("different-code", StatutBillet.VALIDE, LocalDate.of(2026, 12, 31), sessionTest);
            billetRepo.save(billet2);

            // Act
            qrCodeRepo.generateAndSave(billetTest);
            qrCodeRepo.generateAndSave(billet2);

            byte[] img1 = qrCodeRepo.findImageByBillet(billetTest);
            byte[] img2 = qrCodeRepo.findImageByBillet(billet2);

            // Assert
            assertNotNull(img1);
            assertNotNull(img2);
            assertFalse(java.util.Arrays.equals(img1, img2));
        }
    }
}