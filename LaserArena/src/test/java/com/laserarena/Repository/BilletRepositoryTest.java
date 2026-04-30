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

@DisplayName("Repository Billet")
class BilletRepositoryTest {

    private static Connection        memoryConn;
    private        BilletRepository  billetRepo;
    private        SessionRepository sessionRepo;
    private        Session           sessionTest;

    // ------------------------------------------------------------------ //
    //  Initialisation : BDD SQLite en mémoire                             //
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
        // Vider les tables avant chaque test
        try (Statement stmt = memoryConn.createStatement()) {
            stmt.executeUpdate("DELETE FROM billet");
            stmt.executeUpdate("DELETE FROM session");
        }
        billetRepo  = new BilletRepository();
        sessionRepo = new SessionRepository();

        // Créer une session de référence pour les tests
        sessionTest = new Session("Session test", TypePartie.ELIMINATION, 30, 10, StatutSession.OUVERTE);
        sessionRepo.save(sessionTest);
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
        Billet b = billet("code-001", StatutBillet.VALIDE);

        // Act
        billetRepo.save(b);

        // Assert
        assertTrue(b.getId() > 0);
    }

    @Test
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

    // ------------------------------------------------------------------ //
    //  findByCode                                                          //
    // ------------------------------------------------------------------ //

    @Test
    void findByCode_devrait_retourner_billet_existant() {
        // Arrange
        billetRepo.save(billet("code-003", StatutBillet.VALIDE));

        // Act
        Optional<Billet> result = billetRepo.findByCode("code-003");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("code-003", result.get().getCode());
    }

    @Test
    void findByCode_devrait_retourner_vide_si_inexistant() {
        // Act
        Optional<Billet> result = billetRepo.findByCode("code-inexistant");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
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

    // ------------------------------------------------------------------ //
    //  findBySession                                                       //
    // ------------------------------------------------------------------ //

    @Test
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

    @Test
    void findBySession_devrait_retourner_liste_vide_si_aucun_billet() {
        // Act
        List<Billet> result = billetRepo.findBySession(sessionTest);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  update                                                              //
    // ------------------------------------------------------------------ //

    @Test
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

    @Test
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

    // ------------------------------------------------------------------ //
    //  delete                                                              //
    // ------------------------------------------------------------------ //

    @Test
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

    @Test
    void delete_billet_inexistant_ne_devrait_pas_lever_dexception() {
        // Act & Assert
        assertDoesNotThrow(() -> billetRepo.delete(9999));
    }

    // ------------------------------------------------------------------ //
    //  findAll                                                             //
    // ------------------------------------------------------------------ //

    @Test
    void findAll_devrait_retourner_tous_les_billets() {
        // Arrange
        billetRepo.save(billet("code-011", StatutBillet.VALIDE));
        billetRepo.save(billet("code-012", StatutBillet.UTILISE));

        // Act
        List<Billet> result = billetRepo.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findAll_devrait_retourner_liste_vide_si_aucun_billet() {
        // Act
        List<Billet> result = billetRepo.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Utilitaire                                                          //
    // ------------------------------------------------------------------ //

    private Billet billet(String code, StatutBillet statut) {
        return new Billet(code, statut, LocalDate.of(2026, 12, 31), sessionTest);
    }
}