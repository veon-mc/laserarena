package com.laserarena.Model;

/**
 * Represents the QR code image associated with a {@link Billet}.
 * <p>
 * Stores the QR code as a byte array containing the image data (typically PNG format)
 * and maintains a reference to the ticket it represents.
 * </p>
 */
public class QRCodeBillet {

    private int    id;
    private byte[] imageBytes;
    private Billet billet;

    /**
     * Default no-argument constructor.
     */
    public QRCodeBillet() {}

    /**
     * Constructs a new QR code with the specified image data and associated ticket.
     *
     * @param imageBytes the QR code image as a byte array
     * @param billet     the ticket this QR code represents
     */
    public QRCodeBillet(byte[] imageBytes, Billet billet) {
        this.imageBytes = imageBytes;
        this.billet     = billet;
    }

    /**
     * Returns the QR code identifier.
     *
     * @return the QR code id
     */
    public int getId() { return id; }

    /**
     * Sets the QR code identifier.
     *
     * @param id the QR code id
     */
    public void setId(int id) { this.id = id; }

    /**
     * Returns the QR code image data as a byte array.
     *
     * @return the image bytes
     */
    public byte[] getImageBytes() { return imageBytes; }

    /**
     * Sets the QR code image data.
     *
     * @param img the image bytes
     */
    public void setImageBytes(byte[] img) { this.imageBytes = img; }

    /**
     * Returns the ticket associated with this QR code.
     *
     * @return the associated ticket
     */
    public Billet getBillet() { return billet; }

    /**
     * Sets the ticket associated with this QR code.
     *
     * @param b the ticket to associate
     */
    public void setBillet(Billet b) { this.billet = b; }
}