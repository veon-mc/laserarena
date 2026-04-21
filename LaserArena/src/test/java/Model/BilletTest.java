package Model;

import com.laserarena.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Modèle Billet")
class BilletTest {

    private Session session;
    private Billet billet;

    @BeforeEach
    void setUp() {
        session = new Session("Session Test", TypePartie.ELIMINATION, 30, 10, StatutSession.OUVERTE);
        session.setId(1);
        billet  = new Billet("code-uuid-123", StatutBillet.VALIDE, LocalDate.of(2025, 12, 31), session);
    }

    // ------------------------------------------------------------------ //
    //  Constructeur                                                        //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Constructeur")
    class ConstructeurTest {

        @Test
        @DisplayName("Le constructeur par défaut crée un billet vide")
        void constructeurParDefaut() {
            Billet b = new Billet();
            assertNull(b.getCode());
            assertNull(b.getStatut());
            assertNull(b.getDateExpiration());
            assertNull(b.getSession());
            assertEquals(0, b.getId());
        }

        @Test
        @DisplayName("Le constructeur paramétré initialise tous les champs")
        void constructeurParametre() {
            assertEquals("code-uuid-123", billet.getCode());
            assertEquals(StatutBillet.VALIDE, billet.getStatut());
            assertEquals(LocalDate.of(2025, 12, 31), billet.getDateExpiration());
            assertSame(session, billet.getSession());
        }
    }

    // ------------------------------------------------------------------ //
    //  Getters / Setters                                                   //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Getters et Setters")
    class GettersSettersTest {

        @Test
        @DisplayName("setId / getId")
        void idGetterSetter() {
            billet.setId(99);
            assertEquals(99, billet.getId());
        }

        @Test
        @DisplayName("setCode / getCode")
        void codeGetterSetter() {
            billet.setCode("nouveau-code");
            assertEquals("nouveau-code", billet.getCode());
        }

        @Test
        @DisplayName("setStatut VALIDE / getStatut")
        void statutValide() {
            billet.setStatut(StatutBillet.VALIDE);
            assertEquals(StatutBillet.VALIDE, billet.getStatut());
        }

        @Test
        @DisplayName("setStatut UTILISE / getStatut")
        void statutUtilise() {
            billet.setStatut(StatutBillet.UTILISE);
            assertEquals(StatutBillet.UTILISE, billet.getStatut());
        }

        @Test
        @DisplayName("setStatut EXPIRE / getStatut")
        void statutExpire() {
            billet.setStatut(StatutBillet.EXPIRE);
            assertEquals(StatutBillet.EXPIRE, billet.getStatut());
        }

        @Test
        @DisplayName("setDateExpiration / getDateExpiration")
        void dateExpirationGetterSetter() {
            LocalDate nouvellDate = LocalDate.of(2026, 6, 15);
            billet.setDateExpiration(nouvellDate);
            assertEquals(nouvellDate, billet.getDateExpiration());
        }

        @Test
        @DisplayName("setSession / getSession — associe correctement la session")
        void sessionGetterSetter() {
            Session autreSession = new Session("Autre", TypePartie.EQUIPES, 15, 8, StatutSession.OUVERTE);
            autreSession.setId(2);
            billet.setSession(autreSession);
            assertSame(autreSession, billet.getSession());
            assertEquals(2, billet.getSession().getId());
        }
    }

    // ------------------------------------------------------------------ //
    //  Lien Billet ↔ Session                                              //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Relation Billet — Session")
    class RelationTest {

        @Test
        @DisplayName("Le billet connaît l'id de sa session")
        void billetConnaitSessionId() {
            assertEquals(1, billet.getSession().getId());
        }

        @Test
        @DisplayName("Le billet connaît le nom de sa session")
        void billetConnaitSessionNom() {
            assertEquals("Session Test", billet.getSession().getNom());
        }

        @Test
        @DisplayName("La date d'expiration du billet peut correspondre à celle de la session")
        void dateExpirationCohérente() {
            LocalDate exp = LocalDate.of(2025, 12, 31);
            session.setDateExpiration(exp);
            billet.setDateExpiration(session.getDateExpiration());
            assertEquals(session.getDateExpiration(), billet.getDateExpiration());
        }
    }

    // ------------------------------------------------------------------ //
    //  Enum StatutBillet                                                   //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Enum StatutBillet")
    class StatutBilletTest {

        @Test
        @DisplayName("StatutBillet contient VALIDE")
        void statutValide() {
            assertEquals(StatutBillet.VALIDE, StatutBillet.valueOf("VALIDE"));
        }

        @Test
        @DisplayName("StatutBillet contient UTILISE")
        void statutUtilise() {
            assertEquals(StatutBillet.UTILISE, StatutBillet.valueOf("UTILISE"));
        }

        @Test
        @DisplayName("StatutBillet contient EXPIRE")
        void statutExpire() {
            assertEquals(StatutBillet.EXPIRE, StatutBillet.valueOf("EXPIRE"));
        }

        @Test
        @DisplayName("StatutBillet a exactement 3 valeurs")
        void statutNombreValeurs() {
            assertEquals(3, StatutBillet.values().length);
        }
    }

    // ------------------------------------------------------------------ //
    //  Modèle QRCodeBillet                                                 //
    // ------------------------------------------------------------------ //
    @Nested
    @DisplayName("Modèle QRCodeBillet")
    class QRCodeBilletTest {

        @Test
        @DisplayName("Constructeur par défaut crée un objet vide")
        void constructeurParDefaut() {
            QRCodeBillet qr = new QRCodeBillet();
            assertEquals(0, qr.getId());
            assertNull(qr.getImageBytes());
            assertNull(qr.getBillet());
        }

        @Test
        @DisplayName("Constructeur paramétré initialise imageBytes et billet")
        void constructeurParametre() {
            byte[] img = {1, 2, 3};
            QRCodeBillet qr = new QRCodeBillet(img, billet);
            assertArrayEquals(img, qr.getImageBytes());
            assertSame(billet, qr.getBillet());
        }

        @Test
        @DisplayName("setId / getId")
        void idGetterSetter() {
            QRCodeBillet qr = new QRCodeBillet();
            qr.setId(7);
            assertEquals(7, qr.getId());
        }

        @Test
        @DisplayName("setImageBytes / getImageBytes")
        void imageBytesGetterSetter() {
            byte[] img = {10, 20, 30};
            QRCodeBillet qr = new QRCodeBillet();
            qr.setImageBytes(img);
            assertArrayEquals(img, qr.getImageBytes());
        }

        @Test
        @DisplayName("setBillet / getBillet")
        void billetGetterSetter() {
            QRCodeBillet qr = new QRCodeBillet();
            qr.setBillet(billet);
            assertSame(billet, qr.getBillet());
        }
    }
}
