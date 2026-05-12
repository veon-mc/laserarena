package com.laserarena.Model;

import java.time.LocalDate;

/**
 * Represents a lasergame session.
 * <p>
 * A session defines the parameters of a game: its name, game type, duration,
 * maximum number of players, status and expiration date. For team-based game types
 * ({@link TypePartie#EQUIPES} and {@link TypePartie#CAPTURE_DRAPEAU}), additional
 * fields store the number of teams, players per team and team names.
 * </p>
 */
public class Session {

    private int           id;
    private String        nom;
    private TypePartie    typePartie;
    private int           dureeMinutes;
    private int           maxJoueurs;
    private StatutSession statut;
    private LocalDate     dateExpiration;
    private int           nombreEquipes;
    private int           joueursParEquipe;
    /** Comma-separated team names (e.g. {@code "Alpha,Beta,Gamma"}). */
    private String        nomsEquipes;

    /**
     * Default no-argument constructor.
     */
    public Session() {}

    /**
     * Constructs a new session with the specified core properties.
     *
     * @param nom          the session name
     * @param typePartie   the game type
     * @param dureeMinutes the game duration in minutes
     * @param maxJoueurs   the maximum number of players
     * @param statut       the initial session status
     */
    public Session(String nom, TypePartie typePartie, int dureeMinutes, int maxJoueurs, StatutSession statut) {
        this.nom          = nom;
        this.typePartie   = typePartie;
        this.dureeMinutes = dureeMinutes;
        this.maxJoueurs   = maxJoueurs;
        this.statut       = statut;
    }

    /**
     * Returns the session identifier.
     *
     * @return the session id
     */
    public int getId() { return id; }

    /**
     * Sets the session identifier.
     *
     * @param id the session id
     */
    public void setId(int id) { this.id = id; }

    /**
     * Returns the session name.
     *
     * @return the session name
     */
    public String getNom() { return nom; }

    /**
     * Sets the session name.
     *
     * @param nom the session name
     */
    public void setNom(String nom) { this.nom = nom; }

    /**
     * Returns the game type.
     *
     * @return the game type
     */
    public TypePartie getTypePartie() { return typePartie; }

    /**
     * Sets the game type.
     *
     * @param t the game type
     */
    public void setTypePartie(TypePartie t) { this.typePartie = t; }

    /**
     * Returns the game duration in minutes.
     *
     * @return the duration in minutes
     */
    public int getDureeMinutes() { return dureeMinutes; }

    /**
     * Sets the game duration in minutes.
     *
     * @param d the duration in minutes
     */
    public void setDureeMinutes(int d) { this.dureeMinutes = d; }

    /**
     * Returns the maximum number of players allowed in this session.
     *
     * @return the maximum player count
     */
    public int getMaxJoueurs() { return maxJoueurs; }

    /**
     * Sets the maximum number of players.
     *
     * @param m the maximum player count
     */
    public void setMaxJoueurs(int m) { this.maxJoueurs = m; }

    /**
     * Returns the current session status.
     *
     * @return the session status
     */
    public StatutSession getStatut() { return statut; }

    /**
     * Sets the session status.
     *
     * @param s the new session status
     */
    public void setStatut(StatutSession s) { this.statut = s; }

    /**
     * Returns the session expiration date.
     *
     * @return the expiration date
     */
    public LocalDate getDateExpiration() { return dateExpiration; }

    /**
     * Sets the session expiration date.
     *
     * @param d the expiration date
     */
    public void setDateExpiration(LocalDate d) { this.dateExpiration = d; }

    /**
     * Returns the number of teams (for team-based game types).
     *
     * @return the number of teams
     */
    public int getNombreEquipes() { return nombreEquipes; }

    /**
     * Sets the number of teams.
     *
     * @param n the number of teams
     */
    public void setNombreEquipes(int n) { this.nombreEquipes = n; }

    /**
     * Returns the number of players per team (for team-based game types).
     *
     * @return the number of players per team
     */
    public int getJoueursParEquipe() { return joueursParEquipe; }

    /**
     * Sets the number of players per team.
     *
     * @param n the number of players per team
     */
    public void setJoueursParEquipe(int n) { this.joueursParEquipe = n; }

    /**
     * Returns the comma-separated team names.
     *
     * @return the team names string (e.g. {@code "Alpha,Beta,Gamma"})
     */
    public String getNomsEquipes() { return nomsEquipes; }

    /**
     * Sets the comma-separated team names.
     *
     * @param n the team names string
     */
    public void setNomsEquipes(String n) { this.nomsEquipes = n; }
}