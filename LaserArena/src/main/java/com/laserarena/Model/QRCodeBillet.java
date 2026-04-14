package com.laserarena.Model;

public class QRCodeBillet {

    private int    id;
    private byte[] imageBytes;
    private Billet billet;

    public QRCodeBillet() {}

    public QRCodeBillet(byte[] imageBytes, Billet billet) {
        this.imageBytes = imageBytes;
        this.billet     = billet;
    }

    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }
    public byte[] getImageBytes()           { return imageBytes; }
    public void   setImageBytes(byte[] img) { this.imageBytes = img; }
    public Billet getBillet()               { return billet; }
    public void   setBillet(Billet b)       { this.billet = b; }
}
