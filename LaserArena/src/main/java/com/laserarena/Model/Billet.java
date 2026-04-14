package com.laserarena.Model;

import java.time.LocalDate;

public class Billet {

    private int          id;
    private String       code;
    private StatutBillet statut;
    private LocalDate    dateExpiration;
    private Session      session;

    public Billet() {}

    public Billet(String code, StatutBillet statut, LocalDate dateExpiration, Session session) {
        this.code           = code;
        this.statut         = statut;
        this.dateExpiration = dateExpiration;
        this.session        = session;
    }

    public int          getId()             { return id; }
    public void         setId(int id)       { this.id = id; }
    public String       getCode()           { return code; }
    public void         setCode(String c)   { this.code = c; }
    public StatutBillet getStatut()         { return statut; }
    public void         setStatut(StatutBillet s) { this.statut = s; }
    public LocalDate    getDateExpiration() { return dateExpiration; }
    public void         setDateExpiration(LocalDate d) { this.dateExpiration = d; }
    public Session      getSession()        { return session; }
    public void         setSession(Session s) { this.session = s; }
}

