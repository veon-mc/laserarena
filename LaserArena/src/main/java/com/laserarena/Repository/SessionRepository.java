package com.laserarena.Repository;

import com.laserarena.Model.Session;
import com.laserarena.Model.StatutSession;
import com.laserarena.Model.TypePartie;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository providing CRUD operations for {@link Session} entities in the SQLite database.
 * <p>
 * Handles saving, updating, deleting, and querying sessions. Also provides a method
 * to count the number of tickets associated with a given session.
 * All methods obtain the database connection through {@link DatabaseConnection#getInstance()}.
 * </p>
 */
public class SessionRepository {

    /**
     * Saves a new session to the database and sets its generated id.
     *
     * @param session the session to save
     * @throws RuntimeException if a database error occurs
     */
    public void save(Session session) {
        String sql = """
            INSERT INTO session (nom, type_partie, duree_minutes, max_joueurs, statut, date_expiration,
                                 nombre_equipes, joueurs_par_equipe, noms_equipes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, session.getNom());
            stmt.setString(2, session.getTypePartie().name());
            stmt.setInt(3, session.getDureeMinutes());
            stmt.setInt(4, session.getMaxJoueurs());
            stmt.setString(5, session.getStatut().name());
            stmt.setString(6, session.getDateExpiration() != null ? session.getDateExpiration().toString() : null);
            stmt.setInt(7, session.getNombreEquipes());
            stmt.setInt(8, session.getJoueursParEquipe());
            stmt.setString(9, session.getNomsEquipes() != null ? session.getNomsEquipes() : "");

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    session.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de la session", e);
        }
    }

    /**
     * Updates all fields of an existing session in the database.
     *
     * @param session the session with updated values
     * @throws RuntimeException if a database error occurs
     */
    public void update(Session session) {
        String sql = """
            UPDATE session SET nom=?, type_partie=?, duree_minutes=?, max_joueurs=?, statut=?,
                               date_expiration=?, nombre_equipes=?, joueurs_par_equipe=?, noms_equipes=?
            WHERE id=?
        """;

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, session.getNom());
            stmt.setString(2, session.getTypePartie().name());
            stmt.setInt(3, session.getDureeMinutes());
            stmt.setInt(4, session.getMaxJoueurs());
            stmt.setString(5, session.getStatut().name());
            stmt.setString(6, session.getDateExpiration() != null ? session.getDateExpiration().toString() : null);
            stmt.setInt(7, session.getNombreEquipes());
            stmt.setInt(8, session.getJoueursParEquipe());
            stmt.setString(9, session.getNomsEquipes() != null ? session.getNomsEquipes() : "");
            stmt.setInt(10, session.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la session", e);
        }
    }

    /**
     * Deletes a session and all its associated data (QR codes and tickets)
     * from the database by its id.
     * <p>
     * The deletion is performed in the correct order to respect foreign key
     * constraints: QR codes first, then tickets, then the session itself.
     * </p>
     *
     * @param id the id of the session to delete
     * @throws RuntimeException if a database error occurs
     */
    public void delete(int id) {
        String deleteQrCodes = "DELETE FROM qrcode_billet WHERE billet_id IN (SELECT id FROM billet WHERE session_id=?)";
        String deleteBillets = "DELETE FROM billet WHERE session_id=?";
        String deleteSession = "DELETE FROM session WHERE id=?";

        Connection conn = DatabaseConnection.getInstance();

        try {
            try (PreparedStatement stmt = conn.prepareStatement(deleteQrCodes)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(deleteBillets)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(deleteSession)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la session", e);
        }
    }

    /**
     * Finds a session by its id.
     *
     * @param id the session id to search for
     * @return an {@link Optional} containing the session if found, or empty otherwise
     * @throws RuntimeException if a database error occurs
     */
    public Optional<Session> findById(int id) {
        String sql = "SELECT * FROM session WHERE id=?";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la session", e);
        }

        return Optional.empty();
    }

    /**
     * Retrieves all sessions from the database, ordered by id descending.
     *
     * @return the list of all sessions
     * @throws RuntimeException if a database error occurs
     */
    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT * FROM session ORDER BY id DESC";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sessions.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des sessions", e);
        }

        return sessions;
    }

    /**
     * Counts the number of tickets associated with a given session.
     *
     * @param sessionId the id of the session
     * @return the number of tickets for the specified session
     * @throws RuntimeException if a database error occurs
     */
    public int countBillets(int sessionId) {
        String sql = "SELECT COUNT(*) FROM billet WHERE session_id=?";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des billets", e);
        }

        return 0;
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Session} object.
     * <p>
     * Team-related columns ({@code nombre_equipes}, {@code joueurs_par_equipe},
     * {@code noms_equipes}) are read defensively to support older database schemas
     * where these columns may not exist.
     * </p>
     *
     * @param rs the result set positioned on the current row
     * @return the mapped session
     * @throws SQLException if a required column cannot be read
     */
    private Session mapper(ResultSet rs) throws SQLException {
        Session s = new Session();

        s.setId(rs.getInt("id"));
        s.setNom(rs.getString("nom"));
        s.setTypePartie(TypePartie.valueOf(rs.getString("type_partie")));
        s.setDureeMinutes(rs.getInt("duree_minutes"));
        s.setMaxJoueurs(rs.getInt("max_joueurs"));
        s.setStatut(StatutSession.valueOf(rs.getString("statut")));

        String date = rs.getString("date_expiration");
        if (date != null) {
            s.setDateExpiration(LocalDate.parse(date));
        }

        try {
            s.setNombreEquipes(rs.getInt("nombre_equipes"));
        } catch (Exception ignored) {}

        try {
            s.setJoueursParEquipe(rs.getInt("joueurs_par_equipe"));
        } catch (Exception ignored) {}

        try {
            s.setNomsEquipes(rs.getString("noms_equipes"));
        } catch (Exception ignored) {}

        return s;
    }
}