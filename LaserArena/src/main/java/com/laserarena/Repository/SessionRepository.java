package com.laserarena.Repository;

import com.laserarena.Model.Session;
import com.laserarena.Model.StatutSession;
import com.laserarena.Model.TypePartie;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionRepository {

    public void save(Session session) {
        String sql = """
            INSERT INTO session (nom, type_partie, duree_minutes, max_joueurs, statut, date_expiration,
                                 nombre_equipes, joueurs_par_equipe, noms_equipes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, session.getNom());
            stmt.setString(2, session.getTypePartie().name());
            stmt.setInt   (3, session.getDureeMinutes());
            stmt.setInt   (4, session.getMaxJoueurs());
            stmt.setString(5, session.getStatut().name());
            stmt.setString(6, session.getDateExpiration() != null
                    ? session.getDateExpiration().toString() : null);
            stmt.setInt   (7, session.getNombreEquipes());
            stmt.setInt   (8, session.getJoueursParEquipe());
            stmt.setString(9, session.getNomsEquipes() != null ? session.getNomsEquipes() : "");

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) session.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de la session", e);
        }
    }

    public void update(Session session) {
        String sql = """
            UPDATE session SET nom=?, type_partie=?, duree_minutes=?, max_joueurs=?, statut=?,
                               date_expiration=?, nombre_equipes=?, joueurs_par_equipe=?, noms_equipes=?
            WHERE id=?
        """;
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, session.getNom());
            stmt.setString(2, session.getTypePartie().name());
            stmt.setInt   (3, session.getDureeMinutes());
            stmt.setInt   (4, session.getMaxJoueurs());
            stmt.setString(5, session.getStatut().name());
            stmt.setString(6, session.getDateExpiration() != null
                    ? session.getDateExpiration().toString() : null);
            stmt.setInt   (7, session.getNombreEquipes());
            stmt.setInt   (8, session.getJoueursParEquipe());
            stmt.setString(9, session.getNomsEquipes() != null ? session.getNomsEquipes() : "");
            stmt.setInt   (10, session.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la session", e);
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM session WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la session", e);
        }
    }

    public Optional<Session> findById(int id) {
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM session WHERE id=?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la session", e);
        }
        return Optional.empty();
    }

    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM session ORDER BY id DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) sessions.add(mapper(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des sessions", e);
        }
        return sessions;
    }

    public int countBillets(int sessionId) {
        String sql = "SELECT COUNT(*) FROM billet WHERE session_id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des billets", e);
        }
        return 0;
    }

    private Session mapper(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setId          (rs.getInt   ("id"));
        s.setNom         (rs.getString("nom"));
        s.setTypePartie  (TypePartie   .valueOf(rs.getString("type_partie")));
        s.setDureeMinutes(rs.getInt   ("duree_minutes"));
        s.setMaxJoueurs  (rs.getInt   ("max_joueurs"));
        s.setStatut      (StatutSession.valueOf(rs.getString("statut")));
        String date = rs.getString("date_expiration");
        if (date != null) s.setDateExpiration(LocalDate.parse(date));
        try { s.setNombreEquipes   (rs.getInt   ("nombre_equipes")); }    catch (Exception ignored) {}
        try { s.setJoueursParEquipe(rs.getInt   ("joueurs_par_equipe")); } catch (Exception ignored) {}
        try { s.setNomsEquipes     (rs.getString("noms_equipes")); }       catch (Exception ignored) {}
        return s;
    }
}
