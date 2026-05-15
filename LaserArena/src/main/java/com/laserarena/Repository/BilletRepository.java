package com.laserarena.Repository;

import com.laserarena.Model.Billet;
import com.laserarena.Model.StatutBillet;
import com.laserarena.Model.Session;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository providing CRUD operations for {@link Billet} entities in the SQLite database.
 * <p>
 * Handles saving, updating, deleting, and querying tickets. Tickets can be retrieved
 * individually by code, filtered by session, or listed in their entirety.
 * All methods obtain the database connection through {@link DatabaseConnection#getInstance()}.
 * </p>
 */
public class BilletRepository {

    /**
     * Saves a new ticket to the database and sets its generated id.
     *
     * @param billet the ticket to save
     * @throws RuntimeException if a database error occurs
     */
    public void save(Billet billet) {
        String sql = """
            INSERT INTO billet (code, statut, date_expiration, session_id)
            VALUES (?, ?, ?, ?)
        """;

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, billet.getCode());
            stmt.setString(2, billet.getStatut().name());
            stmt.setString(3, billet.getDateExpiration().toString());
            stmt.setInt(4, billet.getSession().getId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) billet.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du billet", e);
        }
    }

    /**
     * Updates the status of an existing ticket in the database.
     *
     * @param billet the ticket whose status should be updated
     * @throws RuntimeException if a database error occurs
     */
    public void update(Billet billet) {
        String sql = "UPDATE billet SET statut=? WHERE id=?";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, billet.getStatut().name());
            stmt.setInt(2, billet.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du billet", e);
        }
    }

    /**
     * Finds a ticket by its unique code, including its associated session data.
     *
     * @param code the unique ticket code to search for
     * @return an {@link Optional} containing the ticket if found, or empty otherwise
     * @throws RuntimeException if a database error occurs
     */
    public Optional<Billet> findByCode(String code) {
        String sql = """
            SELECT b.*, s.id as s_id, s.nom
            FROM billet b
            JOIN session s ON b.session_id = s.id
            WHERE b.code = ?
        """;

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapperWithSession(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du billet", e);
        }

        return Optional.empty();
    }

    /**
     * Retrieves all tickets belonging to a given session, ordered by id descending.
     *
     * @param session the session whose tickets should be retrieved
     * @return the list of tickets for the specified session
     * @throws RuntimeException if a database error occurs
     */
    public List<Billet> findBySession(Session session) {
        List<Billet> billets = new ArrayList<>();
        String sql = "SELECT * FROM billet WHERE session_id=? ORDER BY id DESC";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, session.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Billet b = mapper(rs);
                    b.setSession(session);
                    billets.add(b);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des billets", e);
        }

        return billets;
    }

    /**
     * Retrieves all tickets from the database with their associated session data,
     * ordered by id descending.
     *
     * @return the list of all tickets
     * @throws RuntimeException if a database error occurs
     */
    public List<Billet> findAll() {
        List<Billet> billets = new ArrayList<>();
        String sql = """
            SELECT b.*, s.id as s_id, s.nom
            FROM billet b
            JOIN session s ON b.session_id = s.id
            ORDER BY b.id DESC
        """;

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) billets.add(mapperWithSession(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des billets", e);
        }

        return billets;
    }

    /**
     * Deletes a ticket and its associated QR code from the database by its id.
     * <p>
     * The QR code is deleted first to respect the foreign key constraint,
     * then the ticket itself is removed.
     * </p>
     *
     * @param id the id of the ticket to delete
     * @throws RuntimeException if a database error occurs
     */
    public void delete(int id) {
        String deleteQr     = "DELETE FROM qrcode_billet WHERE billet_id=?";
        String deleteBillet = "DELETE FROM billet WHERE id=?";

        Connection conn = DatabaseConnection.getInstance();

        try {
            try (PreparedStatement stmt = conn.prepareStatement(deleteQr)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(deleteBillet)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du billet", e);
        }
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Billet} object without session data.
     *
     * @param rs the result set positioned on the current row
     * @return the mapped ticket
     * @throws SQLException if a column cannot be read
     */
    private Billet mapper(ResultSet rs) throws SQLException {
        Billet b = new Billet();
        b.setId(rs.getInt("id"));
        b.setCode(rs.getString("code"));
        b.setStatut(StatutBillet.valueOf(rs.getString("statut")));
        b.setDateExpiration(LocalDate.parse(rs.getString("date_expiration")));
        return b;
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Billet} object including
     * its associated {@link Session} (id and name only).
     *
     * @param rs the result set positioned on the current row
     * @return the mapped ticket with session data
     * @throws SQLException if a column cannot be read
     */
    private Billet mapperWithSession(ResultSet rs) throws SQLException {
        Billet b = mapper(rs);

        Session s = new Session();
        s.setId(rs.getInt("s_id"));
        s.setNom(rs.getString("nom"));

        b.setSession(s);
        return b;
    }
}