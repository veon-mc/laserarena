package com.laserarena.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Modèle Session")
class SessionTest {

    private Session session;

    @BeforeEach
    void setUp() {
        session = new Session("Session Test", TypePartie.ELIMINATION, 30, 10, StatutSession.OUVERTE);
    }

    // ------------------------------------------------------------------ //
    //  Constructeur                                                        //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Constructeur")
    class ConstructeurTest {

        @Test
        @DisplayName("Le constructeur par défaut crée une session vide")
        void constructeurParDefaut() {
            Session s = new Session();
            assertNull(s.getNom());
            assertNull(s.getTypePartie());
            assertNull(s.getStatut());
            assertEquals(0, s.getId());
            assertEquals(0, s.getDureeMinutes());
            assertEquals(0, s.getMaxJoueurs());
        }

        @Test
        @DisplayName("Le constructeur paramétré initialise tous les champs")
        void constructeurParametre() {
            assertEquals("Session Test", session.getNom());
            assertEquals(TypePartie.ELIMINATION, session.getTypePartie());
            assertEquals(30, session.getDureeMinutes());
            assertEquals(10, session.getMaxJoueurs());
            assertEquals(StatutSession.OUVERTE, session.getStatut());
        }

        @Test
        @DisplayName("L'id est 0 par défaut après construction")
        void idParDefautEstZero() {
            assertEquals(0, session.getId());
        }
    }

    // ------------------------------------------------------------------ //
    //  Getters / Setters de base                                          //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Getters et Setters")
    class GettersSettersTest {

        @Test
        @DisplayName("setId / getId")
        void idGetterSetter() {
            session.setId(42);
            assertEquals(42, session.getId());
        }

        @Test
        @DisplayName("setNom / getNom")
        void nomGetterSetter() {
            session.setNom("Nouvelle session");
            assertEquals("Nouvelle session", session.getNom());
        }

        @Test
        @DisplayName("setTypePartie / getTypePartie — EQUIPES")
        void typePartieEquipes() {
            session.setTypePartie(TypePartie.EQUIPES);
            assertEquals(TypePartie.EQUIPES, session.getTypePartie());
        }

        @Test
        @DisplayName("setTypePartie / getTypePartie — CAPTURE_DRAPEAU")
        void typePartieCapturedrapeau() {
            session.setTypePartie(TypePartie.CAPTURE_DRAPEAU);
            assertEquals(TypePartie.CAPTURE_DRAPEAU, session.getTypePartie());
        }

        @Test
        @DisplayName("setDureeMinutes / getDureeMinutes")
        void dureeGetterSetter() {
            session.setDureeMinutes(60);
            assertEquals(60, session.getDureeMinutes());
        }

        @Test
        @DisplayName("setMaxJoueurs / getMaxJoueurs")
        void maxJoueursGetterSetter() {
            session.setMaxJoueurs(20);
            assertEquals(20, session.getMaxJoueurs());
        }

        @Test
        @DisplayName("setStatut / getStatut — TERMINEE")
        void statutTerminee() {
            session.setStatut(StatutSession.TERMINEE);
            assertEquals(StatutSession.TERMINEE, session.getStatut());
        }

        @Test
        @DisplayName("setStatut / getStatut — EN_COURS")
        void statutEnCours() {
            session.setStatut(StatutSession.EN_COURS);
            assertEquals(StatutSession.EN_COURS, session.getStatut());
        }

        @Test
        @DisplayName("setDateExpiration / getDateExpiration")
        void dateExpirationGetterSetter() {
            LocalDate date = LocalDate.of(2025, 12, 31);
            session.setDateExpiration(date);
            assertEquals(date, session.getDateExpiration());
        }

        @Test
        @DisplayName("getDateExpiration retourne null si non définie")
        void dateExpirationNullParDefaut() {
            Session s = new Session();
            assertNull(s.getDateExpiration());
        }
    }

    // ------------------------------------------------------------------ //
    //  Champs équipes                                                      //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Gestion des équipes")
    class EquipesTest {

        @Test
        @DisplayName("setNombreEquipes / getNombreEquipes")
        void nombreEquipesGetterSetter() {
            session.setNombreEquipes(4);
            assertEquals(4, session.getNombreEquipes());
        }

        @Test
        @DisplayName("setJoueursParEquipe / getJoueursParEquipe")
        void joueursParEquipeGetterSetter() {
            session.setJoueursParEquipe(5);
            assertEquals(5, session.getJoueursParEquipe());
        }

        @Test
        @DisplayName("setNomsEquipes / getNomsEquipes")
        void nomsEquipesGetterSetter() {
            session.setNomsEquipes("Alpha,Beta,Gamma");
            assertEquals("Alpha,Beta,Gamma", session.getNomsEquipes());
        }

        @Test
        @DisplayName("Le total de joueurs = nombreEquipes * joueursParEquipe")
        void totalJoueursCoherent() {
            session.setNombreEquipes(3);
            session.setJoueursParEquipe(4);
            session.setMaxJoueurs(session.getNombreEquipes() * session.getJoueursParEquipe());
            assertEquals(12, session.getMaxJoueurs());
        }

        @Test
        @DisplayName("getNombreEquipes vaut 0 par défaut")
        void nombreEquipesDefaut() {
            Session s = new Session();
            assertEquals(0, s.getNombreEquipes());
        }

        @Test
        @DisplayName("getNomsEquipes est null par défaut")
        void nomsEquipesDefaut() {
            Session s = new Session();
            assertNull(s.getNomsEquipes());
        }
    }

    // ------------------------------------------------------------------ //
    //  Enum TypePartie                                                     //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Enum TypePartie")
    class TypePartieTest {

        @Test
        @DisplayName("TypePartie contient ELIMINATION")
        void typePartieElimination() {
            assertEquals(TypePartie.ELIMINATION, TypePartie.valueOf("ELIMINATION"));
        }

        @Test
        @DisplayName("TypePartie contient EQUIPES")
        void typePartieEquipes() {
            assertEquals(TypePartie.EQUIPES, TypePartie.valueOf("EQUIPES"));
        }

        @Test
        @DisplayName("TypePartie contient CAPTURE_DRAPEAU")
        void typePartieCapturedrapeau() {
            assertEquals(TypePartie.CAPTURE_DRAPEAU, TypePartie.valueOf("CAPTURE_DRAPEAU"));
        }

        @Test
        @DisplayName("TypePartie a exactement 3 valeurs")
        void typePartieNombreValeurs() {
            assertEquals(3, TypePartie.values().length);
        }
    }

    // ------------------------------------------------------------------ //
    //  Enum StatutSession                                                  //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Enum StatutSession")
    class StatutSessionTest {

        @Test
        @DisplayName("StatutSession contient OUVERTE")
        void statutOuverte() {
            assertEquals(StatutSession.OUVERTE, StatutSession.valueOf("OUVERTE"));
        }

        @Test
        @DisplayName("StatutSession contient TERMINEE")
        void statutTerminee() {
            assertEquals(StatutSession.TERMINEE, StatutSession.valueOf("TERMINEE"));
        }

        @Test
        @DisplayName("StatutSession contient EN_COURS")
        void statutEnCours() {
            assertEquals(StatutSession.EN_COURS, StatutSession.valueOf("EN_COURS"));
        }

        @Test
        @DisplayName("StatutSession a exactement 3 valeurs")
        void statutNombreValeurs() {
            assertEquals(3, StatutSession.values().length);
        }
    }
}
