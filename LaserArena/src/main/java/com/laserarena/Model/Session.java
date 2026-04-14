package com.laserarena.Model;

import java.time.LocalDate;

public class Session {

    private int           id;
    private String        nom;
    private TypePartie    typePartie;
    private int           dureeMinutes;
    private int           maxJoueurs;
    private StatutSession statut;
    private LocalDate     dateExpiration;

    public Session() {}

    public Session(String nom, TypePartie typePartie, int dureeMinutes, int maxJoueurs, StatutSession statut) {
        this.nom          = nom;
        this.typePartie   = typePartie;
        this.dureeMinutes = dureeMinutes;
        this.maxJoueurs   = maxJoueurs;
        this.statut       = statut;
    }

    public int           getId()             { return id; }
    public void          setId(int id)       { this.id = id; }
    public String        getNom()            { return nom; }
    public void          setNom(String nom)  { this.nom = nom; }
    public TypePartie    getTypePartie()     { return typePartie; }
    public void          setTypePartie(TypePartie t) { this.typePartie = t; }
    public int           getDureeMinutes()   { return dureeMinutes; }
    public void          setDureeMinutes(int d) { this.dureeMinutes = d; }
    public int           getMaxJoueurs()     { return maxJoueurs; }
    public void          setMaxJoueurs(int m){ this.maxJoueurs = m; }
    public StatutSession getStatut()         { return statut; }
    public void          setStatut(StatutSession s) { this.statut = s; }
    public LocalDate     getDateExpiration() { return dateExpiration; }
    public void          setDateExpiration(LocalDate d) { this.dateExpiration = d; }
}
