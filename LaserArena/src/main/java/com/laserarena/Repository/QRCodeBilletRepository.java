package com.laserarena.Repository;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.laserarena.Model.Billet;
import com.laserarena.Model.QRCodeBillet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class QRCodeBilletRepository {

    private static final int QR_SIZE = 300;

    // ------------------------------------------------------------------ //
    //  Génère le QR code et le sauvegarde en base                        //
    // ------------------------------------------------------------------ //
    public void genererEtSauvegarder(Billet billet) {
        byte[] imageBytes = genererQRCode(billet.getCode());
        save(new QRCodeBillet(imageBytes, billet));
    }

    public void save(QRCodeBillet qr) {
        String sql = "INSERT INTO qrcode_billet (image_bytes, billet_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setBytes(1, qr.getImageBytes());
            stmt.setInt  (2, qr.getBillet().getId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) qr.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du QR code", e);
        }
    }

    public byte[] findImageByBillet(Billet billet) {
        String sql = "SELECT image_bytes FROM qrcode_billet WHERE billet_id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billet.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBytes("image_bytes");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du QR code", e);
        }
        return null;
    }

    public Optional<QRCodeBillet> findByBillet(Billet billet) {
        String sql = "SELECT * FROM qrcode_billet WHERE billet_id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billet.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    QRCodeBillet qr = new QRCodeBillet();
                    qr.setId        (rs.getInt  ("id"));
                    qr.setImageBytes(rs.getBytes("image_bytes"));
                    qr.setBillet    (billet);
                    return Optional.of(qr);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du QR code", e);
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------ //
    //  Génération de l'image QR via ZXing                                //
    // ------------------------------------------------------------------ //
    private byte[] genererQRCode(String contenu) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(contenu, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }
}
