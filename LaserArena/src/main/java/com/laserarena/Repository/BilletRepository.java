package com.laserarena.Repository;

import com.laserarena.Model.Billet;
import com.laserarena.Model.StatutBillet;
import com.laserarena.Model.Session;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BilletRepository {

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
                if (rs.next()) return Optional.of(mapperAvecSession(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du billet", e);
        }

        return Optional.empty();
    }

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

            while (rs.next()) billets.add(mapperAvecSession(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des billets", e);
        }

        return billets;
    }

    public void delete(int id) {
        String sql = "DELETE FROM billet WHERE id=?";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du billet", e);
        }
    }

    private Billet mapper(ResultSet rs) throws SQLException {
        Billet b = new Billet();
        b.setId(rs.getInt("id"));
        b.setCode(rs.getString("code"));
        b.setStatut(StatutBillet.valueOf(rs.getString("statut")));
        b.setDateExpiration(LocalDate.parse(rs.getString("date_expiration")));
        return b;
    }

    private Billet mapperAvecSession(ResultSet rs) throws SQLException {
        Billet b = mapper(rs);

        Session s = new Session();
        s.setId(rs.getInt("s_id"));
        s.setNom(rs.getString("nom"));

        b.setSession(s);
        return b;
    }
}