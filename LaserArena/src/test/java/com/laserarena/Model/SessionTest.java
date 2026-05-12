package com.laserarena.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Session} model class.
 * <p>
 * Covers constructors, getters and setters, team-related fields,
 * and the {@link TypePartie} and {@link StatutSession} enums.
 * All tests follow the AAA (Arrange / Act / Assert) pattern.
 * </p>
 */
@DisplayName("Modèle Session")
class SessionTest {

    private Session session;

    /**
     * Creates a default {@link Session} instance reused across tests.
     */
    @BeforeEach
    void setUp() {
        session = new Session("Session Test", TypePartie.ELIMINATION, 30, 10, StatutSession.OUVERTE);
    }

    /**
     * Tests related to {@link Session} constructors.
     */
    @Nested
    @DisplayName("Constructeur")
    class ConstructeurTest {

        /**
         * Verifies that the no-arg constructor produces an empty {@link Session}
         * with all fields at their default values.
         */
        @Test
        @DisplayName("Le constructeur par défaut crée une session vide")
        void constructeurParDefaut() {
            // Act
            Session s = new Session();

            // Assert
            assertNull(s.getNom());
            assertNull(s.getTypePartie());
            assertNull(s.getStatut());
            assertEquals(0, s.getId());
            assertEquals(0, s.getDureeMinutes());
            assertEquals(0, s.getMaxJoueurs());
        }

        /**
         * Verifies that the parameterized constructor correctly initializes
         * all provided fields.
         */
        @Test
        @DisplayName("Le constructeur paramétré initialise tous les champs")
        void constructeurParametre() {
            // Assert
            assertEquals("Session Test",         session.getNom());
            assertEquals(TypePartie.ELIMINATION, session.getTypePartie());
            assertEquals(30,                     session.getDureeMinutes());
            assertEquals(10,                     session.getMaxJoueurs());
            assertEquals(StatutSession.OUVERTE,  session.getStatut());
        }

        /**
         * Verifies that the id defaults to {@code 0} after construction,
         * before any database insertion.
         */
        @Test
        @DisplayName("L'id est 0 par défaut après construction")
        void idParDefautEstZero() {
            // Assert
            assertEquals(0, session.getId());
        }
    }

    /**
     * Tests for basic getters and setters of the {@link Session} model.
     */
    @Nested
    @DisplayName("Getters et Setters")
    class GettersSettersTest {

        /**
         * Verifies that {@link Session#setId(int)} correctly updates the id.
         */
        @Test
        @DisplayName("setId / getId")
        void idGetterSetter() {
            // Act
            session.setId(42);

            // Assert
            assertEquals(42, session.getId());
        }

        /**
         * Verifies that {@link Session#setNom(String)} correctly updates the name.
         */
        @Test
        @DisplayName("setNom / getNom")
        void nomGetterSetter() {
            // Act
            session.setNom("Nouvelle session");

            // Assert
            assertEquals("Nouvelle session", session.getNom());
        }

        /**
         * Verifies that the game type can be updated to {@link TypePartie#EQUIPES}.
         */
        @Test
        @DisplayName("setTypePartie / getTypePartie — EQUIPES")
        void typePartieEquipes() {
            // Act
            session.setTypePartie(TypePartie.EQUIPES);

            // Assert
            assertEquals(TypePartie.EQUIPES, session.getTypePartie());
        }

        /**
         * Verifies that the game type can be updated to {@link TypePartie#CAPTURE_DRAPEAU}.
         */
        @Test
        @DisplayName("setTypePartie / getTypePartie — CAPTURE_DRAPEAU")
        void typePartieCapturedrapeau() {
            // Act
            session.setTypePartie(TypePartie.CAPTURE_DRAPEAU);

            // Assert
            assertEquals(TypePartie.CAPTURE_DRAPEAU, session.getTypePartie());
        }

        /**
         * Verifies that {@link Session#setDureeMinutes(int)} correctly updates the duration.
         */
        @Test
        @DisplayName("setDureeMinutes / getDureeMinutes")
        void dureeGetterSetter() {
            // Act
            session.setDureeMinutes(60);

            // Assert
            assertEquals(60, session.getDureeMinutes());
        }

        /**
         * Verifies that {@link Session#setMaxJoueurs(int)} correctly updates the max player count.
         */
        @Test
        @DisplayName("setMaxJoueurs / getMaxJoueurs")
        void maxJoueursGetterSetter() {
            // Act
            session.setMaxJoueurs(20);

            // Assert
            assertEquals(20, session.getMaxJoueurs());
        }

        /**
         * Verifies that the session status can be updated to {@link StatutSession#TERMINEE}.
         */
        @Test
        @DisplayName("setStatut / getStatut — TERMINEE")
        void statutTerminee() {
            // Act
            session.setStatut(StatutSession.TERMINEE);

            // Assert
            assertEquals(StatutSession.TERMINEE, session.getStatut());
        }

        /**
         * Verifies that the session status can be updated to {@link StatutSession#EN_COURS}.
         */
        @Test
        @DisplayName("setStatut / getStatut — EN_COURS")
        void statutEnCours() {
            // Act
            session.setStatut(StatutSession.EN_COURS);

            // Assert
            assertEquals(StatutSession.EN_COURS, session.getStatut());
        }

        /**
         * Verifies that {@link Session#setDateExpiration(LocalDate)} correctly updates the date.
         */
        @Test
        @DisplayName("setDateExpiration / getDateExpiration")
        void dateExpirationGetterSetter() {
            // Arrange
            LocalDate date = LocalDate.of(2025, 12, 31);

            // Act
            session.setDateExpiration(date);

            // Assert
            assertEquals(date, session.getDateExpiration());
        }

