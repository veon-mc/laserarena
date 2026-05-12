package com.laserarena.Repository;

import com.laserarena.Model.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link QRCodeBilletRepository} using an in-memory SQLite database.
 * <p>
 * Covers the full lifecycle of a QR code entity: manual persistence via
 * {@link QRCodeBilletRepository#save(QRCodeBillet)}, automatic generation and persistence via
 * {@link QRCodeBilletRepository#generateAndSave(Billet)}, and retrieval via
 * {@link QRCodeBilletRepository#findByBillet(Billet)} and
 * {@link QRCodeBilletRepository#findImageByBillet(Billet)}.
 * All tests follow the AAA (Arrange / Act / Assert) pattern.
 * </p>
 */
@DisplayName("QRCodeBilletRepository")
class QRCodeBilletRepositoryTest {

    private static Connection             memoryConn;
    private        QRCodeBilletRepository qrCodeRepo;
    private        BilletRepository       billetRepo;
    private        SessionRepository      sessionRepo;
    private        Session                sessionTest;
    private        Billet                 billetTest;

    /**
     * Creates the in-memory SQLite schema once for the entire test class.
     * The connection is injected into {@link DatabaseConnection} so that
     * all repository instances use it instead of the production file.
     *
     * @throws Exception if the connection or table creation fails
     */
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

    /**
     * Clears all rows from the three tables, re-instantiates the repositories,
     * and inserts a fresh reference session and ticket before each test.
     *
     * @throws Exception if the DELETE statements fail
     */
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

    /**
     * Resets the {@link DatabaseConnection} singleton and closes the
     * in-memory connection after all tests have run.
     *
     * @throws Exception if the connection cannot be closed
     */
    @AfterAll
    static void tearDown() throws Exception {
        DatabaseConnection.resetInstance();
        if (memoryConn != null && !memoryConn.isClosed()) memoryConn.close();
    }


    /**
     * Tests for the {@link QRCodeBilletRepository#save(QRCodeBillet)} method.
     */
    @Nested
    @DisplayName("save")
    class SaveTests {

        /**
         * Verifies that {@link QRCodeBilletRepository#save(QRCodeBillet)} assigns
         * a positive generated id to the persisted entity.
         */
        @Test
        @DisplayName("Assigne un id généré positif")
        void shouldAssignGeneratedId() {
            // Arrange
            byte[]       img = {1, 2, 3, 4, 5};
            QRCodeBillet qr  = new QRCodeBillet(img, billetTest);

            // Act
            qrCodeRepo.save(qr);

            // Assert
            assertTrue(qr.getId() > 0);
        }

        /**
         * Verifies that the raw image bytes are persisted correctly and can be
         * retrieved via {@link QRCodeBilletRepository#findImageByBillet(Billet)}.
         */
        @Test
        @DisplayName("Persiste les image bytes correctement")
        void shouldPersistImageBytes() {
            // Arrange
            byte[]       img = {10, 20, 30, 40, 50};
            QRCodeBillet qr  = new QRCodeBillet(img, billetTest);

            // Act
            qrCodeRepo.save(qr);
            byte[] result = qrCodeRepo.findImageByBillet(billetTest);

            // Assert
            assertNotNull(result);
            assertArrayEquals(img, result);
        }

        /**
         * Verifies that the saved entity is linked to the correct ticket.
         */
        @Test
        @DisplayName("Persiste l'association avec le bon billet")
        void shouldPersistAssociationWithTicket() {
            // Arrange
            byte[]       img = {1, 2, 3};
            QRCodeBillet qr  = new QRCodeBillet(img, billetTest);

            // Act
            qrCodeRepo.save(qr);
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(billetTest.getId(), result.get().getBillet().getId());
        }

        /**
         * Verifies that saving a second QR code for the same ticket throws a
         * {@link RuntimeException} due to the UNIQUE constraint on {@code billet_id}.
         */
        @Test
        @DisplayName("Lève une exception en cas de billet_id dupliqué")
        void shouldThrowOnDuplicateBilletId() {
            // Arrange
            qrCodeRepo.save(new QRCodeBillet(new byte[]{1}, billetTest));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> qrCodeRepo.save(new QRCodeBillet(new byte[]{2}, billetTest)));
        }
    }


    /**
     * Tests for the {@link QRCodeBilletRepository#findImageByBillet(Billet)} method.
     */
    @Nested
    @DisplayName("findImageByBillet")
    class FindImageByBilletTests {

        /**
         * Verifies that the method returns the correct byte array when a QR code
         * exists for the given ticket.
         */
        @Test
        @DisplayName("Retourne les image bytes du QR code existant")
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

        /**
         * Verifies that the method returns {@code null} when no QR code has been
         * persisted for the given ticket.
         */
        @Test
        @DisplayName("Retourne null si aucun QR code n'existe pour ce billet")
        void shouldReturnNullWhenNoQRCode() {
            // Act
            byte[] result = qrCodeRepo.findImageByBillet(billetTest);

            // Assert
            assertNull(result);
        }

        /**
         * Verifies that each ticket retrieves only its own QR code image
         * when multiple tickets exist.
         */
        @Test
        @DisplayName("Retourne l'image correcte pour le bon billet")
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


    /**
     * Tests for the {@link QRCodeBilletRepository#findByBillet(Billet)} method.
     */
    @Nested
    @DisplayName("findByBillet")
    class FindByBilletTests {

        /**
         * Verifies that the method returns the full {@link QRCodeBillet} entity
         * when a QR code exists for the given ticket.
         */
        @Test
        @DisplayName("Retourne le QRCodeBillet existant")
        void shouldReturnQRCodeBillet() {
            // Arrange
            byte[]       img = {5, 6, 7};
            qrCodeRepo.save(new QRCodeBillet(img, billetTest));

            // Act
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isPresent());
            assertArrayEquals(img, result.get().getImageBytes());
            assertEquals(billetTest.getId(), result.get().getBillet().getId());
        }

        /**
         * Verifies that the method returns an empty {@link Optional} when no QR code
         * has been persisted for the given ticket.
         */
        @Test
        @DisplayName("Retourne Optional vide si aucun QR code n'existe")
        void shouldReturnEmptyWhenNoQRCode() {
            // Act
            Optional<QRCodeBillet> result = qrCodeRepo.findByBillet(billetTest);

            // Assert
            assertTrue(result.isEmpty());
        }

        /**
         * Verifies that the returned {@link QRCodeBillet} has the same id as the
         * one assigned during the save operation.
         */
        @Test
        @DisplayName("L'entité retournée a un id cohérent avec celui assigné lors du save")
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


    /**
     * Tests for the {@link QRCodeBilletRepository#generateAndSave(Billet)} method,
     * which internally uses ZXing to produce a PNG image from the ticket code.
     */
    @Nested
    @DisplayName("generateAndSave")
    class GenerateAndSaveTests {

        /**
         * Verifies that {@link QRCodeBilletRepository#generateAndSave(Billet)} produces
         * and persists a non-null, non-empty image for the given ticket.
         */
        @Test
        @DisplayName("Génère et persiste un QR code pour le billet")
        void shouldGenerateAndPersist() {
            // Act
            qrCodeRepo.generateAndSave(billetTest);

            // Assert
            byte[] image = qrCodeRepo.findImageByBillet(billetTest);
            assertNotNull(image);
            assertTrue(image.length > 0);
        }

        /**
         * Verifies that the generated image starts with the PNG magic bytes
         * {@code 0x89 0x50 0x4E 0x47}, confirming the output format.
         */
        @Test
        @DisplayName("L'image générée est un PNG valide")
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

        /**
         * Verifies that the generated entity is retrievable via
         * {@link QRCodeBilletRepository#findByBillet(Billet)} and is correctly
         * linked to the original ticket.
         */
        @Test
        @DisplayName("Le QR code généré est accessible via findByBillet")
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

        /**
         * Verifies that two different tickets produce different QR code images,
         * since their encoded content (the UUID code) differs.
         */
        @Test
        @DisplayName("Deux billets différents produisent des images QR différentes")
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
