package com.laserarena.Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


@Nested
@DisplayName("QRCodeBillet model")
public class QRCodeBilletTest {

    private Billet billet;

    @Test
    @DisplayName("Default constructor creates an empty object")
    void defaultConstructorCreatesEmptyObject() {
        // Act
        QRCodeBillet qr = new QRCodeBillet();

        // Assert
        assertEquals(0, qr.getId());
        assertNull(qr.getImageBytes());
        assertNull(qr.getBillet());
    }

    @Test
    @DisplayName("Parameterized constructor initializes imageBytes and billet")
    void parameterizedConstructorInitializesFields() {
        // Arrange
        byte[] img = {1, 2, 3};

        // Act
        QRCodeBillet qr = new QRCodeBillet(img, billet);

        // Assert
        assertArrayEquals(img, qr.getImageBytes());
        assertSame(billet, qr.getBillet());
    }

    @Test
    @DisplayName("Parameterized constructor does not set the id")
    void parameterizedConstructorDoesNotSetId() {
        // Act
        QRCodeBillet qr = new QRCodeBillet(new byte[]{1}, billet);

        // Assert
        assertEquals(0, qr.getId());
    }

    @Test
    @DisplayName("setId / getId")
    void idGetterSetter() {
        // Arrange
        QRCodeBillet qr = new QRCodeBillet();

        // Act
        qr.setId(7);

        // Assert
        assertEquals(7, qr.getId());
    }

    @Test
    @DisplayName("setImageBytes / getImageBytes")
    void imageBytesGetterSetter() {
        // Arrange
        byte[] img = {10, 20, 30};
        QRCodeBillet qr = new QRCodeBillet();

        // Act
        qr.setImageBytes(img);

        // Assert
        assertArrayEquals(img, qr.getImageBytes());
    }

    @Test
    @DisplayName("setImageBytes accepts null")
    void imageBytesAcceptsNull() {
        // Arrange
        QRCodeBillet qr = new QRCodeBillet(new byte[]{1}, billet);

        // Act
        qr.setImageBytes(null);

        // Assert
        assertNull(qr.getImageBytes());
    }

    @Test
    @DisplayName("setImageBytes with empty array")
    void imageBytesEmptyArray() {
        // Arrange
        QRCodeBillet qr = new QRCodeBillet();

        // Act
        qr.setImageBytes(new byte[0]);

        // Assert
        assertEquals(0, qr.getImageBytes().length);
    }

    @Test
    @DisplayName("setBillet / getBillet")
    void billetGetterSetter() {
        // Arrange
        QRCodeBillet qr = new QRCodeBillet();

        // Act
        qr.setBillet(billet);

        // Assert
        assertSame(billet, qr.getBillet());
    }

    @Test
    @DisplayName("setBillet accepts null")
    void billetAcceptsNull() {
        // Arrange
        QRCodeBillet qr = new QRCodeBillet(new byte[]{1}, billet);

        // Act
        qr.setBillet(null);

        // Assert
        assertNull(qr.getBillet());
    }
}