        /**
         * Verifies that the expiration date is {@code null} by default
         * when using the no-arg constructor.
         */
        @Test
        @DisplayName("getDateExpiration retourne null si non définie")
        void dateExpirationNullParDefaut() {
            // Arrange
            Session s = new Session();

            // Assert
            assertNull(s.getDateExpiration());
        }
    }


    /**
     * Tests for team-related fields used in {@link TypePartie#EQUIPES}
     * and {@link TypePartie#CAPTURE_DRAPEAU} sessions.
     */
    @Nested
    @DisplayName("Gestion des équipes")
    class EquipesTest {

        /**
         * Verifies that {@link Session#setNombreEquipes(int)} correctly updates the team count.
         */
        @Test
        @DisplayName("setNombreEquipes / getNombreEquipes")
        void nombreEquipesGetterSetter() {
            // Act
            session.setNombreEquipes(4);

            // Assert
            assertEquals(4, session.getNombreEquipes());
        }

        /**
         * Verifies that {@link Session#setJoueursParEquipe(int)} correctly updates
         * the players-per-team value.
         */
        @Test
        @DisplayName("setJoueursParEquipe / getJoueursParEquipe")
        void joueursParEquipeGetterSetter() {
            // Act
            session.setJoueursParEquipe(5);

            // Assert
            assertEquals(5, session.getJoueursParEquipe());
        }

        /**
         * Verifies that {@link Session#setNomsEquipes(String)} correctly updates
         * the comma-separated team names string.
         */
        @Test
        @DisplayName("setNomsEquipes / getNomsEquipes")
        void nomsEquipesGetterSetter() {
            // Act
            session.setNomsEquipes("Alpha,Beta,Gamma");

            // Assert
            assertEquals("Alpha,Beta,Gamma", session.getNomsEquipes());
        }

        /**
         * Verifies that the total player count equals {@code nombreEquipes * joueursParEquipe}.
         */
        @Test
        @DisplayName("Le total de joueurs = nombreEquipes * joueursParEquipe")
        void totalJoueursCoherent() {
            // Arrange
            session.setNombreEquipes(3);
            session.setJoueursParEquipe(4);

            // Act
            session.setMaxJoueurs(session.getNombreEquipes() * session.getJoueursParEquipe());

            // Assert
            assertEquals(12, session.getMaxJoueurs());
        }

        /**
         * Verifies that {@link Session#getNombreEquipes()} returns {@code 0} by default.
         */
        @Test
        @DisplayName("getNombreEquipes vaut 0 par défaut")
        void nombreEquipesDefaut() {
            // Arrange
            Session s = new Session();

            // Assert
            assertEquals(0, s.getNombreEquipes());
        }

        /**
         * Verifies that {@link Session#getNomsEquipes()} returns {@code null} by default.
         */
        @Test
        @DisplayName("getNomsEquipes est null par défaut")
        void nomsEquipesDefaut() {
            // Arrange
            Session s = new Session();

            // Assert
            assertNull(s.getNomsEquipes());
        }
    }


    /**
     * Tests for the {@link TypePartie} enumeration.
     */
    @Nested
    @DisplayName("Enum TypePartie")
    class TypePartieTest {

        /** Verifies that {@link TypePartie#ELIMINATION} is resolvable by name. */
        @Test
        @DisplayName("TypePartie contient ELIMINATION")
        void typePartieElimination() {
            assertEquals(TypePartie.ELIMINATION, TypePartie.valueOf("ELIMINATION"));
        }

        /** Verifies that {@link TypePartie#EQUIPES} is resolvable by name. */
        @Test
        @DisplayName("TypePartie contient EQUIPES")
        void typePartieEquipes() {
            assertEquals(TypePartie.EQUIPES, TypePartie.valueOf("EQUIPES"));
        }

        /** Verifies that {@link TypePartie#CAPTURE_DRAPEAU} is resolvable by name. */
        @Test
        @DisplayName("TypePartie contient CAPTURE_DRAPEAU")
        void typePartieCapturedrapeau() {
            assertEquals(TypePartie.CAPTURE_DRAPEAU, TypePartie.valueOf("CAPTURE_DRAPEAU"));
        }

        /** Verifies that {@link TypePartie} contains exactly 3 values. */
        @Test
        @DisplayName("TypePartie a exactement 3 valeurs")
        void typePartieNombreValeurs() {
            assertEquals(3, TypePartie.values().length);
        }
    }


    /**
     * Tests for the {@link StatutSession} enumeration.
     */
    @Nested
    @DisplayName("Enum StatutSession")
    class StatutSessionTest {

        /** Verifies that {@link StatutSession#OUVERTE} is resolvable by name. */
        @Test
        @DisplayName("StatutSession contient OUVERTE")
        void statutOuverte() {
            assertEquals(StatutSession.OUVERTE, StatutSession.valueOf("OUVERTE"));
        }

        /** Verifies that {@link StatutSession#TERMINEE} is resolvable by name. */
        @Test
        @DisplayName("StatutSession contient TERMINEE")
        void statutTerminee() {
            assertEquals(StatutSession.TERMINEE, StatutSession.valueOf("TERMINEE"));
        }

        /** Verifies that {@link StatutSession#EN_COURS} is resolvable by name. */
        @Test
        @DisplayName("StatutSession contient EN_COURS")
        void statutEnCours() {
            assertEquals(StatutSession.EN_COURS, StatutSession.valueOf("EN_COURS"));
        }

        /** Verifies that {@link StatutSession} contains exactly 3 values. */
        @Test
        @DisplayName("StatutSession a exactement 3 valeurs")
        void statutNombreValeurs() {
            assertEquals(3, StatutSession.values().length);
        }
    }
}
