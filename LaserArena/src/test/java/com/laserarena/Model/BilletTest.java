package com.laserarena.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Billet} model class.
 * <p>
 * Covers constructors, getters and setters, the relationship between
 * a {@link Billet} and its parent {@link Session}, and the
 * {@link StatutBillet} enumeration.
 * All tests follow the AAA (Arrange / Act / Assert) pattern.
 * </p>
 */
@DisplayName("Modèle Billet")
class BilletTest {

    private Session session;
    private Billet  billet;

    /**
     * Creates a reference {@link Session} and a default {@link Billet}
     * reused across tests.
     */
    @BeforeEach
    void setUp() {
        session = new Session("Session Test", TypePartie.ELIMINATION, 30, 10, StatutSession.OUVERTE);
        session.setId(1);
        billet = new Billet("code-uuid-123", StatutBillet.VALIDE, LocalDate.of(2025, 12, 31), session);
    }


    /**
     * Tests related to {@link Billet} constructors.
     */
    @Nested
    @DisplayName("Constructeur")
    class ConstructeurTest {

        /**
         * Verifies that the no-arg constructor produces an empty {@link Billet}
         * with all fields at their default values.
         */
        @Test
        @DisplayName("Le constructeur par défaut crée un billet vide")
        void constructeurParDefaut() {
            // Act
            Billet b = new Billet();

            // Assert
            assertNull(b.getCode());
            assertNull(b.getStatut());
            assertNull(b.getDateExpiration());
            assertNull(b.getSession());
            assertEquals(0, b.getId());
        }

        /**
         * Verifies that the parameterized constructor correctly initializes
         * all provided fields.
         */
        @Test
        @DisplayName("Le constructeur paramétré initialise tous les champs")
        void constructeurParametre() {
            // Assert
            assertEquals("code-uuid-123",              billet.getCode());
            assertEquals(StatutBillet.VALIDE,           billet.getStatut());
            assertEquals(LocalDate.of(2025, 12, 31),   billet.getDateExpiration());
            assertSame(session,                         billet.getSession());
        }
    }

    /**
     * Tests for getters and setters of the {@link Billet} model.
     */
    @Nested
    @DisplayName("Getters et Setters")
    class GettersSettersTest {

        /**
         * Verifies that {@link Billet#setId(int)} correctly updates the id.
         */
        @Test
        @DisplayName("setId / getId")
        void idGetterSetter() {
            // Act
            billet.setId(99);

            // Assert
            assertEquals(99, billet.getId());
        }

        /**
         * Verifies that {@link Billet#setCode(String)} correctly updates the ticket code.
         */
        @Test
        @DisplayName("setCode / getCode")
        void codeGetterSetter() {
            // Act
            billet.setCode("nouveau-code");

            // Assert
            assertEquals("nouveau-code", billet.getCode());
        }

        /**
         * Verifies that the status can be set to {@link StatutBillet#VALIDE}.
         */
        @Test
        @DisplayName("setStatut VALIDE / getStatut")
        void statutValide() {
            // Act
            billet.setStatut(StatutBillet.VALIDE);

            // Assert
            assertEquals(StatutBillet.VALIDE, billet.getStatut());
        }

        /**
         * Verifies that the status can be updated to {@link StatutBillet#UTILISE}
         * after a ticket is scanned at the arena entrance.
         */
        @Test
        @DisplayName("setStatut UTILISE / getStatut")
        void statutUtilise() {
            // Act
            billet.setStatut(StatutBillet.UTILISE);

            // Assert
            assertEquals(StatutBillet.UTILISE, billet.getStatut());
        }

        /**
         * Verifies that the status can be updated to {@link StatutBillet#EXPIRE}
         * when the expiration date has passed.
         */
        @Test
        @DisplayName("setStatut EXPIRE / getStatut")
        void statutExpire() {
            // Act
            billet.setStatut(StatutBillet.EXPIRE);

            // Assert
            assertEquals(StatutBillet.EXPIRE, billet.getStatut());
        }

        /**
         * Verifies that {@link Billet#setDateExpiration(LocalDate)} correctly updates
         * the expiration date.
         */
        @Test
        @DisplayName("setDateExpiration / getDateExpiration")
        void dateExpirationGetterSetter() {
            // Arrange
            LocalDate nouvelleDate = LocalDate.of(2026, 6, 15);

            // Act
            billet.setDateExpiration(nouvelleDate);

            // Assert
            assertEquals(nouvelleDate, billet.getDateExpiration());
        }

        /**
         * Verifies that {@link Billet#setSession(Session)} correctly associates
         * a different session with the ticket.
         */
        @Test
        @DisplayName("setSession / getSession — associe correctement la session")
        void sessionGetterSetter() {
            // Arrange
            Session autreSession = new Session("Autre", TypePartie.EQUIPES, 15, 8, StatutSession.OUVERTE);
            autreSession.setId(2);

            // Act
            billet.setSession(autreSession);

            // Assert
            assertSame(autreSession, billet.getSession());
            assertEquals(2, billet.getSession().getId());
        }
    }

    /**
     * Tests for the relationship between a {@link Billet} and its parent {@link Session}.
     */
    @Nested
    @DisplayName("Relation Billet — Session")
    class RelationTest {

        /**
         * Verifies that a ticket can retrieve its associated session's id.
         */
        @Test
        @DisplayName("Le billet connaît l'id de sa session")
        void billetConnaitSessionId() {
            // Assert
            assertEquals(1, billet.getSession().getId());
        }

        /**
         * Verifies that a ticket can retrieve its associated session's name.
         */
        @Test
        @DisplayName("Le billet connaît le nom de sa session")
        void billetConnaitSessionNom() {
            // Assert
            assertEquals("Session Test", billet.getSession().getNom());
        }

        /**
         * Verifies that the ticket's expiration date can be aligned
         * with the session's expiration date.
         */
        @Test
        @DisplayName("La date d'expiration du billet peut correspondre à celle de la session")
        void dateExpirationCoherente() {
            // Arrange
            LocalDate exp = LocalDate.of(2025, 12, 31);
            session.setDateExpiration(exp);

            // Act
            billet.setDateExpiration(session.getDateExpiration());

            // Assert
            assertEquals(session.getDateExpiration(), billet.getDateExpiration());
        }
    }

    /**
     * Tests for the {@link StatutBillet} enumeration.
     */
    @Nested
    @DisplayName("Enum StatutBillet")
    class StatutBilletEnumTest {

        /** Verifies that {@link StatutBillet#VALIDE} is resolvable by name. */
        @Test
        @DisplayName("StatutBillet contient VALIDE")
        void statutValide() {
            assertEquals(StatutBillet.VALIDE, StatutBillet.valueOf("VALIDE"));
        }

        /** Verifies that {@link StatutBillet#UTILISE} is resolvable by name. */
        @Test
        @DisplayName("StatutBillet contient UTILISE")
        void statutUtilise() {
            assertEquals(StatutBillet.UTILISE, StatutBillet.valueOf("UTILISE"));
        }

        /** Verifies that {@link StatutBillet#EXPIRE} is resolvable by name. */
        @Test
        @DisplayName("StatutBillet contient EXPIRE")
        void statutExpire() {
            assertEquals(StatutBillet.EXPIRE, StatutBillet.valueOf("EXPIRE"));
        }

        /** Verifies that {@link StatutBillet} contains exactly 3 values. */
        @Test
        @DisplayName("StatutBillet a exactement 3 valeurs")
        void statutNombreValeurs() {
            assertEquals(3, StatutBillet.values().length);
        }
    }
}
