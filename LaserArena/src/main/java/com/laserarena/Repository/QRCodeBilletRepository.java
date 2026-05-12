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

/**
 * Repository responsible for generating, saving and retrieving QR code images
 * associated with {@link Billet} entities.
 * <p>
 * Uses the ZXing library to generate QR code images in PNG format and stores
 * them as byte arrays in the {@code qrcode_billet} table.
 * </p>
 */
public class QRCodeBilletRepository {

    /** The width and height in pixels of generated QR code images. */
    private static final int QR_SIZE = 300;

    /**
     * Generates a QR code image for the given ticket and saves it to the database.
     *
     * @param billet the ticket for which to generate and save the QR code
     */
    public void generateAndSave(Billet billet) {
        byte[] imageBytes = generateQRCode(billet.getCode());
        save(new QRCodeBillet(imageBytes, billet));
    }

    /**
     * Saves a {@link QRCodeBillet} entity to the database and sets its generated id.
     *
     * @param qr the QR code entity to save
     * @throws RuntimeException if a database error occurs
     */
    public void save(QRCodeBillet qr) {
        String sql = "INSERT INTO qrcode_billet (image_bytes, billet_id) VALUES (?, ?)";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setBytes(1, qr.getImageBytes());
            stmt.setInt(2, qr.getBillet().getId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    qr.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du QR code", e);
        }
    }

    /**
     * Retrieves the QR code image bytes for the given ticket.
     *
     * @param billet the ticket whose QR code image should be retrieved
     * @return the image bytes, or {@code null} if no QR code exists for the ticket
     * @throws RuntimeException if a database error occurs
     */
    public byte[] findImageByBillet(Billet billet) {
        String sql = "SELECT image_bytes FROM qrcode_billet WHERE billet_id=?";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billet.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("image_bytes");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du QR code", e);
        }

        return null;
    }

    /**
     * Retrieves the full {@link QRCodeBillet} entity for the given ticket.
     *
     * @param billet the ticket whose QR code entity should be retrieved
     * @return an {@link Optional} containing the QR code entity if found, or empty otherwise
     * @throws RuntimeException if a database error occurs
     */
    public Optional<QRCodeBillet> findByBillet(Billet billet) {
        String sql = "SELECT * FROM qrcode_billet WHERE billet_id=?";

        Connection conn = DatabaseConnection.getInstance();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billet.getId());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    QRCodeBillet qr = new QRCodeBillet();

                    qr.setId(rs.getInt("id"));
                    qr.setImageBytes(rs.getBytes("image_bytes"));
                    qr.setBillet(billet);

                    return Optional.of(qr);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du QR code", e);
        }

        return Optional.empty();
    }

    /**
     * Generates a QR code image from the given text content using the ZXing library.
     *
     * @param contenu the text to encode in the QR code
     * @return the QR code image as a PNG byte array
     * @throws RuntimeException if encoding or image writing fails
     */
    private byte[] generateQRCode(String contenu) {
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