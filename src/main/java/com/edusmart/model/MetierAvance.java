package com.edusmart.model;

public class MetierAvance {
    private int id;
    private String nom;
    private String description;
    private int metierId;

    public MetierAvance() {}

    public MetierAvance(int id, String nom, String description, int metierId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.metierId = metierId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMetierId() { return metierId; }
    public void setMetierId(int metierId) { this.metierId = metierId; }
}
