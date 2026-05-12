package com.laserarena.Model;

import java.time.LocalDate;

/**
 * Represents a ticket (billet) granting access to a lasergame session.
 * <p>
 * Each ticket has a unique code, a status indicating whether it is valid,
 * already used or expired, an expiration date and a reference to the
 * {@link Session} it belongs to.
 * </p>
 */
public class Billet {

    private int          id;
    private String       code;
    private StatutBillet statut;
    private LocalDate    dateExpiration;
    private Session      session;

    /**
     * Default no-argument constructor.
     */
    public Billet() {}

    /**
     * Constructs a new ticket with the specified properties.
     *
     * @param code           the unique ticket code (typically a UUID)
     * @param statut         the initial ticket status
     * @param dateExpiration the ticket expiration date
     * @param session        the session this ticket is associated with
     */
    public Billet(String code, StatutBillet statut, LocalDate dateExpiration, Session session) {
        this.code           = code;
        this.statut         = statut;
        this.dateExpiration = dateExpiration;
        this.session        = session;
    }

    /**
     * Returns the ticket identifier.
     *
     * @return the ticket id
     */
    public int getId() { return id; }

    /**
     * Sets the ticket identifier.
     *
     * @param id the ticket id
     */
    public void setId(int id) { this.id = id; }

    /**
     * Returns the unique ticket code.
     *
     * @return the ticket code
     */
    public String getCode() { return code; }

    /**
     * Sets the unique ticket code.
     *
     * @param c the ticket code
     */
    public void setCode(String c) { this.code = c; }

    /**
     * Returns the current ticket status.
     *
     * @return the ticket status
     */
    public StatutBillet getStatut() { return statut; }

    /**
     * Sets the ticket status.
     *
     * @param s the new ticket status
     */
    public void setStatut(StatutBillet s) { this.statut = s; }

    /**
     * Returns the ticket expiration date.
     *
     * @return the expiration date
     */
    public LocalDate getDateExpiration() { return dateExpiration; }

    /**
     * Sets the ticket expiration date.
     *
     * @param d the expiration date
     */
    public void setDateExpiration(LocalDate d) { this.dateExpiration = d; }

    /**
     * Returns the session associated with this ticket.
     *
     * @return the associated session
     */
    public Session getSession() { return session; }

    /**
     * Sets the session associated with this ticket.
     *
     * @param s the session to associate
     */
    public void setSession(Session s) { this.session = s; }
}