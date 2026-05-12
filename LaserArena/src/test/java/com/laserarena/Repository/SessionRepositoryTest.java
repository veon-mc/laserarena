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
 * Integration tests for {@link SessionRepository} using an in-memory SQLite database.
 * <p>
 * Each test runs against a fresh, isolated dataset. The in-memory connection is
 * shared across the test class via {@link DatabaseConnection#setInstanceForTest(Connection)},
 * and the tables are wiped before each test in {@link #setUp()}.
 * All tests follow the AAA (Arrange / Act / Assert) pattern.
 * </p>
 */
@DisplayName("Repository Session")
class SessionRepositoryTest {

    private static Connection        memoryConn;
    private        SessionRepository repo;

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
     * Clears all rows from the {@code session} and {@code billet} tables
     * and re-instantiates the repository before each test to guarantee isolation.
     *
     * @throws Exception if the DELETE statements fail
     */
    @BeforeEach
    void setUp() throws Exception {
        try (Statement stmt = memoryConn.createStatement()) {
            stmt.executeUpdate("DELETE FROM billet");
            stmt.executeUpdate("DELETE FROM session");
        }
        repo = new SessionRepository();
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
     * Verifies that {@link SessionRepository#save(Session)} assigns a positive
     * generated id to the persisted session.
     */
    @Test
    @DisplayName("save assigne un id généré positif")
    void save_devrait_assigner_un_id_genere() {
        // Arrange
        Session s = session("Soir", TypePartie.ELIMINATION, 30, 10);

        // Act
        repo.save(s);

        // Assert
        assertTrue(s.getId() > 0);
    }

    /**
     * Verifies that {@link SessionRepository#save(Session)} persists all fields
     * correctly, including the optional expiration date.
     */
    @Test
    @DisplayName("save persiste tous les champs de la session")
    void save_devrait_persister_tous_les_champs() {
        // Arrange
        Session s = session("Match équipes", TypePartie.EQUIPES, 45, 16);
        s.setDateExpiration(LocalDate.of(2026, 12, 31));

        // Act
        repo.save(s);
        Optional<Session> result = repo.findById(s.getId());

        // Assert
        assertTrue(result.isPresent());
        Session found = result.get();
        assertEquals("Match équipes",           found.getNom());
        assertEquals(TypePartie.EQUIPES,         found.getTypePartie());
        assertEquals(45,                         found.getDureeMinutes());
        assertEquals(16,                         found.getMaxJoueurs());
        assertEquals(StatutSession.OUVERTE,      found.getStatut());
        assertEquals(LocalDate.of(2026, 12, 31), found.getDateExpiration());
    }


    /**
     * Verifies that {@link SessionRepository#findById(int)} returns the correct
     * session when it exists.
     */
    @Test
    @DisplayName("findById retourne la session existante")
    void findById_devrait_retourner_session_existante() {
        // Arrange
        Session s = session("Capture", TypePartie.CAPTURE_DRAPEAU, 60, 20);
        repo.save(s);

        // Act
        Optional<Session> result = repo.findById(s.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Capture", result.get().getNom());
    }

    /**
     * Verifies that {@link SessionRepository#findById(int)} returns an empty
     * {@link Optional} when no session matches the given id.
     */
    @Test
    @DisplayName("findById retourne Optional vide si la session n'existe pas")
    void findById_devrait_retourner_vide_si_inexistant() {
        // Act
        Optional<Session> result = repo.findById(9999);

        // Assert
        assertTrue(result.isEmpty());
    }


    /**
     * Verifies that {@link SessionRepository#findAll()} returns all persisted sessions.
     */
    @Test
    @DisplayName("findAll retourne toutes les sessions")
    void findAll_devrait_retourner_toutes_les_sessions() {
        // Arrange
        repo.save(session("Session 1", TypePartie.ELIMINATION,     30, 10));
        repo.save(session("Session 2", TypePartie.EQUIPES,         45, 16));
        repo.save(session("Session 3", TypePartie.CAPTURE_DRAPEAU, 60, 20));

        // Act
        List<Session> result = repo.findAll();

        // Assert
        assertEquals(3, result.size());
    }

    /**
     * Verifies that {@link SessionRepository#findAll()} returns an empty list
     * when no sessions have been persisted.
     */
    @Test
    @DisplayName("findAll retourne une liste vide si aucune session")
    void findAll_devrait_retourner_liste_vide_si_aucune_session() {
        // Act
        List<Session> result = repo.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }


    /**
     * Verifies that {@link SessionRepository#update(Session)} correctly persists
     * a status change.
     */
    @Test
    @DisplayName("update modifie le statut de la session")
    void update_devrait_modifier_le_statut() {
        // Arrange
        Session s = session("A modifier", TypePartie.ELIMINATION, 30, 10);
        repo.save(s);

        // Act
        s.setStatut(StatutSession.TERMINEE);
        repo.update(s);

        // Assert
        Optional<Session> result = repo.findById(s.getId());
        assertTrue(result.isPresent());
        assertEquals(StatutSession.TERMINEE, result.get().getStatut());
    }

    /**
     * Verifies that {@link SessionRepository#update(Session)} correctly persists
     * a name change.
     */
    @Test
    @DisplayName("update modifie le nom de la session")
    void update_devrait_modifier_le_nom() {
        // Arrange
        Session s = session("Ancien nom", TypePartie.ELIMINATION, 30, 10);
        repo.save(s);

        // Act
        s.setNom("Nouveau nom");
        repo.update(s);

        // Assert
        Optional<Session> result = repo.findById(s.getId());
        assertTrue(result.isPresent());
        assertEquals("Nouveau nom", result.get().getNom());
    }


    /**
     * Verifies that {@link SessionRepository#delete(int)} removes the session
     * from the database.
     */
    @Test
    @DisplayName("delete supprime la session")
    void delete_devrait_supprimer_la_session() {
        // Arrange
        Session s = session("A supprimer", TypePartie.ELIMINATION, 30, 10);
        repo.save(s);
        int id = s.getId();

        // Act
        repo.delete(id);

        // Assert
        assertTrue(repo.findById(id).isEmpty());
    }

    /**
     * Verifies that {@link SessionRepository#delete(int)} does not throw an exception
     * when called with a non-existent id.
     */
    @Test
    @DisplayName("delete sur une session inexistante ne lève pas d'exception")
    void delete_session_inexistante_ne_devrait_pas_lever_dexception() {
        // Act & Assert
        assertDoesNotThrow(() -> repo.delete(9999));
    }


    /**
     * Verifies that {@link SessionRepository#countBillets(int)} returns {@code 0}
     * when no tickets have been generated for the session.
     */
    @Test
    @DisplayName("countBillets retourne 0 si aucun billet")
    void countBillets_devrait_retourner_zero_si_aucun_billet() {
        // Arrange
        Session s = session("Sans billets", TypePartie.ELIMINATION, 30, 10);
        repo.save(s);

        // Act
        int count = repo.countBillets(s.getId());

        // Assert
        assertEquals(0, count);
    }


    /**
     * Helper factory method to create a {@link Session} with {@link StatutSession#OUVERTE}.
     *
     * @param nom   the session name
     * @param type  the game type
     * @param duree the duration in minutes
     * @param max   the maximum number of players
     * @return a new unsaved {@link Session}
     */
    private Session session(String nom, TypePartie type, int duree, int max) {
        return new Session(nom, type, duree, max, StatutSession.OUVERTE);
    }
}
