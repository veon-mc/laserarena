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
    // Pour EQUIPES et CAPTURE_DRAPEAU
    private int           nombreEquipes;
    private int           joueursParEquipe;
    private String        nomsEquipes; // JSON simple: "Alpha,Beta,Gamma"

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
    public int           getNombreEquipes()  { return nombreEquipes; }
    public void          setNombreEquipes(int n) { this.nombreEquipes = n; }
    public int           getJoueursParEquipe() { return joueursParEquipe; }
    public void          setJoueursParEquipe(int n) { this.joueursParEquipe = n; }
    public String        getNomsEquipes()    { return nomsEquipes; }
    public void          setNomsEquipes(String n) { this.nomsEquipes = n; }
}
