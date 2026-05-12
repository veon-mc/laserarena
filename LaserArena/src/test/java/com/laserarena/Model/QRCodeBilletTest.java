package com.laserarena.Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link QRCodeBillet} model class.
 * <p>
 * Covers both constructors, all getters and setters, and edge cases
 * such as {@code null} values and empty byte arrays.
 * All tests follow the AAA (Arrange / Act / Assert) pattern.
 * </p>
 */
@DisplayName("Modèle QRCodeBillet")
public class QRCodeBilletTest {

    private Billet billet;

    /**
     * Tests related to {@link QRCodeBillet} constructors.
     */
    @Nested
    @DisplayName("Constructeur")
    class ConstructeurTest {

        /**
         * Verifies that the no-arg constructor produces an empty {@link QRCodeBillet}
         * with all fields at their default values.
         */
        @Test
        @DisplayName("Le constructeur par défaut crée un objet vide")
        void defaultConstructorCreatesEmptyObject() {
            // Act
            QRCodeBillet qr = new QRCodeBillet();

            // Assert
            assertEquals(0, qr.getId());
            assertNull(qr.getImageBytes());
            assertNull(qr.getBillet());
        }

        /**
         * Verifies that the parameterized constructor correctly initializes
         * {@code imageBytes} and {@code billet}.
         */
        @Test
        @DisplayName("Le constructeur paramétré initialise imageBytes et billet")
        void parameterizedConstructorInitializesFields() {
            // Arrange
            byte[] img = {1, 2, 3};

            // Act
            QRCodeBillet qr = new QRCodeBillet(img, billet);

            // Assert
            assertArrayEquals(img, qr.getImageBytes());
            assertSame(billet, qr.getBillet());
        }

        /**
         * Verifies that the parameterized constructor does not assign an id,
         * leaving it at {@code 0} until the entity is persisted.
         */
        @Test
        @DisplayName("Le constructeur paramétré ne définit pas l'id")
        void parameterizedConstructorDoesNotSetId() {
            // Act
            QRCodeBillet qr = new QRCodeBillet(new byte[]{1}, billet);

            // Assert
            assertEquals(0, qr.getId());
        }
    }

    /**
     * Tests for getters and setters of the {@link QRCodeBillet} model.
     */
    @Nested
    @DisplayName("Getters et Setters")
    class GettersSettersTest {

        /**
         * Verifies that {@link QRCodeBillet#setId(int)} correctly updates the id.
         */
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

        /**
         * Verifies that {@link QRCodeBillet#setImageBytes(byte[])} correctly updates
         * the raw PNG image data.
         */
        @Test
        @DisplayName("setImageBytes / getImageBytes")
        void imageBytesGetterSetter() {
            // Arrange
            byte[]       img = {10, 20, 30};
            QRCodeBillet qr  = new QRCodeBillet();

            // Act
            qr.setImageBytes(img);

            // Assert
            assertArrayEquals(img, qr.getImageBytes());
        }

        /**
         * Verifies that {@link QRCodeBillet#setImageBytes(byte[])} accepts {@code null},
         * effectively clearing the stored image.
         */
        @Test
        @DisplayName("setImageBytes accepte null")
        void imageBytesAcceptsNull() {
            // Arrange
            QRCodeBillet qr = new QRCodeBillet(new byte[]{1}, billet);

            // Act
            qr.setImageBytes(null);

            // Assert
            assertNull(qr.getImageBytes());
        }

        /**
         * Verifies that {@link QRCodeBillet#setImageBytes(byte[])} accepts an empty array.
         */
        @Test
        @DisplayName("setImageBytes accepte un tableau vide")
        void imageBytesEmptyArray() {
            // Arrange
            QRCodeBillet qr = new QRCodeBillet();

            // Act
            qr.setImageBytes(new byte[0]);

            // Assert
            assertEquals(0, qr.getImageBytes().length);
        }

        /**
         * Verifies that {@link QRCodeBillet#setBillet(Billet)} correctly associates
         * a ticket with the QR code entity.
         */
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

        /**
         * Verifies that {@link QRCodeBillet#setBillet(Billet)} accepts {@code null},
         * effectively removing the ticket association.
         */
        @Test
        @DisplayName("setBillet accepte null")
        void billetAcceptsNull() {
            // Arrange
            QRCodeBillet qr = new QRCodeBillet(new byte[]{1}, billet);

            // Act
            qr.setBillet(null);

            // Assert
            assertNull(qr.getBillet());
        }
    }
}
