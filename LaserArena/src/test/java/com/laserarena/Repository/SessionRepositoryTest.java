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

@DisplayName("Repository Session")
class SessionRepositoryTest {

    private static Connection      memoryConn;
    private        SessionRepository repo;

    // ------------------------------------------------------------------ //
    //  Initialisation : BDD SQLite en mémoire partagée entre tous les tests
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
        }
        DatabaseConnection.setInstanceForTest(memoryConn);
    }

    @BeforeEach
    void setUp() throws Exception {
        // Vider la table avant chaque test — isolation garantie
        try (Statement stmt = memoryConn.createStatement()) {
            stmt.executeUpdate("DELETE FROM billet");
            stmt.executeUpdate("DELETE FROM session");
        }
        repo = new SessionRepository();
    }

    @AfterAll
    static void tearDown() throws Exception {
        DatabaseConnection.resetInstance();
        if (memoryConn != null && !memoryConn.isClosed()) memoryConn.close();
    }

    // ------------------------------------------------------------------ //
    //  save                                                                //
    // ------------------------------------------------------------------ //

    @Test
    void save_devrait_assigner_un_id_genere() {
        // Arrange
        Session s = session("Soir", TypePartie.ELIMINATION, 30, 10);

        // Act
        repo.save(s);

        // Assert
        assertTrue(s.getId() > 0);
    }

    @Test
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

    // ------------------------------------------------------------------ //
    //  findById                                                            //
    // ------------------------------------------------------------------ //

    @Test
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

    @Test
    void findById_devrait_retourner_vide_si_inexistant() {
        // Act
        Optional<Session> result = repo.findById(9999);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  findAll                                                             //
    // ------------------------------------------------------------------ //

    @Test
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

    @Test
    void findAll_devrait_retourner_liste_vide_si_aucune_session() {
        // Act
        List<Session> result = repo.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  update                                                              //
    // ------------------------------------------------------------------ //

    @Test
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

    @Test
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

    // ------------------------------------------------------------------ //
    //  delete                                                              //
    // ------------------------------------------------------------------ //

    @Test
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

    @Test
    void delete_session_inexistante_ne_devrait_pas_lever_dexception() {
        // Act & Assert
        assertDoesNotThrow(() -> repo.delete(9999));
    }

    // ------------------------------------------------------------------ //
    //  countBillets                                                        //
    // ------------------------------------------------------------------ //

    @Test
    void countBillets_devrait_retourner_zero_si_aucun_billet() {
        // Arrange
        Session s = session("Sans billets", TypePartie.ELIMINATION, 30, 10);
        repo.save(s);

        // Act
        int count = repo.countBillets(s.getId());

        // Assert
        assertEquals(0, count);
    }

    // ------------------------------------------------------------------ //
    //  Utilitaire                                                          //
    // ------------------------------------------------------------------ //

    private Session session(String nom, TypePartie type, int duree, int max) {
        return new Session(nom, type, duree, max, StatutSession.OUVERTE);
    }
}