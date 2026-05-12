package com.laserarena.Repository;

import com.laserarena.Model.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link BilletRepository} using an in-memory SQLite database.
 * <p>
 * Each test runs against a fresh, isolated dataset. A reference {@link Session} is
 * created in {@link #setUp()} and reused across tests as the parent of all test tickets.
 * The in-memory connection is shared via {@link DatabaseConnection#setInstanceForTest(Connection)}.
 * All tests follow the AAA (Arrange / Act / Assert) pattern.
 * </p>
 */
@DisplayName("Repository Billet")
class BilletRepositoryTest {

    private static Connection        memoryConn;
    private        BilletRepository  billetRepo;
    private        SessionRepository sessionRepo;
    private        Session           sessionTest;

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
        }
        DatabaseConnection.setInstanceForTest(memoryConn);
    }

    /**
     * Clears all rows from the {@code billet} and {@code session} tables,
     * re-instantiates the repositories, and inserts a fresh reference session
     * before each test to guarantee isolation.
     *
     * @throws Exception if the DELETE statements fail
     */
    @BeforeEach
    void setUp() throws Exception {
        try (Statement stmt = memoryConn.createStatement()) {
            stmt.executeUpdate("DELETE FROM billet");
            stmt.executeUpdate("DELETE FROM session");
        }
        billetRepo  = new BilletRepository();
        sessionRepo = new SessionRepository();

        sessionTest = new Session("Session test", TypePartie.ELIMINATION, 30, 10, StatutSession.OUVERTE);
        sessionRepo.save(sessionTest);
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
     * Verifies that {@link BilletRepository#save(Billet)} assigns a positive
     * generated id to the persisted ticket.
     */
    @Test
    @DisplayName("save assigne un id généré positif")
    void save_devrait_assigner_un_id_genere() {
        // Arrange
        Billet b = billet("code-001", StatutBillet.VALIDE);

        // Act
        billetRepo.save(b);

        // Assert
        assertTrue(b.getId() > 0);
    }

    /**
     * Verifies that {@link BilletRepository#save(Billet)} persists all fields
     * correctly, including the ticket code, status, expiration date and session link.
     */
    @Test
    @DisplayName("save persiste tous les champs du billet")
    void save_devrait_persister_tous_les_champs() {
        // Arrange
        Billet b = billet("code-002", StatutBillet.VALIDE);

        // Act
        billetRepo.save(b);
        Optional<Billet> result = billetRepo.findByCode("code-002");

        // Assert
        assertTrue(result.isPresent());
        Billet found = result.get();
        assertEquals("code-002",                   found.getCode());
        assertEquals(StatutBillet.VALIDE,           found.getStatut());
        assertEquals(LocalDate.of(2026, 12, 31),   found.getDateExpiration());
        assertEquals(sessionTest.getId(),           found.getSession().getId());
    }


    /**
     * Verifies that {@link BilletRepository#findByCode(String)} returns the
     * correct ticket when it exists in the database.
     */
    @Test
    @DisplayName("findByCode retourne le billet existant")
    void findByCode_devrait_retourner_billet_existant() {
        // Arrange
        billetRepo.save(billet("code-003", StatutBillet.VALIDE));

        // Act
        Optional<Billet> result = billetRepo.findByCode("code-003");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("code-003", result.get().getCode());
    }

    /**
     * Verifies that {@link BilletRepository#findByCode(String)} returns an empty
     * {@link Optional} when no ticket matches the given code.
     */
    @Test
    @DisplayName("findByCode retourne Optional vide si le code est inconnu")
    void findByCode_devrait_retourner_vide_si_inexistant() {
        // Act
        Optional<Billet> result = billetRepo.findByCode("code-inexistant");

        // Assert
        assertTrue(result.isEmpty());
    }

    /**
     * Verifies that {@link BilletRepository#findByCode(String)} also loads the
     * associated {@link Session} via the JOIN query.
     */
    @Test
    @DisplayName("findByCode inclut la session associée au billet")
    void findByCode_devrait_inclure_la_session_associee() {
        // Arrange
        billetRepo.save(billet("code-004", StatutBillet.VALIDE));

        // Act
        Optional<Billet> result = billetRepo.findByCode("code-004");

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getSession());
        assertEquals(sessionTest.getNom(), result.get().getSession().getNom());
    }


    /**
     * Verifies that {@link BilletRepository#findBySession(Session)} returns all
     * tickets belonging to the given session.
     */
    @Test
    @DisplayName("findBySession retourne tous les billets de la session")
    void findBySession_devrait_retourner_tous_les_billets_de_la_session() {
        // Arrange
        billetRepo.save(billet("code-005", StatutBillet.VALIDE));
        billetRepo.save(billet("code-006", StatutBillet.VALIDE));
        billetRepo.save(billet("code-007", StatutBillet.UTILISE));

        // Act
        List<Billet> result = billetRepo.findBySession(sessionTest);

        // Assert
        assertEquals(3, result.size());
    }

    /**
     * Verifies that {@link BilletRepository#findBySession(Session)} returns an empty
     * list when no tickets have been generated for the session.
     */
    @Test
    @DisplayName("findBySession retourne une liste vide si aucun billet")
    void findBySession_devrait_retourner_liste_vide_si_aucun_billet() {
        // Act
        List<Billet> result = billetRepo.findBySession(sessionTest);

        // Assert
        assertTrue(result.isEmpty());
    }


    /**
     * Verifies that {@link BilletRepository#update(Billet)} correctly persists
     * a status change to {@link StatutBillet#UTILISE}.
     */
    @Test
    @DisplayName("update modifie le statut vers UTILISE")
    void update_devrait_modifier_statut_vers_utilise() {
        // Arrange
        Billet b = billet("code-008", StatutBillet.VALIDE);
        billetRepo.save(b);

        // Act
        b.setStatut(StatutBillet.UTILISE);
        billetRepo.update(b);

        // Assert
        Optional<Billet> result = billetRepo.findByCode("code-008");
        assertTrue(result.isPresent());
        assertEquals(StatutBillet.UTILISE, result.get().getStatut());
    }

    /**
     * Verifies that {@link BilletRepository#update(Billet)} correctly persists
     * a status change to {@link StatutBillet#EXPIRE}.
     */
    @Test
    @DisplayName("update modifie le statut vers EXPIRE")
    void update_devrait_modifier_statut_vers_expire() {
        // Arrange
        Billet b = billet("code-009", StatutBillet.VALIDE);
        billetRepo.save(b);

        // Act
        b.setStatut(StatutBillet.EXPIRE);
        billetRepo.update(b);

        // Assert
        Optional<Billet> result = billetRepo.findByCode("code-009");
        assertTrue(result.isPresent());
        assertEquals(StatutBillet.EXPIRE, result.get().getStatut());
    }


    /**
     * Verifies that {@link BilletRepository#delete(int)} removes the ticket
     * from the database.
     */
    @Test
    @DisplayName("delete supprime le billet")
    void delete_devrait_supprimer_le_billet() {
        // Arrange
        Billet b = billet("code-010", StatutBillet.VALIDE);
        billetRepo.save(b);
        int id = b.getId();

        // Act
        billetRepo.delete(id);

        // Assert
        assertTrue(billetRepo.findByCode("code-010").isEmpty());
    }

    /**
     * Verifies that {@link BilletRepository#delete(int)} does not throw an exception
     * when called with a non-existent id.
     */
    @Test
    @DisplayName("delete sur un billet inexistant ne lève pas d'exception")
    void delete_billet_inexistant_ne_devrait_pas_lever_dexception() {
        // Act & Assert
        assertDoesNotThrow(() -> billetRepo.delete(9999));
    }


    /**
     * Verifies that {@link BilletRepository#findAll()} returns all persisted tickets.
     */
    @Test
    @DisplayName("findAll retourne tous les billets")
    void findAll_devrait_retourner_tous_les_billets() {
        // Arrange
        billetRepo.save(billet("code-011", StatutBillet.VALIDE));
        billetRepo.save(billet("code-012", StatutBillet.UTILISE));

        // Act
        List<Billet> result = billetRepo.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    /**
     * Verifies that {@link BilletRepository#findAll()} returns an empty list
     * when no tickets have been persisted.
     */
    @Test
    @DisplayName("findAll retourne une liste vide si aucun billet")
    void findAll_devrait_retourner_liste_vide_si_aucun_billet() {
        // Act
        List<Billet> result = billetRepo.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }


    /**
     * Helper factory method to create a {@link Billet} linked to {@link #sessionTest}
     * with a fixed expiration date of 2026-12-31.
     *
     * @param code   the unique ticket code
     * @param statut the initial status
     * @return a new unsaved {@link Billet}
     */
    private Billet billet(String code, StatutBillet statut) {
        return new Billet(code, statut, LocalDate.of(2026, 12, 31), sessionTest);
    }
}
